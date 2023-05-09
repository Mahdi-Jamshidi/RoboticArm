package ir.magiccodes.roboticarm

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import ir.magiccodes.roboticarm.databinding.ItemDevicesBinding

class DevicesAdapter(val devices: ArrayList<BluetoothDevice>, private val deviceEvent:DeviceEvent ):RecyclerView.Adapter<DevicesAdapter.DevicesViewHolder>() {
    lateinit var binding: ItemDevicesBinding
//    private val devices = ArrayList<BluetoothDevice>()

    inner class DevicesViewHolder(itemView: View): ViewHolder(itemView){
        @SuppressLint("MissingPermission")
        fun bindItem(device: BluetoothDevice){
            binding.tvName.text = device.name ?: "null"
            binding.tvAddress.text = device.address

            itemView.setOnClickListener {
                deviceEvent.selectedDevice(device)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = ItemDevicesBinding.inflate(layoutInflater)
        return DevicesViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        holder.bindItem(devices[position])
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    fun addDevice(device: BluetoothDevice) {
        devices.add(device)
        notifyItemInserted(itemCount)
    }

    fun clearDevices() {
        devices.clear()
        notifyDataSetChanged()
    }
}
interface DeviceEvent {
    fun selectedDevice(device: BluetoothDevice)
}