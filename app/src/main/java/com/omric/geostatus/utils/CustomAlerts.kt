package com.omric.geostatus.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

class CustomAlerts {
    public fun openTextAlert(context: Context, title: String, placeHolder: String?, positiveButton: String, negativeButton: String, onConfirm: (input: String) -> Unit) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        val textInputLayout = TextInputLayout(context)
        textInputLayout.placeholderText = placeHolder
        textInputLayout.setPadding(
            19,
            0,
            19,
            19
        )
        val input = EditText(context)
        textInputLayout.addView(input)

        builder
            .setTitle(title)
            .setView(textInputLayout)
            .setPositiveButton(positiveButton) { dialog, which ->
                onConfirm(input.text.toString())
            }
            .setNegativeButton(negativeButton, null)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}