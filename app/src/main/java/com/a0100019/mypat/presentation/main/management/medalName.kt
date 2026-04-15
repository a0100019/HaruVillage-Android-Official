package com.a0100019.mypat.presentation.main.management

fun medalName(type: Int): String =
    when (type) {
        0 -> "없음"
        1 -> "신입 관리인"
        //칭호 설명
        2 -> "팻 사랑꾼"
        3 -> "최애 팻"
        4 -> "완벽한 집사"
        5 -> "사랑이 넘치는"
        6 -> "한자 덕후"
        7 -> "다국어 꿈나무"
        8 -> "개근상"
        9 -> "역사의 시작"
        10 -> "장난꾸러기"
        11 -> "너무 좋아요"
        12 -> "마을의 활력소"
        13 -> "공감 요정"
        14 -> "마을의 목소리"
        15 -> "명상가"
        16 -> "애국자"
        17 -> "푸바오"
        18 -> "모범생"
        19 -> "꼼꼬미"
        20 -> "마당발"
        21 -> "단짝"
        22 -> "꾸준한 관리인"
        23 -> "하루마을 응원단"
        24 -> "나를 따르라!"
        25 -> "수문장"
        26 -> "하루마을 공헌자"
        27 -> "황금 열쇠"
        28 -> "혼자 놀기 장인"
        29 -> "후원자"
        30 -> "첫 발걸음"
        31 -> "연필을 든 순간"
        else -> "알 수 없음"
    }

//20 -> "하루마을 후원자"
//21 -> "하루마을 공헌자"

fun medalExplain(type: Int): String =
    when (type) {
        0 -> "없음"
        1 -> "하루마을에 처음으로 들어온 관리인에게 부여하는 칭호"
        //칭호 설명
        2 -> "펫 레벨 50 달성"
        3 -> "펫 레벨 100 달성"
        4 -> "펫과 게임 100회 진행"
        5 -> "일기 감정 중 사랑이 10개 이상"
        6 -> "사자 성어 별 10개 이상"
        7 -> "영어 문제를 푼 횟수가 50개 이상"
        8 -> "10일 연속 출석"
        9 -> "도감 첫 구경하기"
        10 -> "아이템을 다 모은 후 아이템 구매 클릭"
        11 -> "좋아요 50개 누르기"
        12 -> "게시글 1개 작성"
        13 -> "게시글 댓글 10개 작성"
        14 -> "채팅 20개 작성"
        15 -> "도감-펫에서 명상 나무늘보 10번 들어가기"
        16 -> "도감-아이템에서 대한민국 국기 10번 들어가기"
        17 -> "대나무숲 1회 작성"
        18 -> "설정에서 설명서 3번 읽기"
        19 -> "설정에서 개인정보 처리방침 끝까지 읽기"
        20 -> "친구 수 10명"
        21 -> "친구와 채팅 100개"
        22 -> "하루마을 50일 출석"
        23 -> "설정에서 리뷰 작성하기"
        24 -> "친구와 보스 잡기 첫 공격"
        25 -> "친구와 보스 잡기 누적 점수 100점 달성"
        26 -> "하루마을의 발전에 큰 도움을 준 사람에게 부여하는 칭호"
        27 -> "힌트 15번 보기"
        28 -> "나에게 친구 요청하기"
        29 -> "광고 제거 구매하기"
        30 -> "영어 튜토리얼 통과"
        31 -> "첫 상식 문제를 완료할 경우 받는 칭호"
        else -> "알 수 없음"
    }

fun addMedalAction(medalData: String, actionId: Int): String {
    // 기본값 처리
    if (medalData == "0") {
        return "$actionId/1"
    }

    val map = medalData
        .split("@")
        .mapNotNull {
            val parts = it.split("/")
            if (parts.size == 2) {
                parts[0].toIntOrNull()?.let { id ->
                    id to (parts[1].toIntOrNull() ?: 0)
                }
            } else null
        }
        .toMap()
        .toMutableMap()

    // 카운트 증가
    map[actionId] = (map[actionId] ?: 0) + 1

    // 다시 String으로 변환
    return map.entries.joinToString("@") { "${it.key}/${it.value}" }
}

fun getMedalActionCount(medalData: String, actionId: Int): Int {
    if (medalData == "0" || medalData.isBlank()) return 0

    return medalData
        .split("@")
        .firstOrNull {
            it.startsWith("$actionId/")
        }
        ?.substringAfter("/")
        ?.toIntOrNull()
        ?: 0
}

fun totalMedalCount(): Int {
    return (1..500) // 충분히 큰 범위
        .count { medalName(it) != "알 수 없음" }
}

/**
 * 메달을 아직 보유하지 않았다면 추가하고 true 반환.
 * 이미 보유 중이면 false 반환.
 *
 * @param currentMedals  "1/3/5" 형태의 슬래시 구분 문자열
 * @param medalId        획득할 메달 ID
 * @return Pair(갱신된 문자열, 획득 여부)
 */
fun tryAcquireMedal(currentMedals: String, medalId: Int): Pair<String, Boolean> {
    val medalList = currentMedals
        .split("/")
        .mapNotNull { it.toIntOrNull() }
        .toMutableList()

    if (medalList.contains(medalId)) return currentMedals to false

    medalList.add(medalId)
    return medalList.joinToString("/") to true
}
