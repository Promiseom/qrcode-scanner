package com.promisetersoo.myapplication

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class CustomLoadingDialog: DialogFragment() {
    private var _negativeButtonListener: DialogInterface.OnClickListener? = null

    companion object{
        const val TAG = "CustomLoadingDialog"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            val builder = AlertDialog.Builder(it)
            val inflater = it.layoutInflater
            val view = inflater.inflate(R.layout.custom_dialog_loading, null)
            view.findViewById<TextView>(R.id.ldTitle).text = getString(R.string.validating_url)
            builder.setView(view)
                .setNegativeButton(getString(R.string.cancel), _negativeButtonListener)
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun setNegativeButtonListener(listener: DialogInterface.OnClickListener): CustomLoadingDialog{
        _negativeButtonListener = listener
        return this
    }
}