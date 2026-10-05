package com.asagity.connectwearable.presentation.debug

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.asagity.connectwearable.BuildConfig
import com.asagity.connectwearable.R
import com.asagity.connectwearable.presentation.theme.AsagityConnectForWearableTheme

/**
 * Scrollable debug options screen. Reachable only via the hidden
 * tap-then-hold gesture on the waiting page. The action buttons are
 * intentionally not wired yet ([enabled] = false); they will be
 * connected to real debug behaviors later.
 */
@Composable
fun DebugOptionsScreen(
    onBack: () -> Unit = {},
    onFindDevice: () -> Unit = {},
    onSimulateBeingFound: () -> Unit = {}
) {
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(
            state = columnState,
            contentPadding = contentPadding
        ) {
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                        .minimumVerticalContentPadding(
                            ListHeaderDefaults.minimumTopListContentPadding
                        ),
                    transformation = SurfaceTransformation(transformationSpec)
                ) {
                    Text(text = stringResource(R.string.debug_options))
                }
            }
            item {
                Text(
                    text = stringResource(
                        R.string.debug_version,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                )
            }
            item {
                DebugActionButton(
                    label = stringResource(R.string.debug_action_pairing_done),
                    icon = Icons.Default.CheckCircle,
                    transformationSpec = transformationSpec
                )
            }
            item {
                DebugActionButton(
                    label = stringResource(R.string.debug_action_enter_home),
                    icon = Icons.Default.Home,
                    transformationSpec = transformationSpec
                )
            }
            item {
                DebugActionButton(
                    label = stringResource(R.string.debug_action_being_found),
                    icon = Icons.Default.Notifications,
                    transformationSpec = transformationSpec,
                    enabled = true,
                    onClick = onSimulateBeingFound
                )
            }
            item {
                DebugActionButton(
                    label = stringResource(R.string.debug_action_find_device),
                    icon = Icons.Default.Search,
                    transformationSpec = transformationSpec,
                    enabled = true,
                    onClick = onFindDevice
                )
            }
            item {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                        .minimumVerticalContentPadding(
                            ButtonDefaults.minimumVerticalListContentPadding
                        ),
                    transformation = SurfaceTransformation(transformationSpec),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    },
                ) {
                    Text(
                        text = stringResource(R.string.debug_back),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TransformingLazyColumnItemScope.DebugActionButton(
    label: String,
    icon: ImageVector,
    transformationSpec: TransformationSpec,
    enabled: Boolean = false,
    onClick: () -> Unit = {}
) {
    Button(
        // TODO: wire the remaining actions to real debug behavior.
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(this, transformationSpec)
            .minimumVerticalContentPadding(
                ButtonDefaults.minimumVerticalListContentPadding
            ),
        transformation = SurfaceTransformation(transformationSpec),
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DebugOptionsPreview() {
    AsagityConnectForWearableTheme {
        AppScaffold {
            DebugOptionsScreen()
        }
    }
}
