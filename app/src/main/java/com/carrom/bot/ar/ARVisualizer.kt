// app/src/main/java/com/carrom/bot/ar/ARVisualizer.kt

package com.carrom.bot.ar

import android.content.Context
import com.google.ar.core.*
import com.google.ar.sceneform.*
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import timber.log.Timber

class ARVisualizer(private val context: Context) {
    
    private var session: Session? = null
    private val anchors = mutableListOf<Anchor>()
    
    fun initializeARSession(): Boolean {
        return try {
            session = Session(context, setOf(
                Session.Feature.FRONT_CAMERA,
                Session.Feature.AUTO_FOCUS
            ))
            Timber.d("AR session initialized")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to init AR")
            false
        }
    }
    
    fun visualizeAimTrajectory(
        strikerPos: Vector3,
        angle: Float,
        power: Float,
        frame: Frame
    ): Anchor? {
        if (session == null) return null
        
        try {
            val hitTestResults = frame.hitTest(320f, 320f)
            
            if (hitTestResults.isNotEmpty()) {
                val hit = hitTestResults[0]
                val anchor = hit.createAnchor()
                anchors.add(anchor)
                
                // Create trajectory visualization
                val trajectoryPoints = calculateTrajectoryPoints(strikerPos, angle, power)
                
                Timber.d("Visualized trajectory with ${trajectoryPoints.size} points")
                return anchor
            }
        } catch (e: Exception) {
            Timber.e(e, "AR visualization failed")
        }
        
        return null
    }
    
    private fun calculateTrajectoryPoints(pos: Vector3, angle: Float, power: Float): List<Vector3> {
        val points = mutableListOf<Vector3>()
        val angleRad = Math.toRadians(angle.toDouble()).toFloat()
        
        for (t in 0..100 step 5) {
            val tNorm = t / 100f
            val distance = power * 2
            val x = pos.x + (tNorm * distance * cos(angleRad.toDouble())).toFloat()
            val y = pos.y
            val z = pos.z + (tNorm * distance * sin(angleRad.toDouble())).toFloat()
            
            points.add(Vector3(x, y, z))
        }
        
        return points
    }
    
    fun visualizePuckPositions(pucks: List<com.carrom.bot.Puck>, frame: Frame) {
        if (session == null) return
        
        try {
            val hitTestResults = frame.hitTest(320f, 320f)
            
            if (hitTestResults.isNotEmpty()) {
                val hit = hitTestResults[0]
                val anchor = hit.createAnchor()
                
                pucks.forEach { puck ->
                    val puckPos = Vector3(puck.x / 100f, 0f, puck.y / 100f)
                    Timber.d("Puck at ${puck.type}: ($puckPos)")
                }
                
                anchors.add(anchor)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to visualize pucks")
        }
    }
    
    fun clearVisualization() {
        anchors.forEach { it.detach() }
        anchors.clear()
    }
    
    fun release() {
        clearVisualization()
        session?.close()
    }
}
