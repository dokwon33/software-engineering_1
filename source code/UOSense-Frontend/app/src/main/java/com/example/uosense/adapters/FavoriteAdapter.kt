package com.example.uosense.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.R
import com.example.uosense.models.BookMarkResponse
import com.example.uosense.models.RestaurantInfo
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteAdapter(
    private var favorites: List<BookMarkResponse>
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val restaurantName: TextView = itemView.findViewById(R.id.restaurantName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favorite = favorites[position]

        // API 호출로 식당 이름 가져오기
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantById(favorite.restaurantId)
                if (response != null) {
                    holder.restaurantName.text = response.body()!!.name
                } else {
                    holder.restaurantName.text = "정보 없음"
                }
            } catch (e: Exception) {
                Toast.makeText(
                    holder.itemView.context,
                    "네트워크 오류 발생: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun getItemCount() = favorites.size

    fun updateData(newFavorites: List<BookMarkResponse>) {
        favorites = newFavorites
        notifyDataSetChanged()
    }
}
