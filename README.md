# Project Title: 시대생 맛집 지도

## Project Scope
서울시립대학교 컴퓨터과학부 2024년 소프트웨어공학
프로젝트로, Software Development Life-Cycle 을 기반으로 객체지향
소프트웨어공학 방법론을 적용하여 서울시립대 주변 맛집 탐색 지도 플랫폼 개발

## Project Duration
2024년 2학기

## Highlighted Features
* 검색어 기반 식당 검색
* 지도 기반 식당 검색
* 식당 정보 열람
* 리뷰 작성 및 상호작용
* 즐겨찾기
* 정보 수정 제안

* DEMO VIDEO

## Project Constraints

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
### Tech Stack
\-Language: Kotlin, Java <br>
\-Framework: Spring boot <br>
\-Development Tool: Android Studio, IntelliJ, Git <br>
\-Collaboration Tool: Notion, GitHub <br>
\-Database: MySQL, Redis <br>
\-Storage: AWS S3 <br>
\-Deploy: AWS EC2 <br>

### 라이브러리(의존성)
* Front-End
* Back-End

## Installation guideline
APK파일과 함께 다운로드 방법

## Project Deliverables
### \-요구사항 분석 명세서 final version: <<링크>>
### \-Architecture 및 Design Documents
* Software architecture & Design: <<[관련 문서 링크](https://github.com/dokwon33/software-engineering_1/blob/main/artifacts/High-level%20Architecture%20UML%20Diagrams%20Document_Final.docx.pdf)>>
* UI Design: <<관련 문서 링크>>

### Coding Standard: <<[관련 문서 링크](https://github.com/dokwon33/software-engineering_1/blob/main/artifacts/Coding_Standard_Final.docx.pdf)>>

### Code: branch description
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

  ### PR 과정
   
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
### 코드에 관한 documentation
- API 명세서: https://eggplant-taurus-0ef.notion.site/API-159a62ef7a25801db690edd5a137df09?pvs=4)
- Swagger UI: http://3.36.51.32:8080/swagger-ui/index.html#

### \-테스트 케이스 및 결과

## Repository Structure
### Front-End Repository
### Back-End Repository
 - UOSense-Backend: SpringBoot 프로젝트 파일   
```
/* UOSense-Backend 내 주요 디렉토리 설명 */
src // 프로젝트 소스코드
 ┣ main
 ┃ ┣ java
 ┃ ┃ ┗ UOSense
 ┃ ┃ ┃ ┗ UOSense_Backend
 ┃ ┃ ┃ ┃ ┣ common  // 유용성이 
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
 ┃ ┗ resources  // 
 ┃ ┃ ┗ application.properties // 환경변수
```

## Project Team Members
* 이도권: 조장, Front-End, 개발담당(FE)
* 김세윤: Scrum master, Front-End, 소통담당
* 김준호: Back-End, 개발담당(BE)
* 장규민: Front-End, 문서담당
* 최수아: Back-End, 디자인담당

