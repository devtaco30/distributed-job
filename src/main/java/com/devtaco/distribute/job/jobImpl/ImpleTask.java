package com.devtaco.distribute.job.jobImpl;

import java.math.BigDecimal;

import org.apache.shardingsphere.elasticjob.api.ShardingContext;

import com.devtaco.distribute.job.DistributeTask;
import com.devtaco.distribute.model.ImplSpec;
import com.devtaco.distribute.model.ImplValue;
import com.devtaco.distribute.repository.DataManager;
import com.devtaco.distribute.service.SlackService;

public class ImpleTask implements DistributeTask {

  private static final int CALCULATION_MAX_RETRY = 3; // 값 계산 -> 검증후 fail 인 경우, 몇 번까지 retry 할 건지..

  private final ImplSpec spec;

  private final DataManager dataManager;

  private SlackService slackBot;

  private ImplValue value;

  public ImpleTask(ImplSpec spec, DataManager dataManager, SlackService slackBot) {
    this.spec = spec;
    this.dataManager = dataManager;
    this.slackBot = slackBot;

    initialize();
  }

  @Override
  public void initialize() {
    // 새 객체 assign
    this.value = new ImplValue(spec.getId());
  }

  // ====================================== 실제 Job 이 돌아가는 부분 Start ==============================

  @Override
  public void calculate() {

    BigDecimal calculatedValue = new BigDecimal("1000");

    this.value.setValue(calculatedValue);

    dataManager.saveValue(this.value);

    slackBot.sendAlert("value Calculated! " + value.getId() + ":" + value.getValue());
  }

  // ====================================== 실제 Job 이 돌아가는 부분 End ==============================

  /**
   * @return 검증이 OK 이면( 재계산이 필요 없으면 ) true. FAIL 이면, false 를 리턴한다.
   */
  public boolean validateCalculation() {
    // ==> 어느 정도까지를 오차 인정할건지(tolerance 개념) 정의해야함.
    return true;
  }

  @Override
  public void execute(ShardingContext shardingContext) {

    // generate State 가 false 면 산출 X
    if (false == spec.getGenFlag()) {
      return;
    }

    int tryCount = 0;

    do {
      calculate();

      if (validateCalculation()) {
        break;
      }

      tryCount++;
    }

    while (tryCount < CALCULATION_MAX_RETRY);

  }

}
