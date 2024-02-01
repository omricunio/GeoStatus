package com.omric.geostatus.ui.profile

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.omric.geostatus.R
import com.omric.geostatus.classes.Location
import com.omric.geostatus.classes.Status
import com.omric.geostatus.databinding.FragmentProfileBinding
import com.omric.geostatus.ui.login.LoginActivity
import com.omric.geostatus.utils.ImageUtils
import com.squareup.picasso.Picasso
import java.util.UUID


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageUtils: ImageUtils

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val user = Firebase.auth.currentUser
        binding.nameTextView.text = user!!.displayName

        if(user.photoUrl != null) {
            Picasso.get().load(user.photoUrl).into(binding.imageView);
        }

        imageUtils = ImageUtils(this)

        binding.editProfileButton.setOnClickListener {
            updateProfile()
        }

        binding.logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        fetchStatuses()

        return root
    }

    fun fetchStatuses() {
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

                val customAdapter = StatusAdapter(statuses.toTypedArray(), { status ->
                    val action = ProfileFragmentDirections.actionNavigationProfileToStatusViewFragment(status)
                    findNavController().navigate(action)
                }, { status ->
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)

                    builder
                        .setItems(arrayOf("edit", "delete" )) { dialog: DialogInterface, which: Int ->
                            when(which) {
                                1 -> {
                                    if(status.id != null){
                                        db.collection("statuses").document(status.id).delete().addOnSuccessListener {
                                            fetchStatuses()
                                        }
                                    }
                                }
                                2 -> {
                                }
                            }
                        }

                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                })

                val recyclerView: RecyclerView = binding.profileStatusRecyclerView
                val llm = LinearLayoutManager(requireContext())
                llm.orientation = LinearLayoutManager.VERTICAL
                recyclerView.layoutManager = llm
                recyclerView.adapter = customAdapter

            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load statuses",
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }

    fun updateProfile() {
        imageUtils.captureImage() {
                imageUrl ->
            val user = Firebase.auth.currentUser!!
            val storageRef = Firebase.storage.reference

            val imageRef = storageRef.child("profiles/${UUID.randomUUID()}")
            val uploadTask = imageRef.putFile(imageUrl)
            uploadTask.addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Failed to upload profile picture",
                    Toast.LENGTH_SHORT,
                ).show()
            }.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uploadedUrl ->
                    user.updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(uploadedUrl).build()).addOnSuccessListener {
                        Picasso.get().load(uploadedUrl).into(binding.imageView);
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}