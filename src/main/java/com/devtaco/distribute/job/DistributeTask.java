package com.devtaco.distribute.job;

import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

/**
 * 분산 작업의 기본 인터페이스입니다.
 * 작업의 생명주기를 3단계로 정의합니다.
 * 
 * 작업 처리 순서:
 * 1. initialize(): 작업 실행을 위한 초기화 단계
 * 2. calculate(): 실제 계산 작업을 수행하는 단계
 * 3. validateCalculation(): 계산 결과의 유효성을 검증하는 단계
 * 
 * @see org.apache.shardingsphere.elasticjob.simple.job.SimpleJob
 */
public interface DistributeTask extends SimpleJob {
  
	/**
	 * 작업 실행 전 필요한 객체와 데이터를 초기화합니다.
	 * 원천 데이터 소스를 준비하는 단계입니다.
	 */
	void initialize();

	/**
	 * 실제 계산 작업을 수행합니다.
	 * 초기화된 데이터를 기반으로 계산하고 결과를 저장합니다.
	 */
	void calculate();

	/**
	 * 계산된 결과의 유효성을 검증합니다.
	 * @return 검증 결과 (true: 성공, false: 실패)
	 */
	boolean validateCalculation();

}
