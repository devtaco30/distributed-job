package com.devtaco.distribute.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.devtaco.distribute.config.ThreadPoolConfig;
import com.devtaco.distribute.job.JobRegistration;
import com.devtaco.distribute.model.JobSpec;
import com.devtaco.distribute.repository.DataManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/** 
 * db 에 insert, update, delete 등의 event 가 발생하면 <p>
 * notify function 이 작동한다. <p>
 * 이 function 은 특정 channel로 event 의 내용을 전달한다.
 */
@Slf4j
@Component
public class SpecUpdateListener {

  /** insert, update, delete 모두 받는다. */ 
  private static final String LISTEN_SPEC_UPDATE_CHANNEL = "watch__spec_update";

  /** Operation -> 숫자로 구분한다. */
  public enum Operation {
    INSERT(1),
    UPDATE(2),
    DELETE(3);

    private final int opertationProp;

    private Operation( int opertationProp ){
      this.opertationProp = opertationProp;
    }

    public int getAsInt(){ return opertationProp;}

  }

  /** job 을 등록하는 곳!  listener 로 들어온 spec 으로 job 을 등록 / 제거한다. */
  private JobRegistration jobRegister;

  /** job spec 의 변화는 dataManger 가 들고 있는 정보가 추가/수정 되었다는 소리 , 여기에도 변화를 알려준다. */
  private DataManager dataMgr;
  
  /** 기존의 jdbcTemplate 를 받아서 PGConnection 으로 unwrap 해서 쓸 거다 */
  private JdbcTemplate jdbcTemplate;
	private PGConnection pgconn;

  /** table 의 변화를 listen 하는 scheduler */
  private ThreadPoolTaskScheduler scheduler = ThreadPoolConfig.getInstance().getScheduledThreadPool();

  /** 제대로 읽지 못하면 alert 하자 */
  private SlackService slackBot;

  public SpecUpdateListener(JdbcTemplate jdbcTemplate, DataManager dataMgr, 
  JobRegistration jobRegister, SlackService slackBot ) {
    this.dataMgr = dataMgr;
    this.jobRegister = jobRegister;
    this.slackBot = slackBot;
    this.jdbcTemplate = jdbcTemplate;
    listenSpecTable();
  }

  /** 
   * 생성자에서 call 하며 <p>
   * db 의 spec table 를 listen 하도록 query 를 한다.
   * @throws SQLException
   */
  public void listenSpecTable() { 
    try {
      Connection conn = jdbcTemplate.getDataSource().getConnection();
      Statement stmt = conn.createStatement();
      this.pgconn = conn.unwrap(PGConnection.class);
      stmt.execute("LISTEN " + LISTEN_SPEC_UPDATE_CHANNEL );
      stmt.close();
    } catch (SQLException e) {
      log.error("SpecUpdateListener fail to liseten spec_table", e);
    } 
  }

  /**
   * DB 변경을 확인하는 scheduler 를 돌린다. <p>
   * spec 사항의 추가 / 변경은 그렇게 자주 일어나는 일이 아니다. <p>
   */
  @PostConstruct
  public void start() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    TimeZone tz = TimeZone.getDefault();
    CronTrigger cronTrigger = new CronTrigger("cron_expression", tz);
    this.scheduler.schedule( scheduledListen(), cronTrigger);
  }

  /** 
   * ===============================================<p>
   * 이 listener 가 하는 main 이 여기
   * ===============================================<p>
   * 매초 실행되는 scheduled job <p>
   * db 로 부터 notfiy 되는 payload 를 받아, insert, update, delete 에 맞는 일을 한다.<p>
   * insert -> regist job <p>
   * update -> deregist , regist job <p>
   * delete -> deregist job <p>
   * ===============================================<p>
   * 
   * @throws SQLException
   *          pgconn.getNotifications() 실패
   * @throws JsonProcessingException
   *          payload convert to JsonNode 실패
   * @throws NullPointerException
   *          updateInfo 에 필요한 정보키가 없을 때,
   *          {@link JobRegistration#dereigstJob(int)} 에서 job 을 찾지 못할 때
   */
  public Runnable scheduledListen() {
    return() -> {

      String payload = "";
      JsonNode updatedInfo = null;

			try {

        // db 로 부터 noti가 여러개 올 수 있다. (array)
				PGNotification[] notifications = pgconn.getNotifications();

        if ( notifications == null ) {
          return;
        }

        // update 되는 row 가 그렇게 자주 있지는 않다. 요청이 있을 때만 mapper 만들자
        ObjectMapper mapper = new ObjectMapper();

        // row 개수만큼 반복
				for (int i=0; i<notifications.length; i++) {

          // db 의 수정분을 string 으로 받는다. 
          // operation 과 id 만 받는다.
          payload = notifications[i].getParameter();

          log.info("db update payload -> {}", payload);
          
          updatedInfo = mapper.readTree(payload);

          int id = updatedInfo.get("id").asInt();
          String opStr = updatedInfo.get("operation").asText();
          Operation op = Operation.valueOf(opStr);

          applyChangeToJob(id, op.getAsInt());

          log.info("updatedInfo => {}", updatedInfo.toString());
          slackBot.sendAlert("new spec updated " + updatedInfo.toString());
				}
        // spec 을 update 했는데, 오류가 나면 알림을 줘야한다. 안그러면 모름.
      } catch (SQLException sqlException) {
        String errMsg = "SpecUpdateListener can not listen event";
        log.error(errMsg, sqlException);
        slackBot.sendAlert(errMsg);
      } catch (JsonProcessingException jsonExeption) {
        String errMsg = "SpecUpdateListener can not convert to payload => " + payload;
        log.error(errMsg, jsonExeption);
        slackBot.sendAlert(errMsg);
      } catch (NullPointerException nullException) {
        String errMsg ="";
        if ( !updatedInfo.isEmpty()) {
          errMsg = "SpecUpdateListener can not get spec info from => " + updatedInfo.toString();
        }
        errMsg = "SpecUpdateListeener can not get spec info from null jsonNode";
        log.error(errMsg, nullException);
        slackBot.sendAlert( errMsg );
      }
		};
	
  }

  /**
   * job 의 변경부분을 jobRegister 를 통해 반영한다.
   * @param id
   * @param op (inesrt,update,delete)
   */
  private void applyChangeToJob( int id, int opProperty ) {

    JobSpec spec = null;

    spec = dataMgr.getSpec(id);
    
    switch ( opProperty ) {
      case 1:  // first regist
        jobRegister.registJob( spec );
        break;
      case 2:  // re-regist
        jobRegister.dereigstJob( spec );
        jobRegister.registJob( spec );
        break;
      case 3: // de-regist
        jobRegister.dereigstJob( spec );
        break;
      default:
        log.info("op is not defined");
        break;
    }
  }

}
