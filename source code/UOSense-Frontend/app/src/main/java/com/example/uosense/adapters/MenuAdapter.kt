import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uosense.R
import com.example.uosense.models.MenuResponse

class MenuAdapter(
    private val mode: MenuMode,
    private val menuImagePicker: MenuImagePicker
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var menuItems = mutableListOf<MenuResponse>()

    inner class ViewHolderDisplay(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuName: TextView = itemView.findViewById(R.id.menuName)
        val menuPrice: TextView = itemView.findViewById(R.id.menuPrice)
        val menuDescription: TextView = itemView.findViewById(R.id.menuDescription)
        val menuImage: ImageView = itemView.findViewById(R.id.menuImage)
    }

    inner class ViewHolderEdit(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editMenuName: EditText = itemView.findViewById(R.id.editMenuName)
        val editMenuPrice: EditText = itemView.findViewById(R.id.editMenuPrice)
        val editMenuDescription: EditText = itemView.findViewById(R.id.editMenuDescription)
        val menuImage: ImageView = itemView.findViewById(R.id.menuImage)
        val btnEditMenuImage: Button = itemView.findViewById(R.id.btnEditMenuImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == MenuMode.DISPLAY.ordinal) {
            ViewHolderDisplay(inflater.inflate(R.layout.item_menu, parent, false))
        } else {
            ViewHolderEdit(inflater.inflate(R.layout.menu_edit, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val menuItem = menuItems[position]

        when (holder) {
            is ViewHolderDisplay -> {
                holder.menuName.text = menuItem.name
                holder.menuPrice.text = "${menuItem.price}원"
                holder.menuDescription.text = menuItem.description ?: "설명 없음"
                Glide.with(holder.itemView.context)
                    .load(menuItem.imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(holder.menuImage)
            }

            is ViewHolderEdit -> {
                holder.editMenuName.setText(menuItem.name)
                holder.editMenuPrice.setText(menuItem.price.toString())
                holder.editMenuDescription.setText(menuItem.description ?: "")
                Glide.with(holder.itemView.context)
                    .load(menuItem.imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(holder.menuImage)

                holder.btnEditMenuImage.setOnClickListener {
                    menuImagePicker.openImagePicker(holder.adapterPosition)
                }
            }
        }
    }
    fun getUpdatedMenuItems(): List<MenuResponse> = menuItems
    override fun getItemCount(): Int = menuItems.size

    override fun getItemViewType(position: Int): Int = mode.ordinal

    fun submitList(newMenuItems: List<MenuResponse>) {
        menuItems.clear()
        menuItems.addAll(newMenuItems)
        notifyDataSetChanged()
    }

    fun addMenuItem(menu: MenuResponse) {
        menuItems.add(menu)
        notifyItemInserted(menuItems.size - 1)
    }

    fun updateMenuImage(position: Int, imageUri: Uri) {
        menuItems[position].imageUrl = imageUri.toString()
        notifyItemChanged(position)
    }
}


enum class MenuMode {
    DISPLAY, EDIT
}
interface MenuImagePicker {
    fun openImagePicker(position: Int)
}