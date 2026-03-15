package br.jpandolpho.semeq.ui.treeview

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import br.jpandolpho.semeq.R
import br.jpandolpho.semeq.data.model.AccessCredentials
import br.jpandolpho.semeq.databinding.ActivityTreeViewBinding
import br.jpandolpho.semeq.databinding.DialogEditNameBinding
import br.jpandolpho.semeq.util.listener.ComponenetClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/*Esta activity implementa uma interface customizada que controla os clicks nos componentes que estão
sendo exibidos no RecyclerView. Mais informações sobre essa decisão podem ser encontradas na classe
ComponentListAdapter, classe que controla o RecyclerView que exibe a árvore.
 */
class TreeViewActivity : AppCompatActivity(), ComponenetClickListener {
    private lateinit var binding: ActivityTreeViewBinding
    private lateinit var viewModel: TreeViewViewModel
    private lateinit var adapter: ComponentListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTreeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(TreeViewViewModel::class.java)

        setupListener()
        setupRecyclerView()
        setupObservers()
        verifyBundle()
    }

    //Listener para encerrar a activity ao clicar na setinha
    private fun setupListener() {
        binding.icBackArrow.setOnClickListener {
            finish()
        }
    }

    //inicializando o Recycler que mostrará a árvore
    /*Optei por utilizar um recycler por ser uma forma que já me era familiar de exibir dados,
    apesar da árvore não ser exatamente uma lista. Desta forma, é possível ter acesso a tamanhos variados
    de árvores, já que a "lista" é criada e atualizada dinamicamente.
    Para dar a impressão de ser uma visualização em árvore, é utilizado um paddingStart variável baseado
    no atributo "level" do componente.
     */
    private fun setupRecyclerView() {
        adapter = ComponentListAdapter(mutableListOf(), this)
        binding.listComponents.adapter = adapter
        binding.listComponents.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        //observer para adicionar o nome do usuário no lugar do placeholder e para fazer a requisição
        //da árvore da API
        viewModel.credentials.observe(this, Observer {
            binding.textUsername.setText(it.username)
            viewModel.fetchTree()
        })

        //observer que gera toasts de erro.
        viewModel.error.observe(this, Observer {
            val message = it
            Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
            ).show()
        })

        //observer que faz o carregamento inicial da árvore após ela ser recebida da API
        viewModel.tree.observe(this, Observer {
            viewModel.showTree()
        })

        //observer que atualiza o adapter com a versão atualizada da árvore que deve ser exibida,
        //se baseando nos atributos "expanded" de cada componente.
        viewModel.currentTree.observe(this, Observer {
            adapter.loadData(it)
        })
    }

    //Verifica se o bundle com as credenciais chegou corretamente. Caso não tenha chego, encerra a
    //activity
    private fun verifyBundle() {
        if (intent.extras != null) {
            val credentials = intent.getSerializableExtra("credentials") as AccessCredentials
            viewModel.storeCredentials(credentials)
        } else{
            finish()
        }
    }

    //funcao da interface. expande ou retrai um item do recycler view, a depender do estado dele.
    override fun toggleItem(position: Int, expand: Boolean) {
        if (expand) {
            viewModel.addChildren(position)
        } else {
            viewModel.removeChildren(position)
        }
    }

    /*funcao da interface. exibe um dialog customizado para edição de componentes que tem o atributo
    "level" com o valor 2.
     */
    override fun editName(position: Int, name: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
        val bindingDialog = DialogEditNameBinding.bind(dialogView)
        bindingDialog.textEditEquipment.setText(name)

        val builder = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setTitle("Edit equipment name")
            .create()

        bindingDialog.buttonConfirm.setOnClickListener {
            val editedName = bindingDialog.textEditEquipment.text.toString()
            viewModel.editName(position, editedName)
            builder.dismiss()
        }
        bindingDialog.buttonCancel.setOnClickListener {
            dialogView.visibility = View.GONE
            builder.dismiss()
        }

        builder.window!!.setGravity(Gravity.BOTTOM)
        builder.show()
    }
}