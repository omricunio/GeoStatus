package com.omric.geostatus.ui.live_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.omric.geostatus.R
import com.omric.geostatus.databinding.FragmentLiveMapBinding
import com.omric.geostatus.ui.maps.InfoWindowAdapter


class LiveMapFragment : Fragment() {

    private var _binding: FragmentLiveMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val liveMapViewModel =
            ViewModelProvider(this).get(LiveMapViewModel::class.java)

        _binding = FragmentLiveMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        liveMapViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment;
        supportMapFragment.getMapAsync(OnMapReadyCallback { googleMap ->
            onMapLoad(googleMap)
        })

        return root
    }

    private fun onMapLoad(googleMap: GoogleMap) {
        googleMap.setInfoWindowAdapter(InfoWindowAdapter(requireActivity()))
        googleMap.setOnMapClickListener { latLng -> // When clicked on map
            // Initialize marker options
            val markerOptions = MarkerOptions()
            // Set position of marker
            markerOptions.position(latLng)
            // Set title of marker
            markerOptions.title(latLng.latitude.toString() + " : " + latLng.longitude)
            markerOptions.snippet("DDD")
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            // Remove all marker
            googleMap.clear()
            // Animating to zoom the marker
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
            // Add marker on map
            googleMap.addMarker(markerOptions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}