package com.devtaco.distribute.model;

import com.devtaco.distribute.util.CronUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 작업 스펙의 기본 속성을 정의하는 추상 클래스입니다.
 * 작업의 식별자, 이름, 실행 조건 등 기본 정보를 관리합니다.
 */
@Setter
@Getter
@Accessors(chain = true)
@ToString
public abstract class JobSpec {

  /**
   * 작업 스펙을 생성합니다.
   * 
   * @param id 작업 식별자
   * @param jobName 작업 이름
   */
  public JobSpec(int id, String jobName) {
    this.id = id;
    this.jobName = jobName;
  }

  @Getter(AccessLevel.NONE)
  private int id;

  @NonNull
  private String jobName;

  /** job 을 돌릴 cron 표현식 */
  private String cronExpression;

  /**
   * job 돌릴 것인가 말것인가를 외부에서 정해서 넣을 수 있다.
   */
  private boolean executeFlag;

  public int getId() {
    return id;
  }

  public JobSpec setCronExpression(String cronExpression) {
    if (null == cronExpression || cronExpression.isEmpty()) {
      this.cronExpression = null;
      return this;
    }
    this.cronExpression = CronUtils.toCanonicalExpression(cronExpression);

    return this; // fluent api style..
  }

}
