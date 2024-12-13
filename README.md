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
각각의 디렉토리에 대한 설명

## Project Team Members
* 이도권: 조장, Front-End, 개발담당(FE)
* 김세윤: Scrum master, Front-End, 소통담당
* 김준호: Back-End, 개발담당(BE)
* 장규민: Front-End, 문서담당
* 최수아: Back-End, 디자인담당

