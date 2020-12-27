package br.com.espacobistro.bluetoothprinter.ui.title

import android.bluetooth.BluetoothDevice
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.espacobistro.bluetoothprinter.domain.demo.Demo
import br.com.espacobistro.bluetoothprinter.util.bluetoohprinter.PrinterController
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException


class TitleViewModel : ViewModel() {

   private lateinit var printerController: PrinterController

   private var _isConnected: MutableLiveData<Boolean?> = PrinterController.isConnected
   var isConnected: LiveData<Boolean?> = _isConnected



   fun initPrinterController(bluetoothDevice: BluetoothDevice){
       viewModelScope.launch {
           printerController = PrinterController.getInstance(bluetoothDevice)
           printerController.beginListenData()
       }
   }


   fun printDemo(demos: List<Demo>){

       var totalPrice = 0.0

       printerController.apply {
           initPrinter()
           setCharBrazilAbnt()
           setAlignPosition(1)
           setBold(1)
           patchText("Pedido nº 2021")
           nextLine()
           nextLine()
           setAlignPosition(0)
           patchText("Cliente: Peter Parker")
           nextLine()
           patchText("Endereço: 20 Ingram Street, Forest Hills, Queens")
           nextLine()
           printThreeData("produto", "quant.", "valor")
           nextLine()
       }

       demos.forEach {
           printerController.apply {
               setAlignPosition(0)
               setBold(0)
               printThreeData(it.title, it.quantity.toString(), "R$ ${it.price.toString()}")
           }
           totalPrice += it.price * it.quantity
        }

       printerController.apply {
           nextLine()
           setAlignPosition(2)
           setBold(1)
           patchText("R$ ${totalPrice.toString()}")
           nextLine()
           nextLine()
           setAlignPosition(1)
           patchText("Obrigado pela preferência!!!")
           nextLine()
           nextLine()
       }

   }

    //lateinit var res: LiveData<String>

    fun print(text: String){
        printerController.apply {
            initPrinter()
            patchText(text)
            nextLine()
            nextLine()
        }
    }

    fun clearIsConnected(){
        _isConnected.value = null
    }


}
