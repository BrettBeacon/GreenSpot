import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.csc202.assignment.greenspot.Plant
import com.csc202.assignment.greenspot.databinding.ListItemPlantBinding
import java.util.UUID

class PlantHolder(
    private val binding: ListItemPlantBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(plant: Plant, onPlantClicked: (plantId: UUID) -> Unit) {
        binding.plantTitle.text = plant.title
        binding.plantPlace.text = plant.place
        binding.plantDate.text = plant.date.toString()

        binding.root.setOnClickListener {
            onPlantClicked(plant.id)
        }

        binding.plantShared.visibility = if (plant.isShared) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

}

class PlantListAdapter(
    private val plants: List<Plant>,
    private val onPlantClicked: (plantId: UUID) -> Unit
) : RecyclerView.Adapter<PlantHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlantHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemPlantBinding.inflate(inflater, parent, false)
        return PlantHolder(binding)
    }

    override fun onBindViewHolder(holder: PlantHolder, position: Int) {
        val plant = plants[position]
        holder.bind(plant, onPlantClicked)
    }

    override fun getItemCount() = plants.size
}