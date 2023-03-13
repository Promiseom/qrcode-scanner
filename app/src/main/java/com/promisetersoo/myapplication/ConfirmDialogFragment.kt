package com.promisetersoo.myapplication

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

/**
 * A dialog to show the app information
 */
class ConfirmDialogFragment(title: String? = null, message: String,
                            listener: DialogInterface.OnClickListener,
                            nListener: DialogInterface.OnClickListener? = null): DialogFragment(){
    private var _title: String?
    private var _message: String
    private var _listener: DialogInterface.OnClickListener
    private var _nListener: DialogInterface.OnClickListener?

    init {
        _title = title
        _message = message
        _listener = listener
        _nListener = nListener
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(_title)
            .setMessage(_message)
            .setPositiveButton(getString(R.string.cont), _listener)
            .setNegativeButton(getString(R.string.cancel), _nListener)
            .create()
    companion object {
        const val TAG = "ConfirmDialogFragment"
    }
}