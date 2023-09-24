package com.example.ud1000

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

// constants:
const val SAMPLE_RATE = 44100

class AudioToneGenerator(private val sampleRate: Int = 44100) {
    private val bufferSize = 1024
    private val audioTrack: AudioTrack
    private var frequency: Double = 440.0
    private var volume: Float = 0f
    private var phase: Double = 0.0
    private val twoPi = 2.0 * PI
    private var isPlaying: Boolean = false

    init {
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC, sampleRate,
            AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
            bufferSize, AudioTrack.MODE_STREAM
        )
    }

    fun start() {
        Thread {
            audioTrack.play()

            val buffer = ShortArray(bufferSize)
            if (isPlaying) {
                return@Thread
            }
            isPlaying = true
            while (isPlaying) {
                for (i in 0 until bufferSize) {
                    phase += twoPi * frequency / sampleRate
                    if (phase >= twoPi) {
                        phase -= twoPi
                    }
//                    buffer[i] = (sin(phase) * Short.MAX_VALUE.toFloat() * volume).toInt().toShort()
                    val phase1 = phase
                    val phase2 = phase1 * 2
                    val phase3 = phase1 * 3
                    val phase4 = phase1 * 4
                    val fundamental = sin(phase1) * Short.MAX_VALUE.toFloat() * (volume * 0.9)
                    val harmonic1 = sin(phase2) * Short.MAX_VALUE.toFloat() * (volume * 0.01)
                    val harmonic2 = sin(phase3) * Short.MAX_VALUE.toFloat() * (volume * 0.01)
                    val harmonic3 = sin(phase4) * Short.MAX_VALUE.toFloat() * (volume * 0.08)
                    buffer[i] = (fundamental + harmonic1 + harmonic2 + harmonic3).toInt().toShort()
                }
                audioTrack.write(buffer, 0, bufferSize)
            }
        }.start()
    }

    fun stop() {
        isPlaying = false
        audioTrack.stop()
    }

    fun fadeIn(duration: Long, volume: Float? = null) {
        Thread {
            val steps = 100
            val stepDuration = duration / steps
            val volume = volume ?: this.volume
            val stepVolume = volume / steps
            this.setVolume(0f)
            this.start()
            for (i in 0 until steps) {
                setVolume(stepVolume * i)
                Thread.sleep(stepDuration)
            }
        }.start()
    }

    fun fadeOut(duration: Long) {
        Thread {
            val steps = 100
            val volume = this.volume
            val stepDuration = duration / steps
            val stepVolume = volume / steps
            for (i in 0 until steps) {
                setVolume(volume - stepVolume * i)
                Thread.sleep(stepDuration)
            }
            this.stop()
            setVolume(volume)
        }.start()
    }

    fun setFrequency(frequency: Double) {
        this.frequency = frequency
    }

    fun setVolume(volume: Float) {
        this.volume = volume
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
