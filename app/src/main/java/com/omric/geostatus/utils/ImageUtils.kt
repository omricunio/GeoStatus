package com.omric.geostatus.utils

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File

class ImageUtils(fragment: Fragment) {
    var onImageCapture: (imageUrl: Uri) -> Unit = {}
    private val imageUrl: Uri

    private val photoContract: ActivityResultLauncher<Uri>
    private val permissionResultReceiver: ActivityResultLauncher<Array<String>>
    private val fragment: Fragment

    init {
        this.fragment = fragment
        val image = File(fragment.requireContext().filesDir, "camera_photos.png")
        imageUrl = FileProvider.getUriForFile(fragment.requireContext(), "com.omric.geostatus.FileProvider", image)

        photoContract = fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { approved ->
            if(approved) {
                onImageCapture(imageUrl)
            }
        }
        permissionResultReceiver = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permitted ->
            if (permitted.values.all { permission -> permission }) {
                photoContract.launch(imageUrl)
            } else {
                Toast.makeText(
                    fragment.requireContext(),
                    "Camera permissions denied",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    fun captureImage(onFinish: (imageUrl: Uri) -> Unit) {
        onImageCapture = onFinish
        if (ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_DENIED
        ) {
            permissionResultReceiver.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION))
        } else {
            photoContract.launch(imageUrl)
        }
    }
}