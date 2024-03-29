package com.omric.geostatus.ui.profile

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.omric.geostatus.classes.Location
import com.omric.geostatus.classes.Status
import com.omric.geostatus.databinding.FragmentProfileBinding
import com.omric.geostatus.room.StatusDBs
import com.omric.geostatus.room.StatusRoom
import com.omric.geostatus.ui.login.LoginActivity
import com.omric.geostatus.utils.CustomAlerts
import com.omric.geostatus.utils.ImageUtils
import com.omric.geostatus.utils.Toaster
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageUtils: ImageUtils
    private lateinit var statusRoom: StatusRoom
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        statusRoom = StatusRoom(requireContext(), StatusDBs.ProfileStatuses)
        CoroutineScope(Dispatchers.IO).launch {
            val statuses = statusRoom.getStatuses()
            if(statuses.isEmpty()) {
                return@launch
            }
            CoroutineScope(Dispatchers.Main).launch {
                setupAdapter(statuses.toTypedArray())
            }
        }

        val user = Firebase.auth.currentUser
        binding.nameTextView.text = user!!.displayName

        if(user.photoUrl != null) {
            Picasso.get().load(user.photoUrl).into(binding.imageView);
        }

        imageUtils = ImageUtils(this)

        binding.editProfileButton.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder
                .setItems(arrayOf("Edit profile picture", "Edit profile name")) { dialog: DialogInterface, which: Int ->
                    when (which) {
                        0 -> { this.updateProfilePicture() }
                        1 -> { this.updateName() }
                    }
                }
            builder.show()
        }

        binding.logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        fetchStatuses()

        return root
    }

    private fun onStatusLongClick(status: Status) {
        val db = Firebase.firestore
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder
            .setItems(arrayOf("Edit name", "Edit picture", "Delete" )) { dialog: DialogInterface, which: Int ->
                when(which) {
                    0 -> {
                        CustomAlerts().openTextAlert(requireContext(), "Edit your status", status.name, "Confirm", "Cancel") {
                                input ->
                            db.collection("statuses").document(status.id!!).update("name", input).addOnSuccessListener {
                                Toaster().show(requireContext(), "Successfully updated status name")
                                fetchStatuses()
                            }
                        }
                    }
                    1 -> {
                        imageUtils.captureImage { imageUrl ->
                            val storageRef = Firebase.storage.reference
                            val imageRef = storageRef.child("images/${UUID.randomUUID()}")
                            val uploadTask = imageRef.putFile(imageUrl)
                            uploadTask.addOnSuccessListener { taskSnapshot ->
                                db.collection("statuses").document(status.id!!).update("imagePath", taskSnapshot.metadata!!.path).addOnSuccessListener {
                                    Toaster().show(requireContext(), "Successfully updated status picture")
                                    fetchStatuses()
                                }
                            }
                        }
                    }
                    2 -> {
                        db.collection("statuses").document(status.id!!).delete().addOnSuccessListener {
                            Toaster().show(requireContext(), "Successfully removed status")
                            fetchStatuses()
                        }
                    }
                }
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun fetchStatuses() {
        val user = Firebase.auth.currentUser!!
        val db = Firebase.firestore
        db.collection("statuses")
            .whereEqualTo("creator", user.uid)
            .get()
            .addOnSuccessListener { result ->
                val statuses = mutableListOf<Status>()
                for (document in result) {
                    val itemData = document.data

                    val name = itemData["name"] as? String
                    val date = itemData["date"] as? String
                    val imagePath = itemData["imagePath"] as? String
                    val creator = itemData["creator"] as? String
                    val loc = itemData["location"] as HashMap<*, *>
                    val location = Location(loc["latitude"] as Double, loc["longitude"] as Double)

                    if(!(name.isNullOrEmpty() || date.isNullOrEmpty() || imagePath.isNullOrEmpty() || creator.isNullOrEmpty())) {
                        val item = Status(name, date, imagePath, creator, location, document.id)
                        statuses.add(item)
                    }
                }

                CoroutineScope(Dispatchers.IO).launch {
                    statusRoom.insertStatuses(statuses.toTypedArray())
                }

                setupAdapter(statuses.toTypedArray())
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load statuses",
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }

    private fun setupAdapter(statuses: Array<Status>) {
        val customAdapter = StatusAdapter(statuses, { status ->
            val action = ProfileFragmentDirections.actionNavigationProfileToStatusViewFragment(status)
            findNavController().navigate(action)
        }, { status ->
            onStatusLongClick(status)
        })

        val recyclerView: RecyclerView = binding.profileStatusRecyclerView
        val llm = LinearLayoutManager(requireContext())
        llm.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = llm
        recyclerView.adapter = customAdapter
    }

    private fun updateProfilePicture() {
        imageUtils.captureImage() {
                imageUrl ->
            val user = Firebase.auth.currentUser!!
            val storageRef = Firebase.storage.reference

            val imageRef = storageRef.child("profiles/${UUID.randomUUID()}")
            val uploadTask = imageRef.putFile(imageUrl)
            uploadTask.addOnFailureListener {
                Toaster().show(requireContext(), "Failed to upload profile picture")
            }.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uploadedUrl ->
                    user.updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(uploadedUrl).build()).addOnSuccessListener {
                        Toaster().show(requireContext(), "Successfully updated profile picture")
                        Picasso.get().load(uploadedUrl).into(binding.imageView);
                    }
                }
            }
        }
    }

    private fun updateName() {
        CustomAlerts().openTextAlert(requireContext(), "Update profile name", "", "Confirm", "Cancel") {
            input ->
            val user = Firebase.auth.currentUser!!
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(input).build()).addOnSuccessListener {
                val usersCollection = Firebase.firestore.collection("users")
                usersCollection.add(hashMapOf("uid" to user.uid, "name" to input)).addOnSuccessListener {
                    binding.nameTextView.text = input
                    Toaster().show(requireContext(), "Successfully updated profile name")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}