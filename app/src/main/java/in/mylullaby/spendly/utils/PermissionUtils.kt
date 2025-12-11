package `in`.mylullaby.spendly.utils

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
}
