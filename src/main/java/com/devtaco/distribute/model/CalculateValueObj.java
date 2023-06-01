package com.devtaco.distribute.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class CalculateValueObj {

  @Getter
  private final int id;

  @Getter
  private long valueTsMillis;

  public CalculateValueObj setValueTsMillis(long valueTsMillis) {
    this.valueTsMillis = valueTsMillis;
    return this;
  }

  /** 생성한 시각 timestamp. */
  @Setter
  @Getter
  private long calculateTsMillis;

  @Setter
  @Getter
  private BigDecimal value;

  public CalculateValueObj(int id) {
    this.id = id;
  }

}
