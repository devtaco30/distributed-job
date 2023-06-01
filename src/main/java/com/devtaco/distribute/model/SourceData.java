package com.devtaco.distribute.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class SourceData implements Comparable<SourceData> {

  private BigDecimal weight;

  /** 지수 산출에 필요한 특정 source list */
  @JsonIgnore // 저장 필요 X
  private List<String> sourceList;

  public void clearSourceList() {
    this.sourceList.clear();
  }

  public SourceData() {
    this.sourceList = new ArrayList<>();
  }

  public void addWeight(BigDecimal eachSectionWeight) {
    this.weight = weight.add(eachSectionWeight);
  }

  public void weightSetToZero() {
    this.weight = BigDecimal.ZERO;
  }

  /**
   * 각 AssetsData 의 weight 를 기준으로 정렬할 때 사용한다.
   */
  @Override
  public int compareTo(SourceData other) {

    if (this.weight.compareTo(other.weight) > 0) {
      return 1;
    } else if (this.weight.compareTo(other.weight) == 0) {
      return 0;
    } else
      return -1;
  }

  @Override
  public boolean equals(Object other) {

    // null check, class check
    if (false == (other instanceof SourceData)) {
      return false;
    }

    SourceData otherSourceData = (SourceData) other;

    return this.sourceList.equals(otherSourceData.sourceList);
  }

  @Override
  public int hashCode() {
    StringBuilder builder = new StringBuilder();
    this.sourceList.stream().forEach(builder::append);
    return builder.toString().hashCode();
  }

}
