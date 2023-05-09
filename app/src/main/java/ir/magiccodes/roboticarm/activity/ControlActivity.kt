package ir.magiccodes.roboticarm.activity

import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.douglasjunior.bluetoothclassiclibrary.*
import io.github.hyuwah.draggableviewlib.DraggableListener
import io.github.hyuwah.draggableviewlib.DraggableView
import io.github.hyuwah.draggableviewlib.setupDraggable
import ir.magiccodes.roboticarm.databinding.ActivityControlBinding
import ir.magiccodes.roboticarm.manager.*
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.atan2

class ControlActivity : AppCompatActivity() {
    lateinit var binding: ActivityControlBinding
    private lateinit var sensorManager: SensorManager
    lateinit var tvDraggable: DraggableView<ImageView>
    private lateinit var service: BluetoothService
    lateinit var timer: Timer

    var armA = 30 // arm to turn left or right
    var armB = 999 // first arm
    var armC = 575 // second arm
    var armD = 655 // third arm
    var armE = 1 // griper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControlBinding.inflate(layoutInflater)
        setContentView(binding.root)


        bluetoothConfig()

        // get device to connecting
        val bluetoothDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("key", BluetoothDevice::class.java)
        } else {
            intent.getParcelableExtra<BluetoothDevice>("key")
        }

        // check bluetooth device not null
        if (bluetoothDevice != null) {
            connecting(bluetoothDevice)
        } else {
            Toast.makeText(this, "bluetooth device is null ", Toast.LENGTH_SHORT).show()
        }


        setupSensor()
        setupDraggableView()
        timerToSendData()

        binding.btnReset.setOnClickListener {
            armA = 30
            armB = 999
            armC = 575
            armD = 655
            armE = 1

            tvDraggable.setViewPosition(0F,0F)
        }

    }

    private fun timerToSendData() {
        timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val dataToSend = "dataToSend: A${armA}dB${armB}dC${armC}dD${armD}dE${armE}d"
                    binding.tvDataSent.text = dataToSend
                }

                sendData("A${armA}d")
                thread { Thread.sleep(50L) }.join()
                sendData("B${armB}d")
                thread { Thread.sleep(50L) }.join()
                sendData("C${armC}d")
                thread { Thread.sleep(50L) }.join()
                sendData("D${armD}d")
                thread { Thread.sleep(50L) }.join()
                sendData("E${armE}d")

            }
        }
        timer.schedule(task, 0L, 250)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerRotationListener()

    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterRotationListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    private fun bluetoothConfig() {
        val config = BluetoothConfiguration()
        config.context = applicationContext
        config.bluetoothServiceClass = BluetoothClassicService::class.java
        config.bufferSize = 1024
        config.characterDelimiter = '\n'
        config.deviceName = "Robotic arm"
        config.callListenersInMainThread = true

        config.uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        BluetoothService.init(config)
        service = BluetoothService.getDefaultInstance()
    }

    private fun connecting(device: BluetoothDevice) {
        service.setOnEventCallback(object : BluetoothService.OnBluetoothEventCallback {
            override fun onDataRead(buffer: ByteArray?, length: Int) {

            }

            override fun onStatusChange(status: BluetoothStatus?) {
                Toast.makeText(this@ControlActivity, status?.name.toString(), Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onDeviceName(deviceName: String?) {
                Toast.makeText(this@ControlActivity, "Connected to $deviceName", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onToast(message: String?) {
                Toast.makeText(this@ControlActivity, "message: $message", Toast.LENGTH_SHORT).show()
            }

            override fun onDataWrite(buffer: ByteArray?) {
                val data = buffer?.toString(Charsets.UTF_8)
//                binding.tvDataSent.text = "Date sent: $data"
            }
        })

        service.connect(device);
    }

    private fun sendData(data: String) {
        val writer = BluetoothWriter(service)
        writer.write(data)
    }

    // up and down volume key
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (armE <= 1) armE = 1 else armE -= 1
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (armE >= 5) armE = 5 else armE += 1
            }
            KeyEvent.KEYCODE_BACK -> onBackPressed()
        }
        binding.tvVolume.text = "Volume $armE"
        return true
    }

    private fun setupDraggableView() {

        val tvDraggableListener = object : DraggableListener {
            override fun onPositionChanged(view: View) {
                var x = view.x
                var y = view.y

                // convert x and y to angle
                val angle = (atan2(y, x)) * 229

                // set arms angle
                armB = mapValueArmB(angle)
                armC = mapValueArmC(angle)
                armD = mapValueArmD(angle)

            }
        }

        tvDraggable = DraggableView.Builder(binding.imgDrag)
            .setStickyMode(DraggableView.Mode.NON_STICKY)
            .setListener(tvDraggableListener)
            .build()
    }

    fun mapValueArmB(value: Float): Int {
        val result = (value / 360) * 99 + 900
        return result.toInt()
    }

    fun mapValueArmC(value: Float): Int {
        val oldRange = 360
        val newRange = 525
        val result = (((value / oldRange) * newRange) + 50)
        return result.toInt()
    }

    fun mapValueArmD(value: Float): Int {
        val oldRange = 360
        val newRange = 655
        val result = ((value / oldRange) * newRange)
        return result.toInt()
    }

    private fun setupSensor() {
        sensorManager = SensorManager(this, object : SensorEvent {
            override fun deviceRotationDegree(rotationDegree: Int) {
                binding.tvRotation.text = "Rotation $rotationDegree"

                when (rotationDegree) {
                    -17, -18, -19, -20, -25, -26, -27, -28, -29, -30, -31, -32 -> {
                        armA = 20
                    }
                    -33, -34, -35, -36, -37, -38, -39, -40, -41, -42, -43, -44, -45, -46, -47, -48 -> {
                        armA = 10
                    }
                    17, 18, 19, 20, 25, 26, 27, 28, 29, 30, 31, 32 -> {
                        armA = 50
                    }
                    33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48 -> {
                        armA = 40
                    }
                    2,1,0,-1,-2 -> {
                        armA = 30
                    }
                }
            }
        })
    }

}