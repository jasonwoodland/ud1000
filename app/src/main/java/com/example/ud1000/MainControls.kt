package com.example.ud1000

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.floor


@Composable
fun MainControls() {
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(0.1.dp)),
        verticalArrangement = Arrangement.Top,

    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp),
            ) {
                item {
                    PersistentControl(
                        key = "frameSize",
                        defaultValue = 1700.0f,
                        read = ::readFloat
                    ) { state ->
                        SettingsItem(
                            label = "Frame Size",
                            valueContent = {
                                Text(
                                    text = "${state.value.toInt()} µs",
                                )
                            },
                            content = {
                                Slider(
                                    value = state.value as Float,
                                    onValueChange = {
                                        state.value = it
                                    },
                                    valueRange = 1400f..2000f
                                )
                            }
                        )
                    }
                }

                item {
                    PersistentControl(
                        key = "mainLength",
                        defaultValue = 240.0f,
                        read = ::readFloat
                    ) { state ->
                        SettingsItem(
                            label = "Main Length",
                            valueContent = {
                                Text(
                                    text = "${state.value.toInt()} µs",
                                )
                            },
                            content = {
                                Slider(
                                    value = state.value as Float,
                                    onValueChange = {
                                        state.value = it
                                    },
                                    valueRange = 240f..280f
                                )
                            }
                        )
                    }
                }

                item {
                    PersistentControl(
                        key = "rxGate",
                        defaultValue = 10.0f,
                        read = ::readFloat
                    ) { state ->
                        SettingsItem(
                            label = "RX Gate",
                            valueContent = {
                                Text(
                                    text = "${state.value.toInt()} µs",
                                )
                            },
                            content = {
                                Slider(
                                    value = state.value as Float,
                                    onValueChange = {
                                        state.value = it
                                    },
                                    valueRange = 10f..20f,
                                    steps = 9
                                )
                            }
                        )
                    }
                }

                item {
                    PersistentControl(
                        key = "dampingLevel",
                        defaultValue = 0,
                        read = ::readInt
                    ) { state ->
                        SettingsItem(
                            label = "Damping Level",
                            valueContent = {
                                Text(
                                    text = "${state.value}",
                                )
                            },
                            content = {
                                Slider(
                                    value = state.value.toFloat(),
                                    onValueChange = {
                                        state.value = it.toInt()
                                    },
                                    valueRange = 0f..2f,
                                    steps = 1,
                                )
                            }
                        )
                    }
                }

                item {
                    PersistentControl(
                        key = "txEnable",
                        defaultValue = false,
                        read = ::readBoolean
                    ) { state ->
                        SettingsItem(
                            label = "TX Enable",
                            valueContent = {
                                Switch(
                                    checked = state.value as Boolean,
                                    onCheckedChange = {
                                        state.value = it
                                        SoundEffectPlayer.newInstance(view.context, R.raw.beep).play()
                                    },
                                )
                            }
                        )
                    }
                }

                item {
                    PersistentControl(
                        key = "audioEnable",
                        defaultValue = true,
                        read = ::readBoolean
                    ) { state ->
                        SettingsItem(
                            label = "Audio Enable",
                            valueContent = {
                                Switch(
                                    checked = state.value as Boolean,
                                    onCheckedChange = {
                                        state.value = it
                                        if (it) {
                                            AudioToneGenerator.getInstance().fadeIn(100)
                                        } else {
                                            AudioToneGenerator.getInstance().fadeOut(100)
                                        }
                                    },
                                )
                            }
                        )
                    }
                }

                item {
                    PersistentControl(
                        key = "audioFreq",
                        defaultValue = 100.0f,
                        read = ::readFloat
                    ) { state ->
                        SettingsItem(
                            label = "Frequency",
                            valueContent = {
                                Text(
                                    text = "${floor(state.value)} Hz",
                                )
                            },
                            content = {
                                Slider(
                                    value = state.value as Float,
                                    onValueChange = {
                                        state.value = it
                                        AudioToneGenerator.getInstance().setFrequency(it.toDouble())
                                    },
                                    valueRange = 100f..3000f
                                )
                            }
                        )
                    }
                }

                item {
                    PersistentControl(
                        key = "audioVolume",
                        defaultValue = 100.0f,
                        read = ::readFloat
                    ) { state ->
                        SettingsItem(
                            label = "Volume",
                            valueContent = {
                                Text(
                                    text = "${floor(state.value * 100)}%",
                                )
                            },
                            content = {
                                Slider(
                                    value = state.value as Float,
                                    onValueChange = {
                                        state.value = it
                                        AudioToneGenerator.getInstance().setVolume(it)
                                        SoundEffectPlayer.newInstance(view.context, R.raw.beep).play()
                                    },
                                    valueRange = 0.0f..1.0f,
                                    steps = 9
                                )
                            }
                        )
                    }
                }
            }
        }

            Row (
                modifier = Modifier
                    .wrapContentHeight()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ){
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { /*TODO*/ }
                ) {
                    Text(text = "GB")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary,
                    ),
                    onClick = { /*TODO*/ }
                ) {
                    Text(text = "Scan FTDI")
                }
            }
    }
}

@Composable
fun SettingsItem(label: String, content: @Composable (() -> Unit)? = null, valueContent: @Composable (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Row(
                // vertical align
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = content?.let { 12.dp } ?: 0.dp,
                        bottom = 0.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold
                )
                valueContent?.let {
                    Spacer(modifier = Modifier.weight(1f))
                    it()
                }
            }
            content?.let {
                it()
            }
        }
    }
}