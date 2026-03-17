package com.a0100019.mypat.presentation.main.management.loading

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.image.etc.BackGroundImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect


@Composable
fun LoadingContainerScreen(
    loadingViewModel: LoadingViewModel = hiltViewModel(),

    popBackStack: () -> Unit = {},

    ) {

    val loadingState : LoadingState = loadingViewModel.collectAsState().value

    val context = LocalContext.current

    loadingViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is LoadingSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
        }
    }

    LoadingScreen(
        situation = loadingState.situation,

        onClose = loadingViewModel::onClose,
        onSituationChangeClick = loadingViewModel::onSituationChange,
        popBackStack = popBackStack
    )
}

@Composable
fun LoadingScreen(
    situation: String = "",

    onClose : () -> Unit = {},
    popBackStack: () -> Unit = {},
    onSituationChangeClick: (String) -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {

        BackGroundImage()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row {
                MainButton(
                    onClick = popBackStack,
                    text = "닫기"
                )
            }

        }

    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    MypatTheme {
        LoadingScreen(
            situation = ""
        )
    }
}