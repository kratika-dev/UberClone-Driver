package com.example.uberclone.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

object SnackbarUtils {

    private var snackbar: Snackbar? = null

    fun showNoInternet(rootView: View) {

        if (snackbar?.isShown == true) return

        snackbar = Snackbar.make(
            rootView,
            "No Internet Connection",
            Snackbar.LENGTH_INDEFINITE
        )

        snackbar?.show()
    }

    fun hideNoInternet() {
        snackbar?.dismiss()
        snackbar = null
    }

    fun showBackOnline(rootView: View) {
        Snackbar.make(
            rootView,
            "Back Online",
            Snackbar.LENGTH_SHORT
        ).show()
    }
}