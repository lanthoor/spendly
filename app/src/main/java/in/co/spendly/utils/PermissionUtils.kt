package `in`.co.spendly.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Utility object for handling runtime permissions.
 */
object PermissionUtils {

    /**
     * Checks if the app has camera permission.
     *
     * @param context Application context
     * @return true if permission is granted
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Gets the camera permission string for requesting.
     */
    const val CAMERA_PERMISSION = Manifest.permission.CAMERA

    /**
     * Checks if the app has SMS read and receive permissions.
     *
     * @param context Application context
     * @return true if both permissions are granted
     */
    fun hasSmsPermissions(context: Context): Boolean {
        val hasReadSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val hasReceiveSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED

        return hasReadSms && hasReceiveSms
    }

    /**
     * Gets the SMS permission strings for requesting.
     */
    val SMS_PERMISSIONS = arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS
    )
}
