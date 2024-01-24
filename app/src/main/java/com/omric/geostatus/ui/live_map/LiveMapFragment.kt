package com.omric.geostatus.ui.live_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.omric.geostatus.R
import com.omric.geostatus.classes.Location
import com.omric.geostatus.classes.Status
import com.omric.geostatus.databinding.FragmentLiveMapBinding
import com.omric.geostatus.ui.maps.InfoWindowAdapter


class LiveMapFragment : Fragment() {

    private var _binding: FragmentLiveMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var liveMapViewModel: LiveMapViewModel
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        liveMapViewModel =
            ViewModelProvider(this).get(LiveMapViewModel::class.java)

        _binding = FragmentLiveMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        liveMapViewModel.statuses.observe(viewLifecycleOwner) {
            val statuses = liveMapViewModel.statuses.value
            if(!statuses.isNullOrEmpty()) {
                setStatusesOnMap(map, statuses)
            }
        }

        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment;
        supportMapFragment.getMapAsync(OnMapReadyCallback { googleMap ->
            googleMap.setInfoWindowAdapter(InfoWindowAdapter(requireActivity()))
            map = googleMap
            loadStatuses()
        })

        return root
    }

    private fun loadStatuses() {
        val db = Firebase.firestore
        db.collection("statuses")
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
                        val item = Status(name, date, imagePath, creator, location)
                        statuses.add(item)
                    }
                }
                liveMapViewModel.statuses.value = statuses
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load statuses",
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }

    private fun setStatusesOnMap(googleMap: GoogleMap, statuses: MutableList<Status>) {
        for (status in statuses) {
            val markerOptions = MarkerOptions()
            markerOptions.position(LatLng(status.location.latitude, status.location.longitude))
            markerOptions.title(status.name)
            markerOptions.snippet(status.imagePath)
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            googleMap.addMarker(markerOptions)
        }

//        googleMap.setOnMapClickListener { latLng -> // When clicked on map
//            // Initialize marker options
//            val markerOptions = MarkerOptions()
//            // Set position of marker
//            markerOptions.position(latLng)
//            // Set title of marker
//            markerOptions.title(latLng.latitude.toString() + " : " + latLng.longitude)
//            markerOptions.snippet("DDD")
//            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//            // Remove all marker
//            googleMap.clear()
//            // Animating to zoom the marker
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
//            // Add marker on map
//            googleMap.addMarker(markerOptions)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}