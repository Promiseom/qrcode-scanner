package com.promisetersoo.myapplication

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class InfoDialogFragment(title: String, message: String): DialogFragment(){
    var _title: String?
    var _message: String

    init {
        _title = title
        _message = message
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog.Builder(requireContext())
        .setTitle(_title)
        .setMessage(_message)
        .setPositiveButton(getString(R.string.ok)) { _, _ -> }
        .create()

    companion object {
        const val TAG = "InfoDialogFragment"
    }
}
