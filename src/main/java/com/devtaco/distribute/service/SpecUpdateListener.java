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
 * PostgreSQL의 데이터베이스 변경 이벤트를 감지하고 작업을 관리하는 리스너입니다.
 * 
 * 동작 방식:
 * 1. PostgreSQL LISTEN 명령을 통해 특정 채널('watch__spec_update')을 구독
 * 2. 테이블의 INSERT/UPDATE/DELETE 이벤트 발생 시 알림 수신
 * 3. 수신된 이벤트에 따라 작업(Job)을 등록/수정/삭제
 * 
 * @see JobRegistration
 * @see DataManager
 */
@Slf4j
@Component
public class SpecUpdateListener {

    /** 데이터베이스 변경 이벤트를 수신할 채널명 */
    private static final String LISTEN_SPEC_UPDATE_CHANNEL = "watch__spec_update";

    /**
     * 데이터베이스 작업 유형을 정의하는 열거형
     */
    public enum Operation {
        INSERT(1),
        UPDATE(2),
        DELETE(3);

        private final int opertationProp;

        private Operation(int opertationProp) {
            this.opertationProp = opertationProp;
        }

        public int getAsInt() { 
            return opertationProp;
        }
    }

    /** 작업 등록을 담당하는 컴포넌트 */
    private JobRegistration jobRegister;

    /** 데이터 관리를 담당하는 컴포넌트 */
    private DataManager dataMgr;
    
    /** PostgreSQL 연결 및 알림 수신을 위한 객체들 */
    private JdbcTemplate jdbcTemplate;
    private PGConnection pgconn;

    /** 데이터베이스 변경 감지를 위한 스케줄러 */
    private ThreadPoolTaskScheduler scheduler = ThreadPoolConfig.getInstance().getScheduledThreadPool();

    /** 알림 발송을 위한 서비스 */
    private SlackService slackBot;

    /**
     * 리스너를 초기화하고 데이터베이스 감시를 시작합니다.
     * 
     * @param jdbcTemplate JDBC 템플릿
     * @param dataMgr 데이터 관리자
     * @param jobRegister 작업 등록기
     * @param slackBot 슬랙 알림 서비스
     */
    public SpecUpdateListener(JdbcTemplate jdbcTemplate, DataManager dataMgr, 
            JobRegistration jobRegister, SlackService slackBot) {
        this.dataMgr = dataMgr;
        this.jobRegister = jobRegister;
        this.slackBot = slackBot;
        this.jdbcTemplate = jdbcTemplate;
        listenSpecTable();
    }

    /**
     * PostgreSQL LISTEN 명령을 실행하여 테이블 변경 감지를 시작합니다.
     * 
     * @throws SQLException 데이터베이스 연결 또는 LISTEN 명령 실행 실패 시
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
     * 변경 감지 스케줄러를 초기화하고 시작합니다.
     * UTC 시간대를 기준으로 동작합니다.
     */
    @PostConstruct
    public void start() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        TimeZone tz = TimeZone.getDefault();
        CronTrigger cronTrigger = new CronTrigger("cron_expression", tz);
        this.scheduler.schedule( scheduledListen(), cronTrigger);
    }

    /**
     * 데이터베이스 변경 이벤트를 처리하는 메인 로직입니다.
     * 
     * 처리 흐름:
     * 1. PostgreSQL 알림 수신
     * 2. 알림 내용(payload) 파싱
     * 3. 작업 유형에 따른 처리:
     *    - INSERT: 새 작업 등록
     *    - UPDATE: 기존 작업 제거 후 재등록
     *    - DELETE: 작업 제거
     * 
     * @throws SQLException PostgreSQL 알림 수신 실패 시
     * @throws JsonProcessingException JSON 파싱 실패 시
     * @throws NullPointerException 필수 데이터 누락 시
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
     * 데이터베이스 변경사항을 작업 시스템에 적용합니다.
     * 
     * @param id 작업 식별자
     * @param opProperty 작업 유형 (1:등록, 2:수정, 3:삭제)
     */
    private void applyChangeToJob(int id, int opProperty) {

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
