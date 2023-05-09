package ir.magiccodes.roboticarm.manager

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import kotlin.math.roundToInt

class SensorManager(private val context: Context, val sensorEvent: SensorEvent) {

    // get rotation Vector Sensor
    private val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    // listening to rotation state to get device rotation
    private val rotationListener = object : SensorEventListener {
        override fun onSensorChanged(event: android.hardware.SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(16)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                // Remap coordinate system
                val remappedRotationMatrix = FloatArray(16)
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remappedRotationMatrix
                )

                // Convert to orientations
                val orientations = FloatArray(3)
                SensorManager.getOrientation(remappedRotationMatrix, orientations)

                for (i in 0..2) {
                    orientations[i] = Math.toDegrees(orientations[i].toDouble()).toFloat()
                }
                // this is rotation Degree in portrait
                val rotationDegree = orientations[2].roundToInt()
                val landscapeRotationDegree = rotationDegree + 90
                sensorEvent.deviceRotationDegree(landscapeRotationDegree)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
    }

    fun registerRotationListener() {
        if (rotationVectorSensor == null) {
            // Rotation Vector Sensor is not available on this device
            Toast.makeText(context, "Rotation Vector Sensor is not available on this device", Toast.LENGTH_LONG).show()
        } else {
            // Rotation Vector Sensor is available on this device
            sensorManager.registerListener(rotationListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun unregisterRotationListener() {
        sensorManager.unregisterListener(rotationListener)
    }

}

interface SensorEvent {
    fun deviceRotationDegree(rotationDegree: Int)
}