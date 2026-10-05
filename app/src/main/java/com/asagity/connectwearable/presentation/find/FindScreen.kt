package com.asagity.connectwearable.presentation.find

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CircularProgressIndicatorDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.asagity.connectwearable.R
import com.asagity.connectwearable.audio.FindAudioPlayer
import com.asagity.connectwearable.haptics.FindHaptics
import com.asagity.connectwearable.presentation.theme.AsagityConnectForWearableTheme

/** Debug fixture: no mobile debugger exists yet, so the peer is fixed. */
const val DEBUG_DEVICE_NAME = "My Phone"

private const val SEND_SOUND_ASSET = "sounds/FindmeWearSend.wav"
private const val RECEIVE_SOUND_ASSET = "sounds/FindmeWearRecv.wav"

/** Full breathe cycle of the send-find edge ring. */
private const val SEND_BREATH_MILLIS = 1000

/** Full breathe cycle of the receive-find edge ring. */
private const val RECEIVE_BREATH_MILLIS = 600

/**
 * Send-find: locate the connected mobile device from the watch.
 * Idle until the edge button starts the search; while searching the
 * edge ring breathes, the send sound loops, and the button turns
 * into Stop. Stopping exits this screen via [onStop].
 */
@Composable
fun FindSendScreen(
    deviceName: String = DEBUG_DEVICE_NAME,
    isPhone: Boolean = true,
    onStop: () -> Unit = {}
) {
    var searching by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val audio = remember { FindAudioPlayer(context.applicationContext) }
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    DisposableEffect(Unit) {
        onDispose { audio.release() }
    }
    LaunchedEffect(searching) {
        if (searching) {
            audio.startLoop(SEND_SOUND_ASSET)
        } else {
            audio.stop()
        }
    }

    ScreenScaffold(
        edgeButton = {
            EdgeButton(
                onClick = {
                    if (searching) {
                        searching = false
                        onStop()
                    } else {
                        searching = true
                    }
                }
            ) {
                Icon(
                    imageVector = if (searching) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        if (searching) R.string.find_stop else R.string.find_action
                    )
                )
            }
        },
        scrollState = columnState
    ) { contentPadding ->
        if (searching) {
            BreathingEdgeRing(breathDurationMillis = SEND_BREATH_MILLIS)
        }
        TransformingLazyColumn(
            state = columnState,
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                )
            }
            item {
                Text(
                    text = stringResource(
                        if (isPhone) R.string.device_type_phone else R.string.device_type_tablet
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                )
            }
            if (searching) {
                item {
                    Text(
                        text = stringResource(R.string.find_searching),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                    )
                }
            }
        }
    }
}

/**
 * Receive-find: the phone is locating this watch. The edge ring breathes
 * fast, the SOS haptics loop runs, and the receive sound loops until
 * Stop exits via [onStop].
 */
@Composable
fun FindReceiveScreen(
    senderName: String = DEBUG_DEVICE_NAME,
    onStop: () -> Unit = {}
) {
    val context = LocalContext.current
    val audio = remember { FindAudioPlayer(context.applicationContext) }
    val haptics = remember { FindHaptics(context.applicationContext) }
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    DisposableEffect(Unit) {
        audio.startLoop(RECEIVE_SOUND_ASSET)
        haptics.startSosLoop()
        onDispose {
            audio.release()
            haptics.stop()
        }
    }

    ScreenScaffold(
        edgeButton = {
            EdgeButton(onClick = onStop) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.find_stop))
            }
        },
        scrollState = columnState
    ) { contentPadding ->
        BreathingEdgeRing(breathDurationMillis = RECEIVE_BREATH_MILLIS)
        TransformingLazyColumn(
            state = columnState,
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = stringResource(R.string.find_receive_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                )
            }
            item {
                Text(
                    text = senderName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                )
            }
        }
    }
}

/**
 * Full-screen indeterminate edge ring pulsing with a seamless breathe
 * loop ([RepeatMode.Reverse] has no jump at the loop point).
 */
@Composable
fun BreathingEdgeRing(breathDurationMillis: Int) {
    val transition = rememberInfiniteTransition(label = "edge_breathe")
    val ringAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = breathDurationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "edge_breathe_alpha"
    )
    CircularProgressIndicator(
        modifier = Modifier
            .fillMaxSize()
            .padding(CircularProgressIndicatorDefaults.FullScreenPadding)
            .alpha(ringAlpha)
    )
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun FindSendPreview() {
    AsagityConnectForWearableTheme {
        AppScaffold {
            FindSendScreen()
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun FindReceivePreview() {
    AsagityConnectForWearableTheme {
        AppScaffold {
            FindReceiveScreen()
        }
    }
}
