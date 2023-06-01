package com.devtaco.distribute.model;

import com.devtaco.distribute.util.CronUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 산출 value 와 그 값이 계산될 때의 정보를 담는다. <p/>
 * impl 에서 이 value 를 도출하는 business 로직을 작성한다.
 */
@Setter
@Getter
@Accessors(chain = true)
@ToString
public abstract class JobSpec {

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
