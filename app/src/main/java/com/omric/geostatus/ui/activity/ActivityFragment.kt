package com.omric.geostatus.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import com.omric.geostatus.R
import com.omric.geostatus.databinding.FragmentActivityBinding
import java.io.File

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
            checkPermissions()
        }

        return root
    }

    private fun shotAndUpload() {
        photoContract.launch(imageUrl)
    }

    private val cameraPermissionResultReceiver = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permitted ->
        if (permitted) {
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
            == PackageManager.PERMISSION_DENIED
        ) {
            cameraPermissionResultReceiver.launch(Manifest.permission.CAMERA)
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
                uploadStatus(imageUrl)
            }
            .setNegativeButton("Cancel", null)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun uploadStatus(imageUrl: Uri) {
        val database = Firebase.database.reference
        database.child("users").setValue("dfsgdgd")

        val storageRef = Firebase.storage.reference
        var file = imageUrl
        val riversRef = storageRef.child("images/${Firebase.auth.currentUser!!.uid}")
        val uploadTask = riversRef.putFile(file)

        uploadTask.addOnFailureListener {
            Toast.makeText(
                requireContext(),
                "Failed to upload status",
                Toast.LENGTH_SHORT,
            ).show()
        }.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(
                requireContext(),
                "Status was successfully uploaded",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}