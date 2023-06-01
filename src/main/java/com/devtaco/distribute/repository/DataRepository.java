package com.devtaco.distribute.repository;

import java.util.List;

import com.devtaco.distribute.model.ImplSpec;
import com.devtaco.distribute.model.ImplValue;

/**
 * 지수 Repository 를 정의해둔다. <p>
 * 물론 Component로 띄우기 위해 class로 선언을 할 것이지만. <p>
 * pseudo code 형식이기에 interface 형태로 둔다.
 */
public interface DataRepository {

  List<ImplSpec> getAllJobSpec();

  List<ImplSpec> getJobSpec(int id);

  public List<ImplValue> getLatestImplValue(int id);

  public boolean saveImplValue(ImplValue value);

  public boolean updateImplvalue(ImplValue value);

  public String safeSqlFormat(String fmt, Object... args);

}
