// app/src/main/java/com/carrom/bot/ml/AimPredictionModel.kt

package com.carrom.bot.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import timber.log.Timber
import kotlin.math.*

class AimPredictionModel(private val context: Context) {
    
    private lateinit var interpreter: Interpreter
    private var isInitialized = false
    
    init {
        try {
            val modelBuffer = FileUtil.loadMappedFile(context, "aim_model.tflite")
            interpreter = Interpreter(modelBuffer)
            isInitialized = true
            Timber.d("ML Model loaded successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load ML model")
        }
    }
    
    fun predictOptimalAim(
        strikerX: Float,
        strikerY: Float,
        targetX: Float,
        targetY: Float,
        historicalAccuracy: FloatArray,
        currentGameState: FloatArray
    ): AimPrediction {
        if (!isInitialized) return fallbackAim(strikerX, strikerY, targetX, targetY)
        
        val inputShape = intArrayOf(1, 10)
        val inputBuffer = TensorBuffer.createFixedSize(inputShape, org.tensorflow.lite.DataType.FLOAT32)
        
        val input = floatArrayOf(
            strikerX, strikerY,
            targetX, targetY,
            historicalAccuracy.average().toFloat(),
            currentGameState[0], // game mode
            currentGameState[1], // pressure level
            currentGameState[2], // distance
            currentGameState[3], // puck type
            currentGameState[4]  // board state
        )
        
        inputBuffer.loadArray(input)
        
        val outputShape = intArrayOf(1, 5)
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, org.tensorflow.lite.DataType.FLOAT32)
        
        interpreter.run(inputBuffer.buffer, outputBuffer.buffer)
        
        val output = outputBuffer.floatArray
        
        return AimPrediction(
            optimalAngle = output[0],
            confidence = output[1],
            predictedPockets = output[2].toInt(),
            powerSuggestion = output[3],
            alternativeAngle = output[4]
        )
    }
    
    private fun fallbackAim(strikerX: Float, strikerY: Float, targetX: Float, targetY: Float): AimPrediction {
        val dx = targetX - strikerX
        val dy = targetY - strikerY
        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        
        return AimPrediction(
            optimalAngle = angle,
            confidence = 0.7f,
            predictedPockets = 1,
            powerSuggestion = 70f,
            alternativeAngle = angle + 15f
        )
    }
    
    data class AimPrediction(
        val optimalAngle: Float,
        val confidence: Float,
        val predictedPockets: Int,
        val powerSuggestion: Float,
        val alternativeAngle: Float
    )
}
