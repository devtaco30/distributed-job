package com.devtaco.distribute.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.ToString;

/**
 * 소스 데이터와 그 가중치를 관리하는 클래스입니다.
 * 가중치 기반 정렬을 지원합니다.
 */
@ToString
@Getter
public class SourceData implements Comparable<SourceData> {

  /** 데이터의 가중치 */
  private BigDecimal weight;

  /** 지수 산출에 필요한 소스 목록 */
  @JsonIgnore // 저장이 필요하지 않은 임시 데이터
  private List<String> sourceList;

  public SourceData() {
    this.sourceList = new ArrayList<>();
    this.weight = BigDecimal.ZERO;  // 초기화 추가
  }

  public void clearSourceList() {
    this.sourceList.clear();
  }

  /**
   * 소스 목록의 가중치를 추가합니다.
   * @param eachSectionWeight 추가할 섹션 가중치
   */
  public void addWeight(BigDecimal eachSectionWeight) {
    this.weight = Objects.requireNonNull(weight).add(
        Objects.requireNonNull(eachSectionWeight, "Section weight cannot be null")
    );
  }

  public void weightSetToZero() {
    this.weight = BigDecimal.ZERO;
  }

  /**
   * 각 AssetsData의 weight를 기준으로 정렬할 때 사용합니다.
   */
  @Override
  public int compareTo(SourceData other) {
    return this.weight.compareTo(other.weight);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof SourceData)) {
      return false;
    }
    SourceData otherSourceData = (SourceData) other;
    return this.sourceList.equals(otherSourceData.sourceList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceList);  // Objects.hash 사용으로 변경
  }

}
