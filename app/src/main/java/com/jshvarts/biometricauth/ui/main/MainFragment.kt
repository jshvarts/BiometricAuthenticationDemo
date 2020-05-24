package com.jshvarts.biometricauth.ui.main

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jshvarts.biometricauth.R
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_fragment.*

private const val VALID_USERNAME = "username"
private const val VALID_PASSWORD = "password"

class MainFragment : Fragment() {

    private val authenticationCallback = @RequiresApi(Build.VERSION_CODES.P)
    object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            // Called when a biometric is recognized.
            onSuccessfulLogin()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            // Called when an unrecoverable error has been encountered and the operation is complete.
            Snackbar.make(container, R.string.authentication_error_text, Snackbar.LENGTH_LONG)
                .show()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            // Called when a biometric is valid but not recognized.
            Snackbar.make(container, R.string.authentication_failed_text, Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var biometricPrompt: BiometricPrompt

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_prompt_title))
            .setDescription(getString(R.string.biometric_prompt_description))
            .setDeviceCredentialAllowed(true) // user can choose to use device pin in the biometric prompt
            .build()

        biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(context),
            authenticationCallback
        )

        authenticateButton.setOnClickListener {
            onAuthenticationRequested()
        }
    }

    private fun onSuccessfulLogin() {
        println("successful login")
        authenticateButton?.text = getString(R.string.logged_in)
    }

    private fun onAuthenticationRequested() {
        when (BiometricManager.from(requireContext()).canAuthenticate()) { // biometrics available
            BiometricManager.BIOMETRIC_SUCCESS -> {
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> requestLoginCredentials()
        }
    }

    private fun requestLoginCredentials() {
        showLoginDialog { username, password ->
            // validate login credentials. in the meantime, assume valid credentials are a hardcoded combo
            if (username == VALID_USERNAME && password == VALID_PASSWORD) {
                onSuccessfulLogin()
            } else {
                requestLoginCredentials()
            }
        }.show()
    }

    private fun showLoginDialog(
        onPositiveClicked: (username: String, password: String) -> Unit
    ): AlertDialog {
        val view = View.inflate(requireContext(), R.layout.alert_dialog_login, null)
        val usernameEditTextView: EditText = view.findViewById(R.id.username_input_edit_text)
        val passwordEditTextView: EditText = view.findViewById(R.id.password_input_edit_text)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_login_title)
            .setView(view)
            .setPositiveButton(R.string.dialog_login_positive_button) { _, _ ->
                onPositiveClicked(
                    usernameEditTextView.text.toString(),
                    passwordEditTextView.text.toString()
                )
            }
            .setNegativeButton(R.string.dialog_login_negative_button) { _, _ -> }
            .setCancelable(false)
            .create()
    }
}
