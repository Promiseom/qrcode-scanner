package com.promisetersoo.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.*
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.google.android.gms.net.CronetProviderInstaller
import org.chromium.net.CronetEngine
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.concurrent.Executors

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
    private var _cronetEngine: CronetEngine? = null
    private var _request: UrlRequest? = null

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
        startNetworkMonitor()
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
//                showToast("Scan Result: ${it.text}")
                _url = it.text
                val message = "Do you want to validate url: $_url?"
                // the user can choose to continue or cancel
                ConfirmDialogFragment(getString(R.string.confirm_title), message, { _, _ ->
//                    Toast.makeText(requireContext(), "Validating Url", Toast.LENGTH_LONG).show()
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

    private fun restartScannerPreview(){
        codeScanner.releaseResources()
        codeScanner.startPreview()
    }

    private fun validateUrl(url: String){
        // validation api
        val requestUrl = "https://promise.pythonanywhere.com/validate?url=$url"

        // check network connection
        if(!_isNetworkAvailable){
            showToast(getString(R.string.no_internet))
            restartScannerPreview()
            return
        }
        // update play services if required
        CronetProviderInstaller.installProvider(requireContext())

        if(_cronetEngine == null) {
            val builder = CronetEngine.Builder(requireContext())
            _cronetEngine = builder.build()
        }
        val executor = Executors.newSingleThreadExecutor()
        val requestBuilder = (_cronetEngine as CronetEngine).newUrlRequestBuilder(requestUrl, requestCallback, executor)
        _request = requestBuilder.build()
        _request?.start()
        Log.i(TAG, "Making request to $requestUrl")

        CustomLoadingDialog().setNegativeButtonListener{_,_->
            _request?.cancel()
            showToast("Validation cancelled")
            restartScannerPreview()
        }.show(childFragmentManager, CustomLoadingDialog.TAG)
    }

    private val requestCallback = object: UrlRequest.Callback(){
        private var buffer: ByteBuffer? = ByteBuffer.allocateDirect(102400)
        private var responseHeaders: Map<String, List<String>>? = null
        private var responseString: String? = null
        private val tag = "URLRequestCallback"

        override fun onRedirectReceived(request: UrlRequest?, info: UrlResponseInfo?, newLocationUrl: String?) {
            Log.i(tag, "onRedirectReceived")
            request?.followRedirect()
        }

        override fun onResponseStarted(request: UrlRequest?, info: UrlResponseInfo?) {
            Log.i(tag, "onResponseStarted called")
            val httpStatusCode = info?.httpStatusCode
            if (httpStatusCode == 200) {
                // The request was fulfilled. Start reading the response.
                request?.read(buffer)
            } else if (httpStatusCode == 503) {
                // The service is unavailable. You should still check if the request
                // contains some data.
                request?.read(buffer)
            }
            responseHeaders = info?.allHeaders
        }

        override fun onReadCompleted(request: UrlRequest?, info: UrlResponseInfo?, byteBuffer: ByteBuffer?) {
            Log.i(tag, "onReadCompleted called")
            // Continue reading the response body by reusing the same buffer
            // until the response has been completed.
            responseString = byteBuffer?.let{
                if(it.hasArray()){
                    // byte to ascii conversion
                    val sb = StringBuilder()
                    it.array().forEach{ b->
                        if(b in (1..127)){
                            sb.append(b.toInt().toChar())
                        }
                    }
                    sb.toString()
                }else{
                    null
                }
            }
            byteBuffer?.clear()
            request?.read(byteBuffer)
        }

        override fun onSucceeded(request: UrlRequest?, info: UrlResponseInfo?) {
            Log.i(tag, "onSucceeded called")
            // close the validation dialog
            val dialog: Fragment? = childFragmentManager.findFragmentByTag(CustomLoadingDialog.TAG)
            dialog?.let {
                (it as DialogFragment).dismiss()
            }
            responseString?.let {
                Log.d(tag, it)
                 val json = JSONObject(it)
                // show the result of the validation
                if(json.getBoolean("status")){
                    val data = json.getJSONObject("data")
                    val message = if(data.getBoolean("is_good")){
                        "${data.getString("url")} is Safe"
                    }else{
                        "${data.getString("url")} is Malicious"
                    }
                    // confirm if to visit the url
                    val cDialog = ConfirmDialogFragment("Result", message, {_,_->
                        var url = data.getString("url")
                        // determine if the url if properly formatted
                        if(!(url.startsWith("http://") || url.startsWith("https://"))){
                            url = "https://$url"
                        }
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(browserIntent)
                    })
                    cDialog.positiveButtonText = "Visit"
                    cDialog.show(childFragmentManager, InfoDialogFragment.TAG)
                }else{
                    // display the error message
                    InfoDialogFragment("Result", json.getString("message")).show(childFragmentManager, InfoDialogFragment.TAG)
                }
            }
            if(responseString == null){
                InfoDialogFragment("Invalid Response", "The request ended with an invalid server response")
                    .show(childFragmentManager, InfoDialogFragment.TAG)
            }
            restartScannerPreview()
        }

        override fun onFailed(request: UrlRequest?, info: UrlResponseInfo?, error: CronetException?) {
            Log.e(tag, "The request failed.", error)
            // close the validation dialog
            val dialog: Fragment? = childFragmentManager.findFragmentByTag(CustomLoadingDialog.TAG)
            dialog?.let{
                (it as DialogFragment).dismiss()
            }
            InfoDialogFragment("Error", "An error occurred while making the request")
                .show(childFragmentManager, InfoDialogFragment.TAG)
            restartScannerPreview()
        }

        override fun onCanceled(request: UrlRequest?, info: UrlResponseInfo?) {
            Log.i(tag, "onCanceled called")
            super.onCanceled(request, info)
            // free resources allocated to process this request
            _request = null
        }
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