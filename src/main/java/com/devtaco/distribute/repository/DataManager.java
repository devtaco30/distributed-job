package com.devtaco.distribute.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.devtaco.distribute.model.ImplSpec;
import com.devtaco.distribute.model.ImplValue;

/** facade pattern을 위해 만들어뒀다 */
@Component
public class DataManager {

  private DataRepository dataRepo;

  public DataManager(DataRepository dataRepo) {
    this.dataRepo = dataRepo;
  }

  public String getValueName(int id) {
    return "valueName";
  }

  public List<ImplSpec> getAllSpec() {
    return new ArrayList<ImplSpec>();
  }

  public ImplSpec getSpec(int id) {
    return new ImplSpec(id, "valueName");
  }

  public void saveValue(ImplValue value) {
  }

}
