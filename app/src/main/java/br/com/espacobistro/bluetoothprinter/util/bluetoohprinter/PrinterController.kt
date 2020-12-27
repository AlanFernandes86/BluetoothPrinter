package br.com.espacobistro.bluetoothprinter.util.bluetoohprinter

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.util.*

class PrinterController private constructor(private var bluetoothDevice: BluetoothDevice) {

    companion object {
        private lateinit var printerController: PrinterController
        var isConnected: MutableLiveData<Boolean?> = MutableLiveData()
        suspend fun getInstance(bluetoothDevice: BluetoothDevice): PrinterController {
            return withContext(Dispatchers.IO) {
                return@withContext if (::printerController.isInitialized) {
                    printerController.closeBluetoothPrinter()
                    printerController = PrinterController(bluetoothDevice)
                    withContext(Dispatchers.Main){
                        isConnected.value = printerController.init()
                    }
                    printerController
                } else {
                    printerController = PrinterController(bluetoothDevice)
                    withContext(Dispatchers.Main){
                        isConnected.value = printerController.init()
                    }
                    printerController
                }
            }
        }
    }

    //Standard uuid from string //
    private lateinit var uuidSting: UUID
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var outputStream: OutputStream
    private lateinit var inputStream: InputStream
    private lateinit var printWriter: PrintWriter

    private lateinit var thread: Thread
    private lateinit var readBuffer: ByteArray
    private var readBufferPosition = 0

    private suspend fun init(): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting)
                bluetoothSocket.connect()
                outputStream = bluetoothSocket.outputStream
                inputStream = bluetoothSocket.inputStream
                printWriter = PrintWriter(outputStream.bufferedWriter(Charsets.ISO_8859_1))
                true
            } catch (e: Exception) {
                println(e.stackTraceToString())
                false
            }
        }
    }

    @Volatile
    var stopWorker = false


    private val LINE_BYTE_SIZE = 32
    private val LEFT_LENGTH = 20
    private val RIGHT_LENGTH = 12
    private val LEFT_TEXT_MAX_LENGTH = 10


    // Open Bluetooth Printer
    @Throws(IOException::class)
    fun openBluetoothPrinter() {
        try {
            bluetoothSocket.connect()
            outputStream = bluetoothSocket.outputStream
            inputStream = bluetoothSocket.inputStream
            printWriter = PrintWriter(outputStream.bufferedWriter(Charsets.ISO_8859_1))
        } catch (ex: Exception) {
        }
    }

    // Disconnect Printer //
    @Throws(IOException::class)
    fun closeBluetoothPrinter() {
        try {
            stopWorker = true
            outputStream.close()
            inputStream.close()
            bluetoothSocket.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun initPrinter() {
        printWriter.write(0x1B)
        printWriter.write(0x40)
        printWriter.flush()
    }

    @Throws(IOException::class)
    fun patchText(text: String) {
        printWriter.println(text)
        printWriter.flush()
    }

    @Throws(IOException::class)
    fun setCharCode(code: Int) {
        printWriter.write(0x1B)
        printWriter.write(0x74)
        printWriter.write(code)
        printWriter.flush()
    }

    // {0x1b,0x74,0x03}; // Brasil - ABNT - 80
    @Throws(IOException::class)
    fun setCharBrazilAbnt() {
        printWriter.write(0x1B)
        printWriter.write(0x74)
        printWriter.write(80)
        printWriter.flush()
    }

    /* Set text alignment
          * @param align Print position 0: Left (default) 1: Center 2: Right
    * @throws IOException
    */
    @Throws(IOException::class)
    fun setAlignPosition(align: Int) {
        printWriter.write(0x1B)
        printWriter.write(0x61)
        printWriter.write(align)
        printWriter.flush()
    }

    @Throws(IOException::class)
    fun nextLine() {
        printWriter.write("\n")
        printWriter.flush()
    }

    @Throws(IOException::class)
    fun printTab(length: Int) {
        for (i in 0 until length) {
            printWriter.write("\t")
        }
        printWriter.flush()
    }

    @Throws(IOException::class)
    fun setLineGap(gap: Int) {
        printWriter.write(0x1B)
        printWriter.write(0x33)
        printWriter.write(gap)
        printWriter.flush()
    }

    /* Set text alignment
           * @param bold  0:OFF  1:ON
     * @throws IOException
     */
    @Throws(IOException::class)
    fun setBold(bold: Int) {
        printWriter.write(0x1b)
        printWriter.write(0x45)
        printWriter.write(bold)
        printWriter.flush()
    }

    /* Set text alignment
           * @param bold  00:Normal text  10:Double height text  20:Double width text  30:Quad area text
     * @throws IOException
     */
    @Throws(IOException::class)
    fun setSize(size: Int) {
        printWriter.write(0x1b)
        printWriter.write(0x21)
        printWriter.write(size)
        printWriter.flush()
    }

    /* Set underline font
           * @param under  0:Underline font OFF  1:Underline font 1-dot ON  2:Underline font 2-dot ON
     * @throws IOException
     */
    fun setUnderline(under: Int) {
        printWriter.write(0x1b)
        printWriter.write(0x2d)
        printWriter.write(under)
        printWriter.flush()
    }

    /* Set text font type
           * @param under  01:Font type A  48:Font type B
     * @throws IOException
     */
    fun setFontType(type: Int) {
        printWriter.write(0x1b)
        printWriter.write(0x4d)
        printWriter.write(type)
        printWriter.flush()
    }

    /**
     * Print two columns
     *
     * @param leftText  Left text
     * @param rightText Right text
     * @return
     */
    fun printTwoData(leftText: String, rightText: String) {
        val sb = StringBuilder()
        val leftTextLength = getBytesLength(leftText)
        val rightTextLength = getBytesLength(rightText)
        sb.append(leftText)

        // Calculate the space between the text on both sides
        val marginBetweenMiddleAndRight: Int = LINE_BYTE_SIZE - leftTextLength - rightTextLength
        for (i in 0 until marginBetweenMiddleAndRight) {
            sb.append(" ")
        }
        sb.append(rightText)
        patchText(sb.toString())
    }

    /**
     * Print three columns
     *
     * @param leftText   Left text
     * @param middleText Middle text
     * @param rightText  Right text
     * @return
     */
    fun printThreeData(leftText: String, middleText: String, rightText: String) {
        var leftText = leftText
        val sb = StringBuilder()
        // At most LEFT_TEXT_MAX_LENGTH Chinese characters + two dots are displayed on the left
        if (leftText.length > LEFT_TEXT_MAX_LENGTH) {
            leftText = leftText.substring(0, LEFT_TEXT_MAX_LENGTH) + ".."
        }
        val leftTextLength = getBytesLength(leftText)
        val middleTextLength = getBytesLength(middleText)
        val rightTextLength = getBytesLength(rightText)
        sb.append(leftText)
        // Calculate the length of the space between the left text and the middle text
        val marginBetweenLeftAndMiddle: Int = LEFT_LENGTH - leftTextLength - middleTextLength / 2
        for (i in 0 until marginBetweenLeftAndMiddle) {
            sb.append(" ")
        }
        sb.append(middleText)

        // Calculate the length of the space between the right text and the middle text
        val marginBetweenMiddleAndRight: Int = RIGHT_LENGTH - middleTextLength / 2 - rightTextLength
        for (i in 0 until marginBetweenMiddleAndRight) {
            sb.append(" ")
        }

        // When printing, I found that the rightmost text is always one character to the right, so a space needs to be deleted
        sb.delete(sb.length - 1, sb.length).append(rightText)
        patchText(sb.toString())
    }

    /**
     * Get data length
     *
     * @param msg
     * @return
     */
    fun getBytesLength(msg: String): Int {
        return msg.toByteArray(Charsets.ISO_8859_1).size
    }

    fun beginListenData(){
           try {
               val handler: Handler = Handler()
                stopWorker = false
                readBufferPosition = 0
                readBuffer = ByteArray(1024)
                thread = Thread {
                    while (!Thread.currentThread().isInterrupted && !stopWorker) {
                        try {
                            val byteAvailable = inputStream.available()
                            if (byteAvailable > 0) {
                                val packetByte = ByteArray(byteAvailable)
                                val byteArray = byteArrayOf(0, 0)
                                if(packetByte.contentEquals(byteArray)){
                                handler.post {
                                    isConnected.value = false
                                    closeBluetoothPrinter()
                                    }
                                }
                                inputStream.read(packetByte)
                            }

                        } catch (ex: Exception) {
                            stopWorker = true
                        }
                    }
                }
                thread.start()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
}