package com.promisetersoo.myapplication

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.*
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.google.zxing.BarcodeFormat
import com.promisetersoo.myapplication.databinding.FragmentSecondBinding

import androidx.core.content.ContextCompat.getSystemService

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var cameraPreviewView: PreviewView
    private lateinit var codeScanner: CodeScanner
    private var _url: String? = null
    private var _isNetworkAvailable = false

    companion object{
      const val TAG = "Second Fragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(requireActivity(), scannerView)
//        startNetworkMonitor()
        requestPermissions()
    }

    /**
     * Requests all required permissions, request for permissions that have not been granted
     */
    private fun requestPermissions(){
        when{
            // check camera permission
            ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED->{
                    startCodeScanner()
            }
            else->{
                // request for permissions
                (activity as MainActivity).requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    /**
     * Monitors network availability
     */
    private fun startNetworkMonitor(){
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val cm = getSystemService(requireContext(), ConnectivityManager::class.java) as ConnectivityManager
        cm.requestNetwork(networkRequest, object: ConnectivityManager.NetworkCallback(){
            override fun onLost(network: Network) {
                super.onLost(network)
                _isNetworkAvailable = false
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                _isNetworkAvailable = true
            }
        })
    }

    private fun startCodeScanner(){
        val activity = requireActivity()

        // set default parameters
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = List(0){ BarcodeFormat.QR_CODE }
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        // callbacks
        codeScanner.decodeCallback = DecodeCallback{
            activity.runOnUiThread{
                showToast("Scan Result: ${it.text}")
                _url = it.text
                val message = "Do you want to validate url: $_url?"
                // the user can choose to continue or cancel
                ConfirmDialogFragment(getString(R.string.confirm_title), message, { _, _ ->
                    Toast.makeText(requireContext(), "Validating Url", Toast.LENGTH_LONG).show()
                    validateUrl(it.text)
                }, {_,_->
                    // restart camera preview to scan something else
                    codeScanner.releaseResources()
                    codeScanner.startPreview()
                }).show(childFragmentManager, ConfirmDialogFragment.TAG)
                showToast("Code Found")
            }
        }
        codeScanner.errorCallback = ErrorCallback{
            activity.runOnUiThread{
                showToast("Camera Initialization error")
            }
        }
        codeScanner.startPreview()
    }

    private fun validateUrl(url: String){
        // check network connection
        if(!_isNetworkAvailable){
            showToast(getString(R.string.no_internet))
        }
        CustomLoadingDialog().setNegativeButtonListener{_,_->
            showToast("Validation cancelled")
            codeScanner.releaseResources()
            codeScanner.startPreview()
        }.show(childFragmentManager, CustomLoadingDialog.TAG)
    }

    private fun showToast(message: String, length: Int = Toast.LENGTH_SHORT){
        Toast.makeText(requireContext(), message, length).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume(){
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}