package com.promisetersoo.myapplication

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class AboutDialog: DialogFragment() {
    companion object{
        const val TAG = "AboutDialogFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            val builder = AlertDialog.Builder(it)
            val inflater = it.layoutInflater
            val view = inflater.inflate(R.layout.help_dialog_layout, null)
            builder.setView(view)
                .setPositiveButton(getString(R.string.ok)){_,_->
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}