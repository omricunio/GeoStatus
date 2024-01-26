package com.omric.geostatus.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.omric.geostatus.R
import com.omric.geostatus.classes.Status
import com.squareup.picasso.Picasso
class CustomAdapter(private val dataSet: Array<Status>, private val onItemClicked: (Status) -> Unit) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView
        val statusImageView: ImageView

        init {
            // Define click listener for the ViewHolder's View
            nameTextView = view.findViewById(R.id.statusNameList)
            statusImageView = view.findViewById(R.id.statusImageViewList)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.status_row, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.itemView.setOnClickListener { onItemClicked(dataSet[position]) }
        viewHolder.nameTextView.text = dataSet[position].name
        val storage = Firebase.storage.reference
        val imageRef = storage.child(dataSet[position].imagePath)
        imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
            Picasso.get().load(imageUrl).into(viewHolder.statusImageView);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}