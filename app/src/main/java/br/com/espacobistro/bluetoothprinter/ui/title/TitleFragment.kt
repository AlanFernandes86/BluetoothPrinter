package br.com.espacobistro.bluetoothprinter.ui.title


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import br.com.espacobistro.bluetoothprinter.R
import br.com.espacobistro.bluetoothprinter.domain.demo.Demo


class TitleFragment : Fragment() {

    private lateinit var viewModel: TitleViewModel
    lateinit var args: TitleFragmentArgs

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(TitleViewModel::class.java)

        val root = inflater.inflate(R.layout.title_fragment, container, false)

        val progressBar = root.findViewById<ProgressBar>(R.id.title_progress)

        val toPrint = root.findViewById<EditText>(R.id.text_toPrint)

        val buttonPrint = root.findViewById<Button>(R.id.button_print)

        val buttonDemo = root.findViewById<Button>(R.id.button_demo)

        val buttonGet = root.findViewById<Button>(R.id.button_getPrinter)!!

        val lblStatus = root.findViewById<TextView>(R.id.lbl_status)


        if (!requireArguments().isEmpty) {
            args = TitleFragmentArgs.fromBundle(requireArguments())
            viewModel.initPrinterController(args.bluetoothDevice)

            val listDemo: List<Demo> = listOf(
                    Demo(1, "Tomate", 10, 5.9),
                    Demo(1, "Cenoura", 5, 2.9),
                    Demo(1, "Beterraba", 2, 3.9),
                    Demo(1, "Banana", 1, 2.9),
                    Demo(1, "Feijão", 12, 8.9),
                    Demo(1, "Leite", 12, 4.9),
                    Demo(1, "Cerveja", 10, 1.9),
                    Demo(1, "Arroz", 10, 4.9),
            )

            buttonDemo.setOnClickListener {
                viewModel.printDemo(listDemo)
            }

            buttonPrint.setOnClickListener {
                viewModel.print(toPrint.text.toString())
            }

            progressBar.visibility = VISIBLE

        }

        viewModel.isConnected.observe(viewLifecycleOwner, Observer {
            when (it) {
                true -> {
                    buttonDemo.isEnabled = true
                    buttonPrint.isEnabled = true
                    toPrint.isEnabled = true
                    lblStatus.text = "Printer connected"
                    progressBar.visibility = INVISIBLE
                    viewModel.clearIsConnected()
                }
                false -> {
                    buttonDemo.isEnabled = false
                    buttonPrint.isEnabled = false
                    toPrint.isEnabled = false
                    lblStatus.text = "Printer Disconnected"
                    Toast.makeText(requireContext(), "Falha ao conectar a impressora!", Toast.LENGTH_LONG).show()
                    progressBar.visibility = INVISIBLE
                    viewModel.clearIsConnected()
                }
                else -> {}
            }
        })

        buttonGet.setOnClickListener {
            findNavController().navigate(TitleFragmentDirections.actionTitleFragmentToSelectDeviceFragment())
        }

        return root
    }
}