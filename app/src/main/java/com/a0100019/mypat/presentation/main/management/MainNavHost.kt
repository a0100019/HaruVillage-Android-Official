package com.a0100019.mypat.presentation.main.management

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.a0100019.mypat.presentation.activity.ActivityContainerScreen
import com.a0100019.mypat.presentation.activity.daily.DailyScreen
import com.a0100019.mypat.presentation.activity.daily.english.EnglishScreen
import com.a0100019.mypat.presentation.activity.daily.knowledge.KnowledgeScreen
import com.a0100019.mypat.presentation.activity.daily.korean.KoreanScreen
import com.a0100019.mypat.presentation.activity.daily.walk.WalkScreen
import com.a0100019.mypat.presentation.neighbor.chat.ChatScreen
import com.a0100019.mypat.presentation.neighbor.community.CommunityScreen
import com.a0100019.mypat.presentation.diary.DiaryScreen
import com.a0100019.mypat.presentation.diary.DiaryWriteScreen
import com.a0100019.mypat.presentation.game.firstGame.FirstGameScreen
import com.a0100019.mypat.presentation.game.secondGame.SecondGameScreen
import com.a0100019.mypat.presentation.game.thirdGame.ThirdGameScreen
import com.a0100019.mypat.presentation.activity.index.IndexScreen
import com.a0100019.mypat.presentation.activity.information.InformationScreen
import com.a0100019.mypat.presentation.login.LoginScreen
import com.a0100019.mypat.presentation.main.MainScreen
import com.a0100019.mypat.presentation.setting.SettingScreen
import com.a0100019.mypat.presentation.activity.store.StoreScreen
import com.a0100019.mypat.presentation.main.world.WorldScreen
import com.a0100019.mypat.presentation.neighbor.NeighborInformationScreen
import com.a0100019.mypat.presentation.neighbor.NeighborScreen
import com.a0100019.mypat.presentation.neighbor.board.BoardMessageScreen
import com.a0100019.mypat.presentation.neighbor.board.BoardScreen
import com.a0100019.mypat.presentation.main.operator.OperatorScreen
import com.a0100019.mypat.presentation.neighbor.privateChat.PrivateChatGameScreen
import com.a0100019.mypat.presentation.neighbor.privateChat.PrivateChatInScreen
import com.a0100019.mypat.presentation.neighbor.privateChat.PrivateRoomScreen
import com.a0100019.mypat.presentation.activity.store.BillingManager
import com.a0100019.mypat.presentation.main.first.FirstContainerScreen

@Composable
fun MainNavHost(
    billingManager: BillingManager
) {
    val navController = rememberNavController()
    //뷰모델 공유하고 싶으면 이렇게 하기
    // val diaryViewModel: DiaryViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = MainRoute.LoginScreen.name,
    ){
        composable(route = MainRoute.MainScreen.name) {
            MainScreen(
                onDailyNavigateClick = {
                    navController.navigate(route = MainRoute.DailyScreen.name)
                },
                onStoreNavigateClick = {
                    navController.navigate(route = MainRoute.StoreScreen.name)
                },
                onIndexNavigateClick = {
                    navController.navigate(route = MainRoute.IndexScreen.name)
                },
                onWorldNavigateClick = {
                    navController.navigate(route = MainRoute.WorldScreen.name)
                },
                onInformationNavigateClick = {
                    navController.navigate(route = MainRoute.InformationScreen.name)
                },
                onSettingNavigateClick = {
                    navController.navigate(route = MainRoute.SettingScreen.name)
                },
                onFirstGameNavigateClick = {
                    navController.navigate(route = MainRoute.FirstGameScreen.name)
                },
                onSecondGameNavigateClick = {
                    navController.navigate(route = MainRoute.SecondGameScreen.name)
                },
                onThirdGameNavigateClick = {
                    navController.navigate(route = MainRoute.ThirdGameScreen.name)
                },
                onNeighborNavigateClick = {
                    navController.navigate(route = MainRoute.NeighborScreen.name)
                },
                onOperatorNavigateClick = {
                    navController.navigate(route = MainRoute.OperatorScreen.name)
                },
                onPrivateRoomNavigateClick = {
                    navController.navigate(route = MainRoute.PrivateRoomScreen.name)
                },
                onDiaryNavigateClick = {
                    navController.navigate(route = MainRoute.DiaryScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                }
                ,
                onActivityNavigateClick = {
                    navController.navigate(route = MainRoute.ActivityScreen.name)
                },
                onNavigateToFirstScreen = {
                    navController.navigate(route = MainRoute.FirstScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                }
                ,

            )
        }

        composable(route = MainRoute.LoginScreen.name) {
            LoginScreen(navController = navController)
        }

        composable(route = MainRoute.DailyScreen.name) {
            DailyScreen(
                onDiaryNavigateClick = {
                    navController.navigate(route = MainRoute.DiaryScreen.name)
                },
                onEnglishNavigateClick = {
                    navController.navigate(route = MainRoute.EnglishScreen.name)
                },
                onKoreanNavigateClick = {
                    navController.navigate(route = MainRoute.KoreanScreen.name)
                },
                onWalkNavigateClick = {
                    navController.navigate(route = MainRoute.WalkScreen.name)
                },
                onKnowledgeNavigateClick = {
                    navController.navigate(route = MainRoute.KnowledgeScreen.name)
                },
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.WorldScreen.name) {
            WorldScreen(
                onMainNavigateClick = {
                    navController.navigate(route = MainRoute.MainScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
                popBackStack = { navController.popBackStack() },
            )
        }

        composable(route = MainRoute.StoreScreen.name) {
            StoreScreen(
                popBackStack = { navController.popBackStack() },
                billingManager = billingManager
            )
        }

        composable(route = MainRoute.IndexScreen.name) {
            IndexScreen(
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.InformationScreen.name) {
            InformationScreen(
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.CommunityScreen.name) {
            CommunityScreen(
                popBackStack = { navController.popBackStack() },
                onNavigateToNeighborInformationScreen = {
                    navController.navigate(route = MainRoute.NeighborInformationScreen.name)
                }
            )
        }

        composable(route = MainRoute.FirstGameScreen.name) {
            FirstGameScreen(
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.SecondGameScreen.name) {
            SecondGameScreen(
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.ThirdGameScreen.name) {
            ThirdGameScreen(
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.DiaryScreen.name) {
            DiaryScreen(
                onDiaryClick = {
                    navController.navigate(route = MainRoute.DiaryWriteScreen.name)
                },
                onNavigateToMainScreen = {
                    navController.navigate(route = MainRoute.MainScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
                popBackStack = { navController.popBackStack() },
                onNavigateToSettingScreen = {
                    navController.navigate(route = MainRoute.SettingScreen.name)
                },
                onNavigateToFirstScreen = {
                    navController.navigate(route = MainRoute.FirstScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
            )
        }

        composable(route = MainRoute.DiaryWriteScreen.name) {
            // 같은 ViewModel 사용
            DiaryWriteScreen(
                //()가 있는 함수는 {} 안에 해야함
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.EnglishScreen.name) {
            EnglishScreen(
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.KoreanScreen.name) {
            KoreanScreen(
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.SettingScreen.name) {
            SettingScreen(
                onSignOutClick = {
                    navController.navigate(route = MainRoute.LoginScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.ChatScreen.name) {
            ChatScreen(
                popBackStack = { navController.popBackStack() },
                onNavigateToNeighborInformationScreen = {
                    navController.navigate(route = MainRoute.NeighborInformationScreen.name)
                }
            )
        }

        composable(route = MainRoute.NeighborInformationScreen.name) {
            NeighborInformationScreen(
                popBackStack = { navController.popBackStack() },
                onNavigateToPrivateRoomScreen = {
                    navController.navigate(route = MainRoute.PrivateRoomScreen.name)
                },
            )
        }

        composable(route = MainRoute.WalkScreen.name) {
            WalkScreen(
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.OperatorScreen.name) {
            OperatorScreen(
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.PrivateRoomScreen.name) {
            PrivateRoomScreen(
                popBackStack = { navController.popBackStack() },
                onNavigateToPrivateChatInScreen = {
                    navController.navigate(route = MainRoute.PrivateChatInScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
                onNavigateToMainScreen = {
                    navController.navigate(route = MainRoute.MainScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
                onNavigateToNeighborInformationScreen = {
                    navController.navigate(route = MainRoute.NeighborInformationScreen.name)
                },
            )
        }

        composable(route = MainRoute.PrivateChatInScreen.name) {
            PrivateChatInScreen(
                popBackStack = { navController.popBackStack() },
                onNavigateToPrivateRoomScreen = {
                    navController.navigate(route = MainRoute.PrivateRoomScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
                onNavigateToNeighborInformationScreen = {
                    navController.navigate(route = MainRoute.NeighborInformationScreen.name)
                },
                onNavigateToPrivateChatGameScreen = {
                    navController.navigate(route = MainRoute.PrivateChatGameScreen.name)
                },
            )
        }

        composable(route = MainRoute.NeighborScreen.name) {
            NeighborScreen(
                popBackStack = { navController.popBackStack() },
                onChatNavigateClick = {
                    navController.navigate(route = MainRoute.ChatScreen.name)
                },
                onCommunityNavigateClick = {
                    navController.navigate(route = MainRoute.CommunityScreen.name)
                },
                onBoardNavigateClick = {
                    navController.navigate(route = MainRoute.BoardScreen.name)
                },
                onPrivateRoomNavigateClick = {
                    navController.navigate(route = MainRoute.PrivateRoomScreen.name)
                },
                onMainNavigateClick = {
                    navController.navigate(route = MainRoute.MainScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
            )
        }

        composable(route = MainRoute.BoardScreen.name) {
            BoardScreen(
                popBackStack = { navController.popBackStack() },
                onNavigateToBoardMessageScreen = {
                    navController.navigate(route = MainRoute.BoardMessageScreen.name)
                },
                onNavigateToMainScreen = {
                    navController.navigate(route = MainRoute.MainScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
                onNavigateToNeighborScreen = {
                    navController.navigate(route = MainRoute.NeighborScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
                onNavigateToFirstScreen = {
                    navController.navigate(route = MainRoute.FirstScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
            )
        }

        composable(route = MainRoute.BoardMessageScreen.name) {
            BoardMessageScreen(
                popBackStack = { navController.popBackStack() },
                onNavigateToBoardScreen = {
                    navController.navigate(route = MainRoute.BoardScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
                onNavigateToNeighborInformationScreen = {
                    navController.navigate(route = MainRoute.NeighborInformationScreen.name)
                },
            )
        }

        composable(route = MainRoute.PrivateChatGameScreen.name) {
            PrivateChatGameScreen(
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.KnowledgeScreen.name) {
            KnowledgeScreen(
                popBackStack = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.FirstScreen.name) {
            FirstContainerScreen(
                popBackStack = { navController.popBackStack() },
                onMainNavigateClick = {
                    navController.navigate(route = MainRoute.MainScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
                onDiaryNavigateClick = {
                    navController.navigate(route = MainRoute.DiaryScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
                onBoardNavigateClick = {
                    navController.navigate(route = MainRoute.BoardScreen.name) {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true // 같은 화면 여러 번 안 쌓이게
                    }
                },
                onSettingNavigateClick = {
                    navController.navigate(route = MainRoute.SettingScreen.name)
                },
            )
        }

        composable(route = MainRoute.ActivityScreen.name) {
            ActivityContainerScreen(
                popBackStack = { navController.popBackStack() },
                onDailyNavigateClick = {
                    navController.navigate(route = MainRoute.DailyScreen.name)
                },
                onIndexNavigateClick = {
                    navController.navigate(route = MainRoute.IndexScreen.name)
                },
                onInformationNavigateClick = {
                    navController.navigate(route = MainRoute.InformationScreen.name)
                },
                onStoreNavigateClick = {
                    navController.navigate(route = MainRoute.StoreScreen.name)
                },
                onWorldNavigateClick = {
                    navController.navigate(route = MainRoute.WorldScreen.name)
                },
            )
        }

//
//        composable(route = LoginRoute.SignUpScreen.name) {
//
//            SignUpScreen(
//                onNavigationToLoginScreen = {
//                    navController.navigate(
//                        route = LoginRoute.LoginScreen.name,
//                        navOptions = navOptions {
//                            //웰컴화면 이후 화면들을 제거, 만약 inclusive = ture까지하면 웰컴도 제거
//                            popUpTo(LoginRoute.WelcomeScreen.name)
//                        }
//                    )
//                }
//            )
//
//        }

    }
}