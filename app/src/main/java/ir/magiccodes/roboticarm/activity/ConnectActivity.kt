package ir.magiccodes.roboticarm.activity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import c.tlgbltcn.library.BluetoothHelper
import c.tlgbltcn.library.BluetoothHelperListener
import ir.magiccodes.roboticarm.DeviceEvent
import ir.magiccodes.roboticarm.DevicesAdapter
import ir.magiccodes.roboticarm.R
import ir.magiccodes.roboticarm.databinding.ActivityConnectBinding
import java.util.*
import kotlin.collections.ArrayList

class ConnectActivity : AppCompatActivity(), DeviceEvent, BluetoothHelperListener {
    lateinit var binding: ActivityConnectBinding
    lateinit var bluetoothHelper: BluetoothHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothHelper = BluetoothHelper(this, this)
            .setPermissionRequired(true)
            .create()

        setupUI()


    }

    private fun setupUI() {
        if (bluetoothHelper.isBluetoothEnabled()) {
            binding.tvBluetooth.setBackgroundResource(R.color.green)
            binding.tvBluetooth.text = "Bluetooth State On"
        } else {
            binding.tvBluetooth.setBackgroundResource(R.color.red)
            binding.tvBluetooth.text = "Bluetooth State Off"
        }

        if (bluetoothHelper.isBluetoothScanning()) binding.btnScan.text = "Stop discovery"
        else binding.btnScan.text = "Start discovery"


        binding.btnOnOff.setOnClickListener {

            if (bluetoothHelper.isBluetoothEnabled()) {

                bluetoothHelper.disableBluetooth()

            } else {
                bluetoothHelper.enableBluetooth()
            }
        }

        binding.btnScan.setOnClickListener {
            if (bluetoothHelper.isBluetoothScanning()) {
                bluetoothHelper.stopDiscovery()

            } else {
                bluetoothHelper.startDiscovery()
            }
        }

        binding.btnPaired.setOnClickListener {
            getPairedDevices()
        }
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        val devices = arrayListOf<BluetoothDevice>()
        pairedDevices?.forEach { device ->
            devices.add(device)
        }
        binding.recyclerPairedDevice.adapter = DevicesAdapter(devices, this)
        binding.recyclerPairedDevice.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    override fun onResume() {
        super.onResume()
        bluetoothHelper.registerBluetoothStateChanged()
    }

    override fun onPause() {
        super.onPause()
        bluetoothHelper.unregisterBluetoothStateChanged()
    }

    override fun selectedDevice(device: BluetoothDevice) {
        val intent = Intent(this, ControlActivity::class.java)
        intent.putExtra("key", device)
        startActivity(intent)
    }

    override fun getBluetoothDeviceList(device: BluetoothDevice?) {
        val devices = ArrayList<BluetoothDevice>()
        devices.add(device!!)
        binding.recyclerAvailableDevice.adapter = DevicesAdapter(devices, this)
        binding.recyclerAvailableDevice.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    override fun onDisabledBluetooh() {
        binding.tvBluetooth.setBackgroundResource(R.color.red)
        binding.tvBluetooth.text = "Bluetooth State Off"
    }

    override fun onEnabledBluetooth() {
        binding.tvBluetooth.text = "Bluetooth State On"
        binding.tvBluetooth.setBackgroundResource(R.color.green)
    }

    override fun onFinishDiscovery() {
        binding.progressBar.visibility = View.INVISIBLE
        Toast.makeText(this, "Scan Finished", Toast.LENGTH_LONG).show()
    }

    override fun onStartDiscovery() {
        binding.progressBar.visibility = View.VISIBLE
        Toast.makeText(this, "Scan Start", Toast.LENGTH_LONG).show()
    }


}

