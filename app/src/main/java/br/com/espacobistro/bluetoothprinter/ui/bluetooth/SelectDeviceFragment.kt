package br.com.espacobistro.bluetoothprinter.ui.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import br.com.espacobistro.bluetoothprinter.R

class SelectDeviceFragment : Fragment() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var pairedDevices: Set<BluetoothDevice>
    private val REQUEST_ENABLE_BLUETOOTH = 1

    private lateinit var listDevice: ListView
    private lateinit var progress: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.select_device_fragment, container, false)

        listDevice = root.findViewById(R.id.select_device_list)!!
        progress = root.findViewById(R.id.device_progress)!!

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if(bluetoothAdapter != null) {
            if (!bluetoothAdapter?.isEnabled!!) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
            }
            else {
                pairedDeviceList()
            }
        }
        else{
            Toast.makeText(requireContext(), "Adaptador bluetooth não encontrado!", Toast.LENGTH_SHORT).show()
        }

        val buttonRefresh = root.findViewById<Button>(R.id.select_device_refresh)!!
        buttonRefresh.setOnClickListener { pairedDeviceList() }

        return root
    }

    private fun pairedDeviceList(){
        pairedDevices = bluetoothAdapter?.bondedDevices?: setOf()
        val list: MutableList<BluetoothDevice> = mutableListOf()

        pairedDevices.map {
            list.add(it)
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list.map{it.name})
        listDevice.adapter = adapter

        listDevice.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device = list[position]
            progress.visibility = VISIBLE
            findNavController().navigate(SelectDeviceFragmentDirections.actionSelectDeviceFragmentToTitleFragment(device))
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH){
            if (resultCode == Activity.RESULT_OK){
                if(bluetoothAdapter?.isEnabled!!){
                    Toast.makeText(requireContext(), "Bluetooth enabled!", Toast.LENGTH_SHORT).show()
                    pairedDeviceList()
                }
                else {
                    Toast.makeText(requireContext(), "Bluetooth disabled!", Toast.LENGTH_SHORT).show()//Disabled
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(requireContext(), "Resquest canceled!", Toast.LENGTH_SHORT).show()// resquest canceled
            }
        }
    }
}