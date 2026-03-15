package br.jpandolpho.semeq.ui.treeview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.jpandolpho.semeq.R
import br.jpandolpho.semeq.data.model.Component
import br.jpandolpho.semeq.databinding.ItemListComponentBinding
import br.jpandolpho.semeq.util.listener.ComponenetClickListener

/*Classe que controla a atualização do dados no RecyclerView. Para que possamos tratar os clicks
nos componentes corretamente, é utilizada uma interface para passar a resposabilidade de tratar os
dados para o listener, que está implementando de fato os métodos. Neste caso, o listener é a própria
activity.
 */
class ComponentListAdapter(
    private var dataset: List<Component>,
    private val clickListener: ComponenetClickListener
) :
    RecyclerView.Adapter<ComponentListAdapter.ViewHolder>() {

    //Criando o layout customizado para os itens da lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_list_component, parent, false)
        return ViewHolder(view)
    }

    //funcao necessaria para o recyclerview, que mostra quantos itens existem no dataset sendo mostrado
    override fun getItemCount(): Int {
        return dataset.size
    }

    //associando os dados a ser mostrados com os campos do layout criado
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val component = dataset[position]
        holder.binding.textComponentName.setText(component.name)
        //ajustando dinamicamente o padding do elemento, para dar a impressão que estamos vendo uma
        //árvore, e não apenas uma lista.
        holder.binding.listItem.setPadding(component.level * 50, 0, 0, 0)
        holder.binding.listItem.setOnClickListener {
            clickListener.toggleItem(position, !component.expanded)
        }
        //exibindo o botão de edição para caso o componente tenha "level" igual a 2.
        if (component.level == 2) {
            holder.binding.icEditName.visibility = View.VISIBLE
            holder.binding.icEditName.setOnClickListener {
                clickListener.editName(position, component.name)
            }
        } else {
            //caso ele não tenha level 2, removemos a imagem
            holder.binding.icEditName.visibility = View.GONE
        }
        //ajustando dinamicamente qual o icone a ser exibido, se é o de sensor ou de pasta, a depender
        //do level do elemento.
        if (component.level == 3) {
            holder.binding.icListItem.setImageResource(R.drawable.ic_sensor)
        } else {
            holder.binding.icListItem.setImageResource(R.drawable.ic_folder)
        }
    }

    //atualizando os dados
    fun loadData(data: List<Component>) {
        dataset = data
        //funcao do viewholder que atualiza a visualização dos elementos
        notifyDataSetChanged()
    }

    //classe auxiliar do viewholder
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: ItemListComponentBinding = ItemListComponentBinding.bind(view)
    }
}