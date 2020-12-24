package br.com.espacobistro.bluetoothprinter.ui.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.graphics.Path
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import br.com.espacobistro.bluetoothprinter.R

class SelectDeviceFragment : Fragment() {

    private lateinit var viewModel: SelectDeviceViewModel
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var pairedDevices: Set<BluetoothDevice>
    private val REQUEST_ENABLE_BLUETOOTH = 1

    companion object {
        val EXTRA_ADDRESS = "Device_address"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.select_device_fragment, container, false)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if(bluetoothAdapter != null) {
            if (bluetoothAdapter?.isEnabled!!) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
            }
        }
        else{
            Toast.makeText(requireContext(), "Adaptador bluetooth não encontrado!", Toast.LENGTH_SHORT).show()
        }

        val buttonRefresh = root.findViewById<Button>(R.id.select_device_refresh)!!
        buttonRefresh.setOnClickListener { pairedDeviceList() }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SelectDeviceViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun pairedDeviceList(){
        pairedDevices = bluetoothAdapter?.bondedDevices?: setOf()
        val list: MutableList<BluetoothDevice> = mutableListOf()

        pairedDevices.map {
            list.add(it)
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
        val listDevice = view?.findViewById<ListView>(R.id.select_device_list)!!
        listDevice.adapter = adapter

        listDevice.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device = list[position]
            val address = device.address
            findNavController().navigate(SelectDeviceFragmentDirections.actionSelectDeviceFragmentToTitleFragment(address))
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH){
            if (resultCode == Activity.RESULT_OK){
                if(bluetoothAdapter?.isEnabled!!){
                    Toast.makeText(requireContext(), "Bluetooth enabled!", Toast.LENGTH_SHORT).show()
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