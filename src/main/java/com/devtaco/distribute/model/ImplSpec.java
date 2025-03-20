package com.devtaco.distribute.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * JobSpec의 구체적인 구현체입니다.
 * 작업 생성 조건과 실행 여부를 관리합니다.
 */
@ToString
@Getter
@Setter
@Accessors(chain = true)
public class ImplSpec extends JobSpec {

  public ImplSpec(int id, String valueName) {
    super(id, valueName);
  }

  /** 생성 플래그 */
  private boolean genFlag;

  /**
   * 조건에 따라 생성 플래그를 설정합니다.
   * @return 현재 ImplSpec 인스턴스
   */
  public ImplSpec setGenFlagByCondition() {
    // condition 에 다라 generate flag 를 설정한다.
    return this;
  }

  /**
   * {@see SpecUpdateListener} 에 의해 spec update -> job 재등록 -> execute
   * <p>
   * 위 과정에서 asset 개수가 1개 이하로 update 될 경우 {@link #setGenFlagByCondition()} 에 의해 이
   * flag -> false
   * 
   * @return go (true) / no go (false)
   */
  public boolean getGenFlag() {
    return this.genFlag;
  }

  public boolean equalSpec(ImplSpec other) {
    return (other.getId() == getId());
  }

}
