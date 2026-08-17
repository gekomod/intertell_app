package pl.intertell.client.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK

/** True only when the device actually has a usable fingerprint/face enrolled — gates the Ustawienia toggle. */
fun isBiometricAvailable(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

/**
 * Shows the OS fingerprint/face prompt. onError fires for both a genuine
 * failure and a plain user cancel — the caller (ClientViewModel) treats
 * either the same way: fall back to the normal password login screen,
 * nothing about the stored session is touched either way.
 */
fun authenticateBiometric(activity: FragmentActivity, onSuccess: () -> Unit, onError: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onError()
            // A single wrong fingerprint isn't fatal — the system prompt stays open and lets the user retry.
            override fun onAuthenticationFailed() = Unit
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Odblokuj Intertell")
        .setSubtitle("Zaloguj się odciskiem palca lub inną metodą biometryczną")
        .setNegativeButtonText("Użyj hasła")
        .build()
    prompt.authenticate(info)
}
