package com.example.uosense.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uosense.R
import com.example.uosense.models.RestaurantListResponse

enum class RestaurantViewType {
    USER_VIEW, CONTROL_VIEW
}

class RestaurantListAdapter(
    private var restaurantList: MutableList<RestaurantListResponse>,
    private val onItemClick: (RestaurantListResponse) -> Unit,
    private val onDeleteClick: (RestaurantListResponse) -> Unit,
    private val viewType: RestaurantViewType
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val restaurantName: TextView = itemView.findViewById(R.id.restaurantName)
        private val restaurantCategory: TextView = itemView.findViewById(R.id.restaurantCategory)
        private val restaurantAddress: TextView = itemView.findViewById(R.id.restaurantAddress)
        private val restaurantRating: TextView = itemView.findViewById(R.id.restaurantRating)
        private val restaurantImage: ImageView = itemView.findViewById(R.id.restaurantImage)

        fun bind(restaurant: RestaurantListResponse) {
            restaurantName.text = restaurant.name
            restaurantCategory.text = restaurant.category
            restaurantAddress.text = restaurant.address
            restaurantRating.text = restaurant.rating.toString()

            Glide.with(itemView.context)
                .load(restaurant.restaurantImage)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.ic_uos)
                .into(restaurantImage)

            itemView.setOnClickListener {
                onItemClick(restaurant)
            }
        }
    }

    inner class ControlViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val restaurantName: TextView = itemView.findViewById(R.id.restaurantName)
        private val restaurantCategory: TextView = itemView.findViewById(R.id.restaurantCategory)
        private val restaurantAddress: TextView = itemView.findViewById(R.id.restaurantAddress)
        private val restaurantRating: TextView = itemView.findViewById(R.id.restaurantRating)
        private val restaurantImage: ImageView = itemView.findViewById(R.id.restaurantImage)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(restaurant: RestaurantListResponse) {
            restaurantName.text = restaurant.name
            restaurantCategory.text = restaurant.category
            restaurantAddress.text = restaurant.address
            restaurantRating.text = restaurant.rating.toString()

            Glide.with(itemView.context)
                .load(restaurant.restaurantImage)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.ic_uos)
                .into(restaurantImage)

            itemView.setOnClickListener {
                onItemClick(restaurant)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(restaurant)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == RestaurantViewType.USER_VIEW.ordinal) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user_restaurant, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_control_restaurant, parent, false)
            ControlViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val restaurant = restaurantList[position]

        if (holder is UserViewHolder) {
            holder.bind(restaurant)
        } else if (holder is ControlViewHolder) {
            holder.bind(restaurant)
        }
    }

    override fun getItemCount(): Int = restaurantList.size

    override fun getItemViewType(position: Int): Int {
        return viewType.ordinal
    }

    fun updateList(newList: List<RestaurantListResponse>) {
        restaurantList.clear()
        restaurantList.addAll(newList)
        notifyDataSetChanged()
    }
}
