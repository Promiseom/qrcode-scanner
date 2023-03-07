package com.promisetersoo.myapplication

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.promisetersoo.myapplication.databinding.FragmentFirstBinding
import androidx.fragment.app.DialogFragment

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnInfo.setOnClickListener{
            InfoDialogFragment("App Info", getString(R.string.app_info)).show(childFragmentManager, InfoDialogFragment.TAG)
        }

        binding.btnScan.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * A dialog to show the app information
 */
class InfoDialogFragment(title: String? = null, message: String): DialogFragment(){
    private var _title: String?
    private var _message: String

    init {
        _title = title
        _message = message
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(_title)
            .setMessage(_message)
            .setPositiveButton(getString(R.string.ok)){_,_ -> }
            .create()

    companion object {
        const val TAG = "InfoDialogFragment"
    }
}