package com.example.ud1000

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun <T> PersistentControl(
    key: String,
    defaultValue: T,
    read: (String, T) -> T,
    content: @Composable (MutableState<T>) -> Unit
) {
    val initialValue = read(key, defaultValue)
    val state = remember { mutableStateOf(initialValue) }

    LaunchedEffect(state.value) {
        Log.d("PersistentControl", "LaunchedEffect: ${state.value}")
        SharedPreferencesUtil.write(key, state.value)
    }

    content(state)
}