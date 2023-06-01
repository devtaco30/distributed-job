package com.devtaco.distribute.model;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Accessors( chain = true )
@ToString
public class ImplValue extends CalculateValueObj {

  public ImplValue( int id ){
    super(id);
  }

  @Setter
  private Map<String, SourceData> sourceClassifyByKey;

 
  public BigDecimal calculateValue() {
    return BigDecimal.ONE;
  }
  
}
