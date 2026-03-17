package com.a0100019.mypat.presentation.main.management

sealed class MainRoute(
    val name:String
) {

    object MainScreen : MainRoute("MainScreen")

    object LoginScreen : MainRoute("LoginScreen")

    object DailyScreen : MainRoute("DailyScreen")

    object StoreScreen : MainRoute("StoreScreen")

    object IndexScreen : MainRoute("IndexScreen")

    object WorldScreen : MainRoute("WorldScreen")

    object SettingScreen : MainRoute("SettingScreen")

    object InformationScreen : MainRoute("InformationScreen")

    object FirstGameScreen : MainRoute("FirstGameScreen")
    object SecondGameScreen : MainRoute("SecondGameScreen")
    object ThirdGameScreen : MainRoute("ThirdGameScreen")

    object DiaryScreen : MainRoute("DiaryScreen")
    object DiaryWriteScreen : MainRoute("DiaryWriteScreen")

    object EnglishScreen : MainRoute("EnglishScreen")

    object KoreanScreen : MainRoute("KoreanScreen")

    object WalkScreen : MainRoute("WalkScreen")

    object OperatorScreen : MainRoute("OperatorScreen")

    object PrivateRoomScreen : MainRoute("PrivateRoomScreen")
    object PrivateChatInScreen : MainRoute("PrivateChatInScreen")

    object NeighborScreen : MainRoute("NeighborScreen")

    object BoardScreen : MainRoute("BoardScreen")
    object BoardMessageScreen : MainRoute("BoardMessageScreen")
    object ChatScreen : MainRoute("ChatScreen")
    object CommunityScreen : MainRoute("CommunityScreen")

    object NeighborInformationScreen : MainRoute("NeighborInformationScreen")

    object PrivateChatGameScreen : MainRoute("PrivateChatGameScreen")

    object KnowledgeScreen : MainRoute("KnowledgeScreen")

    object ActivityScreen : MainRoute("ActivityScreen")

    object FirstScreen : MainRoute("FirstScreen")

}



