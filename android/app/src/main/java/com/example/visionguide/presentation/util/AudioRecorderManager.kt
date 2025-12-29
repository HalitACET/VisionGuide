
package com.example.visionguide.presentation.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    var isRecording = false
        private set

    fun startRecording(): File? {
        // Create file in cache dir
        outputFile = File(context.cacheDir, "record_${System.currentTimeMillis()}.mp3")
        
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        return try {
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            outputFile
        } catch (e: IOException) {
            Log.e("AudioRecorder", "Recording failed", e)
            null
        }
    }

    fun stopRecording(): File? {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Stop failed", e)
        } finally {
            mediaRecorder = null
            isRecording = false
        }
        return outputFile
    }
}
