# Project Title: 시대생 맛집 지도

## Project Scope
서울시립대학교 컴퓨터과학부 2024년 소프트웨어공학
프로젝트로, Software Development Life-Cycle 을 기반으로 객체지향
소프트웨어공학 방법론을 적용하여 서울시립대 주변 맛집 탐색 지도 플랫폼 개발

## Project Duration
2024년 2학기

## Highlighted Features
#### 검색어 기반 식당 검색 
사용자는 식당 이름, 음식 카테고리 또는 탐색 지역을 기준으로 식당을 검색할 수 있다. <br>
검색 결과는 필터를 통해 원하는 순서로 정렬할 수 있다.

#### 지도 기반 식당 검색
사용자는 현재 위치를 기반으로 지도에서 주위 식당 정보를 시각적으로 확인할 수 있다. <br>
또한 지도 구역 필터(정문, 후문, 쪽문, 남문)를 통해 각 구역에 해당하는 식당 정보를 손쉽게 확인할 수 있다. <br>

#### 식당 정보 열람
사용자는 식당의 주소, 전화번호 등의 정보를 열람할 수 있다.

#### 리뷰 작성 및 상호작용
사용자는 사진을 포함한 리뷰를 작성하고 평점을 매길 수 있다. 리뷰 이벤트에 참여한 리뷰어는 해당 사실이 리뷰에 표시된다. <br>
사용자는 다른 사용자가 작성한 리뷰를 열람하고 좋아요 및 신고를 수행할 수 있다. <br>
관리자는 부적절한 리뷰를 삭제할 수 있다. <br>

#### 즐겨찾기
사용자는 마음에 드는 식당을 즐겨찾기에 추가할 수 있다.

#### 사용자 계정 생성
사용자는 서울시립대 웹메일과 비밀번호로 계정을 생성할 수 있다.

#### 사용자 로그인/로그아웃
사용자는 자신의 계정을 로그인, 로그아웃 할 수 있다.

#### 사용자 프로필 수정 
사용자는 자신의 이름, 사진 등의 프로필 정보를 수정할 수 있다. 

#### 정보 수정 제안
사용자는 특정 식당이나 새로운 식당의 정보에 대해 수정을 제안할 수 있다.

#### 관리자 권한
관리자는 사용자들의 신고 및 정보 수정 제안 목록을 확인할 수 있고, 이를 반영하여 데이터베이스를 관리한다.

#### DEMO VIDEO: <<관련 문서 링크>>

## Project Constraints
### 기존 예상했던 제약사항
#### 플랫폼 및 기술적 제약
- 개발된 애플리케이션은 안드로이드에서 모두 동작해야 한다. <br>
- 모바일 애플리케이션의 경우, Android는 Kotlin 또는 Java로 구현해야 한다. <br>
- 데이터 저장은 클라우드 기반 데이터베이스와 스토리지(AWS EC2, AWS S3)를 사용해야 하며, 사용자 데이터를 안전하게 보호해야 한다. <br>

#### API 사용제한
- API 호출 횟수 제한이 존재한다.

#### 위치 기반 서비스의 제한
- 위치 추적 데이터가 정확하지 않을 수 있다. <br>
- 배터리 소모나 네트워크 사용량의 문제로 실시간 위치 업데이트가 제한될 수 있다. <br>

#### 보안 및 개인정보 보호
- 개인정보 보호법을 준수해야 한다. <br>
- API 키가 외부로 유출되지 않도록 안전하게 관리해야 한다. <br>

### 실제 구현 시 제약사항
#### AWS EC2 인스턴스의 사양 부족 문제
- 인스턴스 서버의 사양(CPU, RAM, 스토리지)이 부족해 데이터베이스와 서버 배포 시 문제가 발생했다. <br>
- 특히 데이터베이스에서 write 요청이 많이 쌓이면 처리 되지 않는 문제가 발생했다. <br>
- RAM 용량 문제 해결을 위해 스왑 메모리 설정하고, write 요청이 처리 되지 않는 문제를 해결하기 위해 MySQL 내 InnoDB Buffer Pool Size를 늘려 문제를 해결했다. <br>

#### Android Studio(IDEA) 캐시 최적화 문제
- 프로그램 내 캐시 버퍼가 초기화가 되지 않고 누적되는 문제가 발생해 주기적으로 캐시 버퍼를 비워주지 않으면 RAM 용량이 가득차는 문제가 발생했다. <br>
- 프로그램 내 gradle 캐시 사용 최소화 속성 추가, gradle 빌드 스크립트에서 Clean Task를 통해 자동 캐시 정리를 설정해 문제를 해결했다. <br>

### 미구현 항목
- 리뷰 싫어요 <br>
- 부적절한 리뷰어에게 경고 메세지 전송 <br>
- 신규 식당 정보 수정 제안 <br>
- 관리자 정보 수정 제안 조회 <br>
- 정보 수정 제안 문의 내역 확인 <br>
- 다른 사용자 즐겨찾기 목록 조회 <br>
- 실제 데이터 등록 부족 <br>

## High level Architecture 
![FinalArchitecture](https://github.com/user-attachments/assets/6b7fa5f8-0e59-4262-adb5-d88a30605232)
>이 아키텍처는 Layered 아키텍처와 마이크로서비스 아키텍처의 특징을 결합한 구조입니다.
>Layered 아키텍처의 안정성과 마이크로서비스 아키텍처의 유연성을 모두 반영하고자 하였습니다.
>
>먼저, 전체 시스템은 Data Layer, Business Layer, Presentation Layer의 3계층으로 나뉘어 Layered 아키텍처의 분리된 책임 원칙을 따릅니다.
>Data Layer는 Redis, MySQL DB, AWS S3 등의 저장소 내의 데이터를 관리하고,
>Business Layer는 사용자 인증, 리뷰, 검색 등 비즈니스 로직을 처리하며,
>Presentation Layer는 프론트엔드 화면과 사용자 경험을 제공합니다.
>또한, 각 서비스는 최대한 독립적인 기능 단위로 설계하여 결합도를 낮추고 확장성과 유지보수성을 높이고자 하였습니다.
>
>*여기서 서비스(Service)는 Controller-Service-Repository 구조의 Service가 아니라, 더 추상회된 개념입니다.


## Technology stacks
### Front-End
- **Language**: Kotlin, XML(UI)
- **Framework**: Android SDK 35 (Compile & Target SDK)
- **Development Tool**: Android Studio, Git
- **Colloboration Tool**: Notion, GitHub
- **UI:** Jetpack Compose, Glide, Google Material Design, XML Layout
- **API & NETWORKING:** Retrofit 2, Gson Converter, OkHttp
- **MAP & LOCATION:** Google Play Services Location, Naver Map SDK
- **Test & Debuging:** JUnit 4, Android Test Manifest
### Back-End
- **Language:** Java
- **Framework:** Spring boot(Spring Boot Starter Data JPA, Web, Mail, Cache)
- **Development Tool:** Intellij IDEA, Git
- **Collaboration Tool:** Notion, GitHub
- **Database:** MySQL
- **Cache:** Caffeine, Redis
- **Storage:** AWS S3
- **Deploy:** AWS EC2
- **Security:** Spring Security, JWT
- **API Document:** Springdoc OpenAPI
- **Code Simplification:** Lombok

## Installation guideline
0. Android 스마트폰 기준 <br>
1. /artifacts/UOSense.apk 다운로드 <br>
2. 출처를 알 수 없는 앱 설치를 위해 "설정" 버튼 클릭 <br>
![KakaoTalk_Photo_2024-12-14-18-48-56 001](https://github.com/user-attachments/assets/b8e81956-0490-49ad-8ba5-35aba41745ca) <br>
3. 출처를 알 수 없는 앱 설치 -> 권한 허용 <br>
![KakaoTalk_Photo_2024-12-14-18-48-57 002](https://github.com/user-attachments/assets/fe31e95b-3d9f-4acd-974b-6b9d528a792f) <br>
5. 출처를 알 수 없는 앱 설치 -> "무시하고 설치" 버튼 클릭 <br>
![KakaoTalk_Photo_2024-12-14-18-48-57 003](https://github.com/user-attachments/assets/63c4a1e0-f5c0-4901-bed4-b8cee8e3a7f4) <br>
6. 설치 중... <br>
![KakaoTalk_Photo_2024-12-14-18-48-57 004](https://github.com/user-attachments/assets/b049907a-e4eb-49e5-ad36-cc5fbca43614) <br>
7. 설치 완료 -> 앱 아이콘 클릭 해 앱 실행!! <br>
![KakaoTalk_Photo_2024-12-14-18-54-54](https://github.com/user-attachments/assets/502dc97a-19d9-4e8d-b04a-da5935cca970) <br>

### 테스트 계정 <br>
#### User 계정 <br>
- email: user@uos.ac.kr <br>
- password: User1234! <br>
#### Admin 계정 <br>
- email: admin@uos.ac.kr <br>
- password: Admin1234! <br>

## Project Deliverables
### - 요구사항 분석 명세서 final version: <<[관련 문서 링크](https://github.com/dokwon33/software-engineering_1/blob/main/artifacts/srs-template-v04.doc)>>
### - Architecture 및 Design Documents
* Software architecture & Design: <<[관련 문서 링크](https://github.com/dokwon33/software-engineering_1/blob/main/artifacts/High-level%20Architecture%20UML%20Diagrams%20Document_Final.pdf)>>
* UI Design: <<[관련 문서 링크](https://github.com/dokwon33/software-engineering_1/blob/main/artifacts/UI_Design_Document_v2.0.docx)>>

### - Coding Standard: <<[관련 문서 링크](https://github.com/dokwon33/software-engineering_1/blob/main/artifacts/Coding_Standard_Final.pdf)>>

#### Code: branch description
    1. Github에서 organization을 새로 판다
    2. repository를 프론트엔드는 Front-End, 백엔드는 Back-End로 각각 명명한다.
    3. 해당 repository에서 배포용 브랜치 이름은 main, 작업 내용을 병합할 개발용 브랜치는 dev로 명명한다.
    4. `Github`에서 이슈 생성, 번호 확인하고 `issue/#이슈번호`로 작업 브랜치를 생성한다.
        1. 이슈에 적은 목표를 달성한다.
        2. 로컬에서 테스트한다.
        3. 수정 사항을 commit하고 원격 저장소로 push한다.
        4. PR 날리고 Reviewer로 상대방 지정한다.
        5. 지정된 Reviewer는 코드를 보고 코멘트를 남긴다.
        6. 리뷰 과정을 거친 후 작업 브랜치를 dev 브랜치에 merge한다.
        7. issue를 닫고 작업 브랜치를 삭제한다.
    5. 4번을 반복해 수행한다. - 기능 구현 완료 시까지
    6. 배포는 각각의 repository를 clone해서 한다.
    7. 최종 완성본 코드는 교수님 계신  software-engineering_1 repository에 올린다.

  #### PR Strategy
   
    1. PR 제목 양식
        - [type] subject
            - 일반 커밋 제목은 type: subject하고 PR 제목은 [type] subject 이런 식으로 구분
        - 타입 종류
            - `feat` : 새로운 기능 추가
            - `fix` : 버그 수정
            - `docs` : 문서 수정
            - `style` : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
            - `refactor` : 코드 리펙토링
            - `test` : 테스트 코드, 리펙토링 테스트 코드 추가
            - `chore` : 빌드 업무 수정, 패키지 매니저 수정
        - e.g. feat: 관리자 페이지 기능 추가 **← 소문자로 시작**
    2.  PR 내용 양식
        
        ```jsx
        body // 한 일 요약, 왜 이렇게 했는지, 팀원에게 공유 필요한 정보 등등
        
        footer // 꼬리말 e.g. resolves, fixes
        ```
        
        - footer  `resolves: #이슈번호` (이슈 해결 했을 경우)
    3. Reviewer로 상대방 지정
    4. 지정된 Reviewer는 코드 확인
        - 기능 확인: 요구 사항 충족하는지
        - 코드 품질: 코드가 읽기 쉽고, 유지보수 가능한지
        - 버그 검출: 잠재적인 버그, 논리적 오류, 예외 처리 누락 여부
        - 스타일 준수: **Coding Standard**를 잘 준수했는지
        - 성능: 비효율적인 코드가 있는지
    5. 코멘트 작성
        - 특정 코드에 대한 코멘트를 원할 경우 + 클릭
    6. 수정 및 재검토
        - 리뷰어의 피드백에 따라 코드를 수정한 뒤, 다시 제출 → 4번(반복)
    7. 리뷰 과정을 거친 후 작업 브랜치를 base 브랜치에 merge
    8. PR을 닫고 작업 브랜치 삭제
#### 코드에 관한 documentation
- 아래 링크 참고
- API 명세서: https://eggplant-taurus-0ef.notion.site/API-159a62ef7a25801db690edd5a137df09?pvs=4
- Swagger UI: http://3.36.51.32:8080/swagger-ui/index.html#
> 보안 상의 이유로 .env 파일을 제거한 후 업로드하였기 때문에 업로드된 코드로는 실행이 불가합니다. (실행 시 에러)

### - 테스트 케이스 및 결과 final version: <<[관련 문서 링크](https://github.com/dokwon33/software-engineering_1/blob/main/artifacts/test_case_template.ver%203.0.xlsx)>>
### - Scrum 관련 자료 <<[관련 문서 링크](https://github.com/dokwon33/software-engineering_1/blob/main/artifacts/Scrum%20%EA%B4%80%EB%A0%A8%20%EB%AC%B8%EC%84%9C.docx)>>
- 회의록
- Sprint 일정표
- Product Backlog


## Repository Structure
### Front-End Repository ([링크](https://github.com/UOSense/Front-End))
 - UOSense-Frontend: 프로젝트 파일
```
/* UOSense-Frontend 내 주요 디렉토리 설명 */
src
 ┣ main
 ┃ ┣ java
 ┃ ┃ ┗ com
 ┃ ┃ ┃ ┗ example
 ┃ ┃ ┃ ┃ ┗ uosense
 ┃ ┃ ┃ ┃ ┃ ┣ adapters // RecyclerView 및 ListView와 같은 UI 구성 요소에 데이터 바인딩을 위한 어댑터 클래스 디렉토리
 ┃ ┃ ┃ ┃ ┃ ┣ data // 데이터 모델 및 저장소 관련 클래스 디렉토리
 ┃ ┃ ┃ ┃ ┃ ┣ fragments // 화면 단위 UI 조각 관리 클래스 디렉토리
 ┃ ┃ ┃ ┃ ┃ ┣ models // API 요청 및 응답을 위한 데이터 모델 클래스 디렉토리
 ┃ ┃ ┃ ┃ ┃ ┣ network // Retrofit API 서비스 인터페이스 및 네트워크 관리 디렉토리
 ┃ ┃ ┃ ┃ ┃ ┣ ui // UI 관리 및 화면 관련 클래스 디렉토리
 ┃ ┃ ┃ ┃ ┃ ┣ AppUtils.kt // 공통적으로 사용하는 앱 유틸리티 함수 모음
 ┃ ┃ ┃ ┃ ┃ ┣ ControlCreateActivity.kt // 관리자 식당 생성 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ ControlMainActivity.kt // 관리자 메인 화면 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ ControlRestaurantDetail.kt // 관리자 식당 상세 정보(수정) 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ ControlRestaurantListActivity.kt // 관리자 식당 목록 확인 및 관리 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ FavoriteListActivity.kt // 자신의 즐겨찾기 확인 액티비티 
 ┃ ┃ ┃ ┃ ┃ ┣ MainActivity.kt // 사용자 메인 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ MyPageActivity.kt // 마이페이지 (자신의 프로필 정보 확인 및 설정) 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ MyReviewActivity.kt // 자신의 리뷰 확인 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ ReportActivity.kt // 리뷰 신고 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ RestaurantDetailActivity.kt // 사용자 식당 상세 정보 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ RestaurantInfoSuggestionActivity.kt // 정보 제안 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ RestaurantListActivity.kt // 사용자 식당 목록 확인 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ ReviewListActivity.kt // 리뷰 전체 목록 확인 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ ReviewWriteActivity.kt // 리뷰 작성 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ SelectedDoorActivity.kt // 필터(정문, 쪽문, 남문, 후문)에 따라 식당 목록 확인 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ SignupActivity.kt // 회원가입 액티비티
 ┃ ┃ ┃ ┃ ┃ ┣ SignupCompleteActivity.kt // 회원가입 완료 액티비티
 ┃ ┃ ┃ ┃ ┃ ┗ StartActivity.kt // 앱 시작 시 첫 화면 액티비티
 ┃ ┣ res
 ┃ ┃ ┣ drawable // 이미지 리소스 등 그래픽 요소 포함 디렉토리
 ┃ ┃ ┣ layout // UI 레이아웃 XML 파일 포함 디렉토리
 ┃ ┃ ┣ mipmap-anydpi-v26 // 화면 해상도에 맞는 앱 아이콘 리소스 디렉토리
 ┃ ┃ ┣ mipmap-hdpi // 화면 해상도에 맞는 앱 아이콘 리소스 디렉토리
 ┃ ┃ ┣ mipmap-mdpi // 화면 해상도에 맞는 앱 아이콘 리소스 디렉토리
 ┃ ┃ ┣ mipmap-xhdpi // 화면 해상도에 맞는 앱 아이콘 리소스 디렉토리
 ┃ ┃ ┣ mipmap-xxhdpi // 화면 해상도에 맞는 앱 아이콘 리소스 디렉토리
 ┃ ┃ ┣ mipmap-xxxhdpi // 화면 해상도에 맞는 앱 아이콘 리소스 디렉토리
 ┃ ┃ ┣ values // 문자열 리소스, 색상, 스타일 등을 정의하는 XML 파일이 포함된 디렉토리
 ┃ ┃ ┗ xml // 앱 설정 파일이나 XML 형식의 사용자 정의 리소스가 포함된 디렉토리
 ┃ ┣ AndroidManifest.xml // 액티비티, 서비스, 권한 등 주요 애플리케이션 속성을 정의
```
### Back-End Repository ([링크](https://github.com/UOSense/Back-End))
 - UOSense-Backend: SpringBoot 프로젝트 파일   
```
/* UOSense-Backend 내 주요 디렉토리 설명 */
src // 프로젝트 소스코드
 ┣ main
 ┃ ┣ java
 ┃ ┃ ┗ UOSense
 ┃ ┃ ┃ ┗ UOSense_Backend
 ┃ ┃ ┃ ┃ ┣ common  // 범용성이 높은 클래스 모음
 ┃ ┃ ┃ ┃ ┃ ┣ converter  // 엔티티 속성과 DB 테이블 내 컬럼을 서로 변환해주는 컨버터 모음
 ┃ ┃ ┃ ┃ ┃ ┣ enumClass // enum 클래스 모음
 ┃ ┃ ┃ ┃ ┃ ┣ exception // 커스텀 예외 모음
 ┃ ┃ ┃ ┃ ┃ ┣ handler // 권한 인증 처리 핸들러 모음
 ┃ ┃ ┃ ┃ ┃ ┣ securityFilter // 보안 관련 필터 모음
 ┃ ┃ ┃ ┃ ┃ ┗ Utils  // 유틸리티 클래스 모음
 ┃ ┃ ┃ ┃ ┣ config  // 설정 파일 모음
 ┃ ┃ ┃ ┃ ┣ controller  // 컨트롤러 모음
 ┃ ┃ ┃ ┃ ┣ dto  // 데이터 전송 객체 모음
 ┃ ┃ ┃ ┃ ┣ entity  // 엔티티 모음
 ┃ ┃ ┃ ┃ ┣ repository // 리파지토리 모음
 ┃ ┃ ┃ ┃ ┣ service // 서비스 모음
 ┃ ┃ ┃ ┃ ┗ UoSenseBackendApplication.java // 어플리케이션
 ┃ ┗ resources  // 애플리케이션 환경 설정 파일 및 기타 리소스 파일
 ┃ ┃ ┗ application.properties // 환경변수
```

## Project Team Members
* 이도권: 조장, Front-End, 개발담당(FE)
* 김세윤: Scrum master, Front-End, 소통담당
* 김준호: Back-End, 개발담당(BE)
* 장규민: Front-End, 문서담당
* 최수아: Back-End, 디자인담당

