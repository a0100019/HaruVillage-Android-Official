🏡 하루마을 (HaruMaul)
지친 일상을 기록하고, 나만의 힐링 마을을 가꾸는 안드로이드 커뮤니티 서비스

단순한 기록을 넘어, 사용자가 앱 내 활동을 통해 성취감을 느끼고 타인과 따뜻한 소통을 나눌 수 있는 공간을 지향합니다.

📈 주요 성과
실제 사용자 1,000명 이상 달성 및 운영

구글 플레이 스토어 정식 출시

사용자 피드백 기반 UI/UX 개선 (총 X회 업데이트)

🚀 Key Features (핵심 기능)
1. 사진 기반 힐링 게시판 📸
소통의 장: 사용자가 사진과 글을 올려 일상을 공유하고, 댓글과 좋아요를 통해 타인과 소통할 수 있는 커뮤니티 기능을 구현했습니다.

기술적 구현: Firebase Storage와 Firestore를 연동하여 이미지 업로드 및 실시간 데이터 업데이트를 처리했습니다.

2. 자동 저장 스마트 다이어리 ✍️
편의성 극대화: 일기 작성 시 별도의 저장 버튼 없이도 작성 중인 내용이 실시간으로 안전하게 저장됩니다.

기술적 도전: 작성 중 데이터 유실을 방지하기 위해 Coroutines와 Firebase의 실시간 동기화 로직을 최적화했습니다.

3. 마을 꾸미기 및 재화 시스템 🏠
게이미피케이션: 앱 내 활동(일기 작성, 소통 등)을 통해 획득한 화폐로 아이템을 구매하고 나만의 마을을 꾸미는 기능을 제공하여 앱 리텐션을 높였습니다.

성취감 부여: 사용자가 자신의 기록이 마을의 성장으로 이어지는 시각적 경험을 제공합니다.

🛠 Tech Stack (기술 스택)
Android
Language: Kotlin

UI: Jetpack Compose (현대적인 선언형 UI 구현)

Architecture: MVVM (관심사 분리를 통한 유지보수성 향상)

Asynchronous: Coroutines

Backend (Infrastructure)
Current: Firebase (Auth, Firestore, Storage)

In Progress 🚧: Spring Boot & MySQL 마이그레이션

서비스 확장성과 데이터 복잡도 해결을 위해 기존 Firebase 기능을 Kotlin 기반 Spring Boot API 서버로 이전 중입니다.

🔥 기술적 성장과 고민 (Troubleshooting)
[고민 1] 사용자 1,000명의 데이터를 어떻게 효율적으로 관리할 것인가?
문제: 사용자가 늘어남에 따라 Firebase의 NoSQL 구조에서 복잡한 랭킹이나 통계 쿼리를 처리할 때 비용과 속도 이슈가 예상되었습니다.

해결: 이를 계기로 관계형 데이터베이스(RDB)의 필요성을 느껴 Spring Boot와 MySQL을 도입하기로 결정했습니다. 현재 특정 기능을 API 서버로 분리하는 MSA(Microservice Architecture) 구조를 학습하며 적용 중입니다.

💻 설치 및 실행 방법
저장소를 클론합니다. git clone https://github.com/유빈님아이디/HaruMaul.git

Android Studio에서 프로젝트를 엽니다.

구글 서비스 설정을 완료한 후 실행합니다.
