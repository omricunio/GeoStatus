package com.omric.geostatus.ui.status_view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.omric.geostatus.classes.Status
import com.omric.geostatus.databinding.StatusViewBinding
import com.squareup.picasso.Picasso

class StatusViewFragment() : Fragment() {

    private var _binding: StatusViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val status = StatusViewFragmentArgs.fromBundle(requireArguments()).status
        _binding = StatusViewBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.fullStatusNameTextView.text = status.name
        binding.fullStatusCreatorTextView.text = status.creator

        val storage = Firebase.storage.reference
        val imageRef = storage.child(status.imagePath)
        imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
            Picasso.get().load(imageUrl).into(binding.fullStatusImageView);
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}