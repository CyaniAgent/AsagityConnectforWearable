package com.asagity.connectwearable.presentation.welcome

import android.os.SystemClock
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CircularProgressIndicatorDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.asagity.connectwearable.R
import com.asagity.connectwearable.presentation.debug.DebugOptionsScreen
import com.asagity.connectwearable.presentation.find.DEBUG_DEVICE_NAME
import com.asagity.connectwearable.presentation.find.FindReceiveScreen
import com.asagity.connectwearable.presentation.find.FindSendScreen
import com.asagity.connectwearable.presentation.theme.AsagityConnectForWearableTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/** Post-welcome destinations. Waiting stays default until the real
 * phone connection state arrives via the Wear Data Layer. */
private enum class FindEntryPoint {
    Waiting,
    Debug,
    FindSend,
    FindReceive
}

private const val WELCOME_DURATION_MILLIS = 2000L

/** Taps required on the waiting text before a long-press unlocks debug options. */
private const val DEBUG_UNLOCK_TAPS = 5

/** Press-and-hold duration that confirms debug unlock once taps are armed. */
private const val DEBUG_UNLOCK_HOLD_MILLIS = 1000L

/** Max gap between consecutive taps before the tap counter resets. */
private const val DEBUG_TAP_WINDOW_MILLIS = 3000L

@Composable
fun WearApp() {
    var showWelcome by remember { mutableStateOf(true) }
    var screen by remember { mutableStateOf(FindEntryPoint.Waiting) }

    LaunchedEffect(Unit) {
        delay(WELCOME_DURATION_MILLIS.milliseconds)
        showWelcome = false
    }

    AsagityConnectForWearableTheme {
        AppScaffold {
            Crossfade(
                targetState = showWelcome,
                label = "welcome_to_waiting"
            ) { isWelcome ->
                if (isWelcome) {
                    WelcomeScreen()
                } else {
                    when (screen) {
                        FindEntryPoint.Waiting -> WaitingForConnectionScreen(
                            onDebugUnlock = { screen = FindEntryPoint.Debug }
                        )
                        FindEntryPoint.Debug -> DebugOptionsScreen(
                            onBack = { screen = FindEntryPoint.Waiting },
                            onFindDevice = { screen = FindEntryPoint.FindSend },
                            onSimulateBeingFound = { screen = FindEntryPoint.FindReceive }
                        )
                        FindEntryPoint.FindSend -> FindSendScreen(
                            deviceName = DEBUG_DEVICE_NAME,
                            isPhone = true,
                            onStop = { screen = FindEntryPoint.Debug }
                        )
                        FindEntryPoint.FindReceive -> FindReceiveScreen(
                            senderName = DEBUG_DEVICE_NAME,
                            onStop = { screen = FindEntryPoint.Debug }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen() {
    ScreenScaffold { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WaitingForConnectionScreen(
    onDebugUnlock: () -> Unit = {}
) {
    var tapCount by remember { mutableStateOf(0) }
    var lastTapUp by remember { mutableLongStateOf(0L) }

    ScreenScaffold { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Full-screen indeterminate edge ring; replaces the previous small
            // centered spinner so progress hugs the round display.
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(CircularProgressIndicatorDefaults.FullScreenPadding)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.waiting_for_connection),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.pointerInput(onDebugUnlock) {
                        // Hidden gesture: 5 taps arm the unlock, then a 1s
                        // press-and-hold on this text opens debug options.
                        awaitEachGesture {
                            awaitFirstDown()
                            val up = withTimeoutOrNull(DEBUG_UNLOCK_HOLD_MILLIS) {
                                waitForUpOrCancellation()
                            }
                            val now = SystemClock.uptimeMillis()
                            if (up == null) {
                                if (tapCount >= DEBUG_UNLOCK_TAPS &&
                                    now - lastTapUp <= DEBUG_TAP_WINDOW_MILLIS
                                ) {
                                    onDebugUnlock()
                                }
                                tapCount = 0
                                waitForUpOrCancellation()
                            } else {
                                tapCount =
                                    if (now - lastTapUp <= DEBUG_TAP_WINDOW_MILLIS) tapCount + 1 else 1
                                lastTapUp = now
                            }
                        }
                    }
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun WelcomePreview() {
    AsagityConnectForWearableTheme {
        AppScaffold {
            WelcomeScreen()
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun WaitingPreview() {
    AsagityConnectForWearableTheme {
        AppScaffold {
            WaitingForConnectionScreen()
        }
    }
}
