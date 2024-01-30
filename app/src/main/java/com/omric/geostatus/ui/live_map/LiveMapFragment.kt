package com.omric.geostatus.ui.live_map

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.omric.geostatus.R
import com.omric.geostatus.classes.Location
import com.omric.geostatus.classes.Status
import com.omric.geostatus.databinding.FragmentLiveMapBinding
import com.omric.geostatus.ui.maps.InfoWindowAdapter
import com.omric.geostatus.ui.maps.StatusMapBubble
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.net.URL


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
            runBlocking {
                val statuses = liveMapViewModel.statuses.value
                if(!statuses.isNullOrEmpty()) {
                    launch {
                        val markerToStatus = mapStatuses(map, statuses)
                        map.setInfoWindowAdapter(InfoWindowAdapter(requireActivity(), markerToStatus))
                    }
                }
            }
        }

        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment;
        supportMapFragment.getMapAsync(OnMapReadyCallback { googleMap ->
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

    private suspend fun mapStatuses(googleMap: GoogleMap, statuses: MutableList<Status>): HashMap<Marker, StatusMapBubble> {
        val hasMap = HashMap<Marker, StatusMapBubble>()
        for (status in statuses) {
            val markerOptions = MarkerOptions()
            markerOptions.position(LatLng(status.location.latitude, status.location.longitude))
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            val storage = Firebase.storage.reference
            val imageRef = storage.child(status.imagePath)
            val imageUrl = imageRef.downloadUrl.await()

            val bitmap = GlobalScope.async {
                val bitmap = Picasso.get().load(imageUrl).get()
                bitmap
            }.await()

            val snapshot = Firebase.firestore.collection("users").whereEqualTo("uid", status.creator).get().await()
            val creatorName = snapshot.documents.first()["name"] as String
            val bubble = StatusMapBubble(status.name, creatorName, bitmap)
            val marker = googleMap.addMarker(markerOptions)!!
            hasMap[marker] = bubble
        }
        return hasMap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}