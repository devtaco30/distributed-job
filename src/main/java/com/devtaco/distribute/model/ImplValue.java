package com.devtaco.distribute.model;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * CalculateValueObj를 확장한 구현체입니다.
 * 소스 데이터의 분류와 계산 로직을 포함합니다.
 */
@Getter
@Accessors( chain = true )
@ToString
public class ImplValue extends CalculateValueObj {

  public ImplValue( int id ){
    super(id);
  }

  /** 키별로 분류된 소스 데이터 맵 */
  @Setter
  private Map<String, SourceData> sourceClassifyByKey;

 
  public BigDecimal calculateValue() {
    return BigDecimal.ONE;
  }
  
}
