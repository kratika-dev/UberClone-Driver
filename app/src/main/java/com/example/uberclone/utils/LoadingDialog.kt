package com.example.uberclone.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.example.uberclone.R

class LoadingDialog(context: Context) {

    private val dialog = Dialog(context)

    init {

        dialog.setContentView(
            LayoutInflater.from(context)
                .inflate(R.layout.dialog_loading, null)
        )

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setCancelable(false)
    }

    fun show() {
        if (!dialog.isShowing)
            dialog.show()
    }

    fun dismiss() {
        if (dialog.isShowing)
            dialog.dismiss()
    }
}