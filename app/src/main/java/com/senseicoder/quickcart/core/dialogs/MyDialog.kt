package com.senseicoder.quickcart.core.dialogs

import android.app.AlertDialog
import android.content.Context

class MyDialog {
    fun showAlertDialog(message: String,context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }

    fun showAlertDialog(message: String, context: Context, callback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                callback(true)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                callback(false)
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }



}