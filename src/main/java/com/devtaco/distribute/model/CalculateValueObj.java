package com.devtaco.distribute.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 계산된 값을 저장하는 기본 객체입니다.
 * 계산 결과와 관련된 시간 정보를 포함합니다.
 */
@Accessors(chain = true)
public class CalculateValueObj {

  /** 값 식별자 */
  @Getter
  private final int id;

  /** 값의 타임스탬프 (밀리초) */
  @Getter
  private long valueTsMillis;

  public CalculateValueObj setValueTsMillis(long valueTsMillis) {
    this.valueTsMillis = valueTsMillis;
    return this;
  }

  /** 계산 수행 시각의 타임스탬프 */
  @Setter
  @Getter
  private long calculateTsMillis;

  /** 계산된 값 */
  @Setter
  @Getter
  private BigDecimal value;

  public CalculateValueObj(int id) {
    this.id = id;
  }

}
