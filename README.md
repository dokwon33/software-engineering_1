# Project Title: 시대생 맛집 지도

## Project Scope
서울 시립대학교 컴퓨터과학부 2024년 소프트웨어공학
프로젝트로, Software Development Life-Cycle 을 기반으로 객체지향
소프트웨어공학 방법론을 적용하여 << >> 개발

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
* Software architecture: <<관련 문서 링크>>
* Software Design: <<관련 문서 링크>>
* UI Design: <<관련 문서 링크>>

### Coding Standard: <<관련 문서 링크>>

### \-Code: branch description 과 코드에 관한 documentation 설명


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

