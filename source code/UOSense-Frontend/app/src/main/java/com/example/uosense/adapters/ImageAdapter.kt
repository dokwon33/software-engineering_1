package com.example.uosense.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.services.s3.AmazonS3Client
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.uosense.R
import kotlin.math.log

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private val imageUrls = mutableListOf<String>()

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]  // S3 URL 포함

        // 이미지가 유효한지 확인 후 로딩
        if (imageUrl.isNullOrEmpty()) {
            // 기본 에러 이미지 설정
            holder.imageView.setImageResource(R.drawable.ic_delete)
        } else {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)  // 로딩 중 이미지
                .error(R.drawable.ic_delete)  // 로딩 실패 시 이미지
                .centerCrop()  // 크기 조정
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView)
        }
    }



    override fun getItemCount() = imageUrls.size

    fun submitList(images: List<String>) {
        imageUrls.clear()
        imageUrls.addAll(images)
        notifyDataSetChanged()
    }
}
