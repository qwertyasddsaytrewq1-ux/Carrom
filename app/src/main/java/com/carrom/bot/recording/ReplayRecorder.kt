// app/src/main/java/com/carrom/bot/recording/ReplayRecorder.kt

package com.carrom.bot.recording

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.view.Surface
import timber.log.Timber
import java.io.File

class ReplayRecorder(private val context: Context) {
    
    private var mediaRecorder: MediaRecorder? = null
    private val recordingDir = File(context.getExternalFilesDir(null), "replays")
    
    init {
        recordingDir.mkdirs()
    }
    
    fun startRecording(width: Int = 1080, height: Int = 1920): File? {
        return try {
            val outputFile = File(recordingDir, "replay_${System.currentTimeMillis()}.mp4")
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setVideoSize(width, height)
                setVideoFrameRate(30)
                setAudioEncodingBitRate(128000)
                setVideoEncodingBitRate(2500000)
                setOutputFile(outputFile.absolutePath)
                
                prepare()
                start()
            }
            
            Timber.d("Recording started: ${outputFile.name}")
            outputFile
        } catch (e: Exception) {
            Timber.e(e, "Recording start failed")
            null
        }
    }
    
    fun stopRecording(): Boolean {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            Timber.d("Recording stopped")
            true
        } catch (e: Exception) {
            Timber.e(e, "Recording stop failed")
            false
        }
    }
    
    fun getRecordedReplays(): List<File> {
        return recordingDir.listFiles()?.toList() ?: emptyList()
    }
}
