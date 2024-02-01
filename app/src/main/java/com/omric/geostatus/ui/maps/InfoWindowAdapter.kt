package com.omric.geostatus.ui.maps

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.nfc.Tag
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.omric.geostatus.R
import com.omric.geostatus.classes.Status
import com.omric.geostatus.ui.live_map.LiveMapFragmentDirections
import com.omric.geostatus.ui.profile.ProfileFragmentDirections
import com.squareup.picasso.Picasso

class StatusMapBubble(val name: String, val creatorName: String, val image: Bitmap, val originalStatus: Status) {}

class InfoWindowAdapter(private val myContext: FragmentActivity, private val markerToStatus: HashMap<Marker, StatusMapBubble>) : GoogleMap.InfoWindowAdapter {
    private val view: View

    init {
        val inflater =
            myContext.applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(
            R.layout.info_window,
            null
        )
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker): View {
        val title = view.findViewById(R.id.title) as TextView
        val subTitle = view.findViewById(R.id.subTitle) as TextView
        val imageView = view.findViewById(R.id.windowIcon) as ImageView

        val bubble = markerToStatus[marker]
        if(bubble === null) {
            return view
        }

        imageView.setImageBitmap(bubble.image)
        title.text = bubble.name
        subTitle.text = bubble.creatorName

        return view
    }

}