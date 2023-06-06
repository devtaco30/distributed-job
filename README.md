# Distributed-job-processor

## Project Description
- Apache 의 Elastic-Job-lite 를 사용하여 프로젝트 내에서 규정한 job 을 HA 된 instance 들로 분산하여 처리하도록 하는 project이다.

## Package Structure
- 계층형 구조로 구성합니다.
```
src_root
├── config
├── job
├── model
├── repository
├── service
├── util
├── zk
resources
├── application-xxx.yml
```
  
- Description
    - config : Application Configuration 을 넣어둡니다. 
      - CoordinatorRegistryCenter 또한 이곳에 설정합니다.
      - 그 외의 SlackBot, RateLimitRule, OkHttpClientPool, ThreadPool 등을 미리 설정해두었습니다. 
    - job: 스케쥴링으로 돌릴 job 을 정의합니다.
      - jobImpl : DistributeTask 의 구현체들을 넣어둡니다.
      - DistributeTask.java : ElasticJob 의 SimpleJob 을 상속한 interface 입니다. jobImpl 에 구현체를 생성합니다.
      - JobRegistration.java : jobImpl 이 등록되고 execute 하는 center 입니다.
    - model: job 혹은 그 외 관련된 추상화 되어야할 개체들의 모델링들이 존재합니다.
    - repostry: DB 관련 facacde 패턴의 컴포넌트나, repository 컴포넌트가 존재합니다.
    - service : job 이외의 비지니스 로직 service 컴포넌트들이 존재합니다.    
- 해당 구조에 없는 패키지가 추가해야될 경우 개발자 재량에 의해 추가 할 수 있습니다. 

## Installation and Run (for local)
- RDB 를 하나 만들고 Job 을 정의합니다. 
- Job Spec 을 추상화하여 class 만들고, table 에 해당 spec 의 실질적인 value 들을 넣습니다.
- JobRegistration 에서는 해당 spec 을 읽어들여 ImplSpec 으로 만들고, job 을 등록 하고 execute합니다.
  

## Test Case
- TBD

## CI/CD Principle
- TBD