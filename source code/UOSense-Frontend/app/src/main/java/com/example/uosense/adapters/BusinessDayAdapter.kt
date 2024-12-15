import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.AppUtils
import com.example.uosense.R
import com.example.uosense.models.BusinessDayInfo

class BusinessDayAdapter(private val mode: BusinessDayMode) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val businessDays = mutableListOf<BusinessDayInfo>()

    // 조회 뷰홀더
    inner class ViewHolderDisplay(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayOfWeek: TextView = itemView.findViewById(R.id.dayOfWeek)
        val openingTime: TextView = itemView.findViewById(R.id.openingTime)
        val closingTime: TextView = itemView.findViewById(R.id.closingTime)
        val startBreakTime: TextView = itemView.findViewById(R.id.startBreakTime)
        val stopBreakTime: TextView = itemView.findViewById(R.id.stopBreakTime)
        val holiday: TextView = itemView.findViewById(R.id.holiday)
    }

    // 수정 뷰홀더
    inner class ViewHolderEdit(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayOfWeek: Spinner = itemView.findViewById(R.id.spinnerDayOfWeek)
        val openingTime: EditText = itemView.findViewById(R.id.editOpeningTime)
        val closingTime: EditText = itemView.findViewById(R.id.editClosingTime)
        val startBreakTime: EditText = itemView.findViewById(R.id.editStartBreakTime)
        val stopBreakTime: EditText = itemView.findViewById(R.id.editStopBreakTime)
        val holiday: CheckBox = itemView.findViewById(R.id.checkboxHoliday)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == BusinessDayMode.DISPLAY.ordinal) {
            val view = inflater.inflate(R.layout.item_business_day, parent, false)
            ViewHolderDisplay(view)
        } else {
            val view = inflater.inflate(R.layout.business_day_edit, parent, false)
            ViewHolderEdit(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val businessDay = businessDays[position]
        when (holder) {
            is ViewHolderDisplay -> {
                holder.dayOfWeek.text = businessDay.dayOfWeek
                holder.openingTime.text = businessDay.openingTime
                holder.closingTime.text = businessDay.closingTime
                holder.startBreakTime.text = businessDay.startBreakTime ?: "없음"
                holder.stopBreakTime.text = businessDay.stopBreakTime ?: "없음"
                holder.holiday.text = if (businessDay.holiday) "휴일" else "영업 중"
            }

            is ViewHolderEdit -> {
                val context = holder.itemView.context
                val daysArray = context.resources.getStringArray(R.array.days_of_week)
                val dayIndex = daysArray.indexOf(businessDay.dayOfWeek)

                holder.dayOfWeek.setSelection(if (dayIndex != -1) dayIndex else 0)
                holder.openingTime.setText(businessDay.openingTime)
                holder.closingTime.setText(businessDay.closingTime)
                holder.startBreakTime.setText(businessDay.startBreakTime ?: "")
                holder.stopBreakTime.setText(businessDay.stopBreakTime ?: "")
                holder.holiday.isChecked = businessDay.holiday
            }
        }
    }

    override fun getItemCount(): Int = businessDays.size

    override fun getItemViewType(position: Int): Int = mode.ordinal

    fun submitList(newBusinessDays: List<BusinessDayInfo>) {
        businessDays.clear()
        businessDays.addAll(newBusinessDays)
        notifyDataSetChanged()
    }

    fun addBusinessDay(businessDay: BusinessDayInfo) {
        businessDays.add(businessDay)
        notifyItemInserted(businessDays.size - 1)
    }

    fun getUpdatedBusinessDays(parentRecyclerView: RecyclerView): List<BusinessDayInfo> {
        return businessDays.mapIndexed { index, _ ->
            (parentRecyclerView.findViewHolderForAdapterPosition(index) as? ViewHolderEdit)?.let {
                BusinessDayInfo(
                    id = businessDays[index].id,
                    dayOfWeek = AppUtils.mapDayOfWeek(it.dayOfWeek.selectedItem.toString()),
                    openingTime = it.openingTime.text.toString(),
                    closingTime = it.closingTime.text.toString(),
                    startBreakTime = it.startBreakTime.text.toString().ifBlank { null },
                    stopBreakTime = it.stopBreakTime.text.toString().ifBlank { null },
                    holiday = it.holiday.isChecked,
                    haveBreakTime = !it.startBreakTime.text.isNullOrBlank() && !it.stopBreakTime.text.isNullOrBlank()
                )
            } ?: businessDays[index]
        }
    }

}

enum class BusinessDayMode {
    DISPLAY, EDIT
}
