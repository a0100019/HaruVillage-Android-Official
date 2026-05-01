# 🏡 하루마을 (HaruVillage)

> 하루를 기록하고, 나만의 마을을 가꾸는 생활 습관 형성 안드로이드 앱

매일의 일상을 일기로 남기고, 그 활동을 통해 얻은 재화로 마을을 꾸며가는 게이미피케이션 기반 커뮤니티 앱입니다.
단순 기록을 넘어 **성취감**과 **따뜻한 소통**을 함께 제공하는 공간을 지향합니다.

---

## 📈 주요 성과

- 🏪 **Google Play Store 정식 출시 및 1년 9개월 운영**
- 📥 누적 다운로드 **1,400+**
- ⭐ 사용자 리뷰 **100+** / 평점 **4.9+**
- 🔄 사용자 피드백 기반 지속적 업데이트 진행 중

---

## 🎬 앱 구동 화면

<table>
  <tr>
    <td align="center"><img src="./assets/로그인.gif" width="200"/><br/>로그인</td>
    <td align="center"><img src="./assets/내정보.gif" width="200"/><br/>내정보</td>
    <td align="center"><img src="./assets/글씨체 변경.gif" width="200"/><br/>글씨체 변경</td>
  </tr>
  <tr>
    <td align="center"><img src="./assets/마을 꾸미기.gif" width="200"/><br/>마을 꾸미기</td>
    <td align="center"><img src="./assets/맵 도감.gif" width="200"/><br/>맵 도감</td>
    <td align="center"><img src="./assets/펫 도감.gif" width="200"/><br/>펫 도감</td>
  </tr>
  <tr>
    <td align="center"><img src="./assets/펫 뽑기.gif" width="200"/><br/>펫 뽑기</td>
    <td align="center"><img src="./assets/펫 키우기.gif" width="200"/><br/>펫 키우기</td>
    <td align="center"><img src="./assets/이웃마을.gif" width="200"/><br/>이웃마을</td>
  </tr>
  <tr>
    <td align="center"><img src="./assets/전체채팅.gif" width="200"/><br/>전체채팅</td>
    <td align="center"><img src="./assets/개인채팅.gif" width="200"/><br/>개인채팅</td>
    <td align="center"><img src="./assets/일기 달력.gif" width="200"/><br/>일기 달력</td>
  </tr>
  <tr>
    <td align="center"><img src="./assets/감정 필터.gif" width="200"/><br/>감정 필터</td>
    <td align="center"><img src="./assets/사진보관함.gif" width="200"/><br/>사진보관함</td>
    <td align="center"><img src="./assets/알림.gif" width="200"/><br/>알림</td>
  </tr>
  <tr>
    <td align="center"><img src="./assets/사자성어.gif" width="200"/><br/>사자성어</td>
    <td align="center"><img src="./assets/영어 단어.gif" width="200"/><br/>영어 단어</td>
    <td align="center"><img src="./assets/상식 퀴즈.gif" width="200"/><br/>상식 퀴즈</td>
  </tr>
  <tr>
    <td align="center"><img src="./assets/숫자게임.gif" width="200"/><br/>숫자게임</td>
    <td align="center"><img src="./assets/스도쿠.gif" width="200"/><br/>스도쿠</td>
    <td align="center"><img src="./assets/컬링.gif" width="200"/><br/>컬링</td>
  </tr>
</table>

---

## 🚀 핵심 기능

### 📓 스마트 다이어리
- 감정 선택, 사진 첨부, 자유 텍스트 기록
- **자동 저장**으로 앱 종료 시에도 데이터 유실 없음
- 캘린더 뷰로 과거 일기 탐색 및 검색
- 알림 예약으로 꾸준한 기록 습관 형성

### 🏘️ 마을 꾸미기 & 게이미피케이션
- 일기 작성 · 걷기 등 활동으로 인앱 재화 획득
- 재화로 배경, 건물, 장식 아이템 구매
- 펫 수집 및 육성 시스템
- 3종 미니게임으로 추가 보상 획득
- 업적 메달 시스템으로 장기 리텐션 유도

### 🚶 건강 관리
- 걸음 수 실시간 측정 (Samsung Health Connect 연동)
- Foreground Service로 백그라운드에서도 지속 추적
- 주/월 단위 활동 통계 차트 제공

### 👥 커뮤니티 & 소셜
- 사진 기반 게시판으로 일상 공유
- 좋아요 · 댓글 · 1:1 채팅 · 이웃 기능
- Firestore 실시간 피드 동기화

### 📚 학습 콘텐츠
- 영어 단어 · 한국어 속담 · 스도쿠 · 상식 문제
- 학습 완료 시 인앱 보상 지급

### 🔐 계정 관리
- 게스트(익명) 로그인으로 즉시 시작
- Google 소셜 로그인 지원
- 게스트 → 정식 계정 업그레이드 가능

---

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| **언어** | Kotlin |
| **UI** | Jetpack Compose, Material 3, Lottie |
| **아키텍처** | Clean Architecture, MVVM + MVI (Orbit) |
| **비동기** | Kotlin Coroutines, Flow |
| **로컬 DB** | Room Database (14 Entity) |
| **백엔드** | Firebase (Auth / Firestore / Storage / Analytics) |
| **네트워크** | Retrofit 2, OkHttp |
| **DI** | Dagger Hilt |
| **백그라운드** | WorkManager, ForegroundService, AlarmManager |
| **헬스** | Samsung Health Connect |
| **AI** | Google Generative AI SDK |
| **이미지** | Coil, Firebase Storage |
| **차트** | YCharts |

---

## 🏗️ 아키텍처

![앱 구조도](./assets/diagram.svg)

```
Presentation Layer
├── Jetpack Compose (UI)
└── ViewModel (MVVM + MVI via Orbit)
    └── Event → Intent → State → UI (단방향 데이터 흐름)

Data Layer
├── Room Database (로컬 캐싱, 오프라인 지원)
│   └── 14 Entity: User, Diary, Walk, Item, World, Pat,
│                   Photo, English, KoreanIdiom, Letter,
│                   AllUser, Area, Knowledge, Sudoku
├── Firebase (Auth, Firestore, Storage)
└── Retrofit (REST API)

DI: Dagger Hilt
Background: WorkManager / ForegroundService / AlarmManager
```

**오프라인 우선 설계**: Room에 먼저 저장 후 Firestore로 동기화 → 네트워크 없이도 정상 동작, 연결 복구 시 자동 업로드

---

## 🔥 기술적 고민 & 해결

### 1. 다이어리 작성 중 데이터 유실 방지
- **문제**: 작성 완료 전 앱이 종료되면 내용이 사라지는 UX 이슈
- **해결**: Flow collector로 입력 이벤트를 구독하여 Room에 즉시 로컬 저장 → 완료 시 Firestore 업로드하는 2단계 저장 구조로 변경

### 2. Firebase의 복잡한 쿼리 한계
- **문제**: 랭킹, 통계 등 복잡한 집계 쿼리를 Firestore에서 처리하기 어려움
- **해결**: Spring Boot + MySQL 기반 API 서버로 단계적 마이그레이션 진행 중 (MSA 구조 학습 및 적용)

### 3. DB 스키마 변경 시 사용자 데이터 유실 위험
- **문제**: 앱 업데이트로 Room 스키마가 변경될 때 기존 설치 사용자 데이터 파괴 가능성
- **해결**: Room Migration 스크립트(v2 → v3 → v4) 작성으로 기존 데이터를 보존하면서 무중단 업그레이드

### 4. 백그라운드 걸음 수 추적 서비스 종료 문제
- **문제**: Android OS의 배터리 최적화로 백그라운드 서비스가 강제 종료됨
- **해결**: ForegroundService + 알림 채널 등록으로 시스템 종료 방지, BootReceiver로 기기 재시작 시 자동 복구

---

## 💻 설치 및 실행

```bash
git clone https://github.com/a0100019/HaruVillage-Android-Official.git
```

1. Android Studio에서 프로젝트 열기
2. Firebase 프로젝트 생성 후 `google-services.json` 추가
3. `local.properties`에 필요한 API 키 설정
4. Run ▶️

---

## 🔗 링크

- [Google Play Store](https://play.google.com/store/apps/details?id=com.a0100019.mypat)
