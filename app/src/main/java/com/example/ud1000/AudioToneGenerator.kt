package com.example.ud1000

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

// constants:
const val SAMPLE_RATE = 44100
const val DEFAULT_FADE_DURATION: Long = 100

class AudioToneGenerator(private val sampleRate: Int = 44100) {
    private val bufferSize = 1024
    private var targetFrequency = 440.0
    private var targetVolume = 0.5
    @Volatile private var currentFrequency = targetFrequency
    @Volatile private var currentVolume = targetVolume

    private val twoPi = 2.0 * PI
    private var isPlaying: Boolean = false

    private val minBufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val audioTrack = AudioTrack(
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build(),
        AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build(),
        minBufferSize,
        AudioTrack.MODE_STREAM,
        AudioManager.AUDIO_SESSION_ID_GENERATE
    )

//    init {
//        val audioManager = AudioManager::class.java
//        val format = AudioFormat.Builder()
//            .setSampleRate(sampleRate)
//            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
//            .build()
//        val mode = AudioTrack.MODE_STREAM // or AudioTrack.PERFORMANCE_MODE_POWER_SAVING
//        val sessionId =
//        audioTrack = AudioTrack(
//            format,
//            bufferSize,
//            mode,
//            sessionId,
//        )
//        audioTrack = AudioTrack(
//            AudioManager.STREAM_MUSIC, sampleRate,
//            AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
//            bufferSize, AudioTrack.MODE_STREAM
//        )
//    }

    fun start() {
        if (isPlaying) {
            return
        }
        isPlaying = true
        audioTrack.play()
        Thread {
            val buffer = ShortArray(minBufferSize / 256)
            var phase = 0.0
            var interpolatedFrequency = currentFrequency
            var interpolatedVolume = currentVolume
            while (isPlaying) {
                for (i in buffer.indices) {
                    interpolatedFrequency += (currentFrequency - interpolatedFrequency) * 0.001
                    interpolatedVolume += (currentVolume - interpolatedVolume) * 0.001


                    phase += twoPi * interpolatedFrequency / sampleRate
                    if (phase >= twoPi) {
                        phase -= twoPi
                    }
//                    buffer[i] = (sin(phase) * Short.MAX_VALUE * currentVolume).toInt().toShort()
                    fun sampleForPhase(phase: Double): Short {
                        val phase2 = phase * 2
                        val phase3 = phase * 3
                        val phase4 = phase * 4
                        val fundamental = sin(phase) * Short.MAX_VALUE * (interpolatedVolume * 0.9)
                        val harmonic1 = sin(phase2) * Short.MAX_VALUE * (interpolatedVolume * 0.01)
                        val harmonic2 = sin(phase3) * Short.MAX_VALUE * (interpolatedVolume * 0.01)
                        val harmonic3 = sin(phase4) * Short.MAX_VALUE * (interpolatedVolume * 0.08)
                        return (fundamental + harmonic1 + harmonic2 + harmonic3).toInt().toShort()
                    }
                    buffer[i] = sampleForPhase(phase)
                }
                audioTrack.write(buffer, 0, buffer.size)
            }
//            audioTrack.stop()
        }.start()
    }

    fun stop() {
        isPlaying = false
    }

    fun fadeIn(duration: Long? = null, volume: Double? = null, callback: (() -> Unit)? = null) {
        Thread {
            targetVolume = volume ?: targetVolume
            val steps = 100
            val stepDuration = (duration ?: DEFAULT_FADE_DURATION) / steps
            val volume = volume ?: this.targetVolume
            val stepVolume = volume / steps
            this.start()
            for (i in 0 until steps) {
                currentVolume = (stepVolume * i)
                Thread.sleep(stepDuration)
            }
            if (callback != null) {
                callback()
            }
        }.start()
    }

    fun fadeOut(duration: Long? = null, callback: (() -> Unit)? = null) {
        Thread {
            val steps = 100
            val volume = this.targetVolume
            val stepDuration = (duration ?: DEFAULT_FADE_DURATION) / steps
            val stepVolume = volume / steps
            for (i in 0 until steps) {
                currentVolume = (volume - stepVolume * i)
                Thread.sleep(stepDuration)
            }
            if (callback != null) {
                callback()
            }
        }.start()
    }

    fun fadeOutAndStop(duration: Long? = null) {
        fadeOut(duration) {
            thread {
                Thread.sleep(100)
                stop()
            }
        }
    }

    fun setFrequency(frequency: Double) {
        targetFrequency = frequency
        currentFrequency = frequency
    }

    fun setVolume(volume: Double) {
        targetVolume = volume
        currentVolume = volume
    }

    companion object {
        @Volatile
        private var INSTANCE: AudioToneGenerator? = null

        fun getInstance(): AudioToneGenerator {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioToneGenerator(SAMPLE_RATE).also { INSTANCE = it }
            }
        }
    }
}
