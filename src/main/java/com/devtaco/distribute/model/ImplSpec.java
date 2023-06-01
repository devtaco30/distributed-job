package com.devtaco.distribute.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString
@Getter
@Setter
@Accessors(chain = true)
public class ImplSpec extends JobSpec {

  public ImplSpec(int id, String valueName) {
    super(id, valueName);
  }

  private boolean genFlag;

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
