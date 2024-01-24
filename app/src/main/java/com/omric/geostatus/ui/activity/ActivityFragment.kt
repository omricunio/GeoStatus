package com.omric.geostatus.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.google.type.LatLng
import com.omric.geostatus.R
import com.omric.geostatus.classes.Location
import com.omric.geostatus.classes.Status
import com.omric.geostatus.databinding.FragmentActivityBinding
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var imageUrl: Uri

    private val photoContract = registerForActivityResult(ActivityResultContracts.TakePicture()) { approved ->
        if(approved) {
            buildStatus(imageUrl)
        }
    }

    private fun createImageUri() : Uri {
        val image = File(requireContext().filesDir, "camera_photos.png")
        return FileProvider.getUriForFile(requireContext(), "com.omric.geostatus.FileProvider", image)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activityViewModel =
            ViewModelProvider(this).get(ActivityViewModel::class.java)

        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        val root: View = binding.root

        imageUrl = createImageUri();

        binding.uploadButton.setOnClickListener {
            val database = Firebase.database.reference
            checkPermissions()
        }

        return root
    }

    private fun shotAndUpload() {
        photoContract.launch(imageUrl)
    }

    private val permissionResultReceiver = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permitted ->
        if (permitted.values.all { permission -> permission }) {
            shotAndUpload()
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permissions denied",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_DENIED
        ) {
            permissionResultReceiver.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION))
        } else {
            shotAndUpload()
        }
    }

    private fun buildStatus(imageUrl: Uri) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        val textInputLayout = TextInputLayout(requireContext())
        textInputLayout.setPadding(
            resources.getDimensionPixelOffset(R.dimen.dp_19), // if you look at android alert_dialog.xml, you will see the message textview have margin 14dp and padding 5dp. This is the reason why I use 19 here
            0,
            resources.getDimensionPixelOffset(R.dimen.dp_19),
            resources.getDimensionPixelOffset(R.dimen.dp_19)
        )
        val input = EditText(context)
        textInputLayout.addView(input)

        builder
            .setTitle("Your new status name")
            .setView(textInputLayout)
            .setPositiveButton("Upload") { dialog, which ->
                uploadStatus(input.text.toString(), imageUrl)
            }
            .setNegativeButton("Cancel", null)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }
    @SuppressLint("MissingPermission")
    private fun uploadStatus(name: String, imageUrl: Uri) {
        binding.activityProgressBar.isVisible = true
        binding.activityPlaceholder.isVisible = false


        val database = Firebase.firestore
        val storageRef = Firebase.storage.reference
        val user = Firebase.auth.currentUser
        val statusesCollection = database.collection("statuses")

        val imageRef = storageRef.child("images/${UUID.randomUUID()}")
        val uploadTask = imageRef.putFile(imageUrl)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { location ->
                uploadTask.addOnFailureListener {
                    onUploadError()
                }.addOnSuccessListener { taskSnapshot ->
                    val status = Status(name, getCurrentDate(), taskSnapshot.metadata!!.path, user!!.uid, Location(location.latitude, location.longitude))
                    statusesCollection.add(status).addOnFailureListener {
                        onUploadError()
                    }.addOnSuccessListener {
                        onUploadSuccess(status, imageUrl.toString())
                    }
                }

        }
    }

    private fun onUploadError() {
        binding.activityProgressBar.isVisible = false
        Toast.makeText(
            requireContext(),
            "Failed to upload status",
            Toast.LENGTH_SHORT,
        ).show()
    }

    private fun onUploadSuccess(status: Status, localImageUrl: String) {
        binding.activityProgressBar.isVisible = false
        binding.activityImageView.isVisible = true
        binding.activityImageView.setImageURI(imageUrl)
        binding.activityName.text = status.name
        Toast.makeText(
            requireContext(),
            "Status was successfully uploaded",
            Toast.LENGTH_SHORT,
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}