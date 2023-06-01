package com.devtaco.distribute.job;

import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

/**
 * 특정 값을 산출하는 task 라고 하자<p>
 * 1. init - 객체 세팅, 원천 소스를 가져올 준비 <p>
 * 2. calculate - 계산 : 객체와 계산 모듈간의 데이터 주고 받고 계산하고 save <p>
 * 3. validate - 계산 결과가 맞는지 검증 <p>
 */
public interface DistributeTask extends SimpleJob {
  
	void initialize();

	void calculate();

	boolean validateCalculation();

}
