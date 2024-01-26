package com.omric.geostatus.ui.maps

import android.content.ContentValues.TAG
import android.content.Context
import android.nfc.Tag
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.omric.geostatus.R
import com.squareup.picasso.Picasso

class InfoWindowAdapter(private val myContext: FragmentActivity) : GoogleMap.InfoWindowAdapter {
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

        val storage = Firebase.storage.reference
        val imageRef = storage.child(marker.snippet!!)
        imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
            Picasso.get().load(imageUrl).into(imageView);
            title.text = marker.title
            subTitle.text = "dadadaddadadasdada"
        }

        return view
    }
}