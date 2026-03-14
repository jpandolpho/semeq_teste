package br.jpandolpho.semeq.ui.treeview

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

class TreeViewActivity : AppCompatActivity(), ComponenetClickListener {
    private lateinit var binding: ActivityTreeViewBinding
    private lateinit var viewModel: TreeViewViewModel
    private lateinit var adapter: ComponentListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTreeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(TreeViewViewModel::class.java)

        setupRecyclerView()
        setupObservers()
        verifyBundle()
    }

    private fun setupRecyclerView() {
        adapter = ComponentListAdapter(mutableListOf(),this)
        binding.listComponents.adapter = adapter
        binding.listComponents.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        viewModel.credentials.observe(this, Observer {
            viewModel.fetchTree(it.access)
        })

        viewModel.error.observe(this, Observer {
            val message = it
            Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
            ).show()
        })

        viewModel.tree.observe(this, Observer {
            viewModel.showTree()
        })

        viewModel.currentTree.observe(this, Observer {
            adapter.loadData(it)
        })
    }

    private fun verifyBundle() {
        if (intent.extras != null) {
            val credentials = intent.getSerializableExtra("credentials") as AccessCredentials
            binding.textUsername.setText(credentials.username)
            viewModel.storeCredentials(credentials)
        }
    }

    override fun toggleItem(position: Int, expand: Boolean) {
        if(expand) {
            viewModel.addChildren(position)
        }else{
            viewModel.removeChildren(position)
        }
    }

    override fun editName(position: Int, name:String) {
        val dialogView =layoutInflater.inflate(R.layout.dialog_edit_name,null)
        val bindingDialog = DialogEditNameBinding.bind(dialogView)
        bindingDialog.textEditEquipment.setText(name)

        val builder = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setTitle("Edit equipment name")
            .setPositiveButton("Confirm", DialogInterface.OnClickListener { dialogInterface, i ->
                val editedName = bindingDialog.textEditEquipment.text.toString()
                viewModel.editName(position,editedName)
                dialogInterface.dismiss()
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
            })
            .create().show()
    }
}