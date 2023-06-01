package com.devtaco.distribute.util;

import java.time.ZonedDateTime;

public class CronUtils {
  public static final String TIMEZONE_ID_UTC = "UTC";

  // accidentally instantiation 막기 위해 constructor 를 private 로 만듬
  private CronUtils(){ }

  /**
   * 비표준(?) expression 일 경우 Quartz 의 cron expression 의 표현식 기준으로 변환한다.
   * @param cronExporession
   * @return 기본 조건 위반( 6개의 항목이 아니거나, empty string 이거나)인 경우, 입력 arg 그대로.
   *          그 외의 정상 조건이면, quartz 에 사용하는 데 올바른 String 으로 변환된다.
   */
  public static String toCanonicalExpression(String cronExporession) {
    if ( null == cronExporession || cronExporession.isEmpty() ){
      return cronExporession;
    }

    // 각 part 값 분리.
    String[] parts = cronExporession.split(" ");

    // [5] 를 밑에서 access 해야해서, 길이가 6 보다 작으면 안됨
    if ( null == parts || parts.length < 6 ) {
      return cronExporession;
    }

    // last part( day of month ) 가 "*" 면 "?" 바꾸자.
    // day of month 가 "*" 가 되면 불가한 게 아니라, day of week, day of month 가 동시에 "*" 면 안되기 때문에
    // 원래 의도한 "아무 달" 의도면 정확한 표현으로 바꾸어 놓으면 day of week 이 "*" 가 되어도 괜찮기 때문이다.
    if ( "*".equals( parts[ 5 ]) ){
      parts[ 5 ] = "?";
    }

    return String.join(" ", parts); // 다시 cron expression ( space sparated String ) 으로 만든다.
  }

  /**
   * cron expression 에 따라, 특정 시간(Instant) 이후 실행 예상 시점을 받는다.
   * 
   * @param strCronExpr       cron expression string. 
   *                          spring 의 것을 사용하면 되지만, 가급적 범용 형태를 사용할 것.(spring dependent X)
   * 
   * @param afterDateTimeUTC  기준 시간. 이 시간 이후의 값으로 받기 위해 준다.
   * 
   * @return afterInstantUTC 이후 실행될 시간( {@link java.time.ZonedDateTime ZonedDateTime} )
   */
  public static ZonedDateTime nextExecutionDate( String strCronExpr, ZonedDateTime afterDateTimeUTC ){

    // spring 의 CronExpression 을 사용한다. CronExpression 은 Quartz lib. 에도 있어서,
    // fully qualified class name 으로 instantiation 한다.
    org.springframework.scheduling.support.CronExpression cronExpression
      = org.springframework.scheduling.support.CronExpression.parse( strCronExpr );

    return cronExpression.next( afterDateTimeUTC );
  }
}
