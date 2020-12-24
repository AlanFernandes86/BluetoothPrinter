package br.com.espacobistro.bluetoothprinter.ui.title

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import br.com.espacobistro.bluetoothprinter.R

class TitleFragment : Fragment() {

    private lateinit var viewModel: TitleViewModel
    lateinit var args: TitleFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.title_fragment, container, false)

        val text = root.findViewById(R.id.text_hello) as TextView
        val button = root.findViewById<Button>(R.id.button_getPrinter)!!

        if(!requireArguments().isEmpty){
            args = TitleFragmentArgs.fromBundle(requireArguments())
            text.text = args.deviceAddress
        }


        button.setOnClickListener {
            findNavController().navigate(TitleFragmentDirections.actionTitleFragmentToSelectDeviceFragment())
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TitleViewModel::class.java)
        // TODO: Use the ViewModel
    }





}