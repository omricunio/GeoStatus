package com.omric.geostatus.ui.maps

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.omric.geostatus.R

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
        if (marker.isInfoWindowShown
        ) {
//            marker.hideInfoWindow()
//            marker.showInfoWindow()
        }
        return null
    }

    override fun getInfoWindow(marker: Marker): View {
        val title = marker.title
        val titleUi = view.findViewById(R.id.title) as TextView
        if (title != null) {
            titleUi.text = title
        } else {
            titleUi.text = ""
            titleUi.visibility = View.GONE
        }
        val snippet = marker.snippet
        val snippetUi = view
            .findViewById(R.id.snippet) as TextView
        if (snippet != null) {
            snippetUi.text = "YOYO"
        } else {
            snippetUi.text = ""
        }
        return view
    }
}