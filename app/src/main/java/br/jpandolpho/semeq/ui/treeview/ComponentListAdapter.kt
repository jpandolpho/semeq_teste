package br.jpandolpho.semeq.ui.treeview

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.jpandolpho.semeq.R
import br.jpandolpho.semeq.data.model.Component
import br.jpandolpho.semeq.databinding.ItemListComponentBinding
import br.jpandolpho.semeq.util.listener.ComponenetClickListener

class ComponentListAdapter(
    private var dataset: List<Component>,
    private val clickListener: ComponenetClickListener
) :
    RecyclerView.Adapter<ComponentListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_list_component, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val component = dataset[position]
        holder.binding.textComponentName.setText(component.name)
        holder.binding.listItem.setPadding(component.level * 50, 0, 0, 0)
        holder.binding.listItem.setOnClickListener {
            clickListener.toggleItem(position,!component.expanded)
        }
        if (component.level == 2) {
            holder.binding.icEditName.visibility = View.VISIBLE
            holder.binding.icEditName.setOnClickListener {
                clickListener.editName(position)
            }
        } else {
            holder.binding.icEditName.visibility = View.GONE
        }
        if (component.level == 3) {
            holder.binding.icListItem.setImageResource(R.drawable.ic_sensor)
        } else {
            holder.binding.icListItem.setImageResource(R.drawable.ic_folder)
        }
    }

    fun loadData(data: List<Component>) {
        dataset = data
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: ItemListComponentBinding = ItemListComponentBinding.bind(view)
    }
}