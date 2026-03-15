package br.jpandolpho.semeq.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import br.jpandolpho.semeq.databinding.ActivityMainBinding
import br.jpandolpho.semeq.ui.treeview.TreeViewActivity
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setupImage()
        setupListener()
        setupObservers()
    }

    private fun setupObservers() {
        //Observer para relatar algum problema no Login ou algum problema na requisição de autenticação.
        viewModel.errorLogin.observe(this, Observer {
            val message = it
            Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
            ).show()
        })

        /*Caso o login seja feito com sucesso, lançamos a próxima activity mandando junto num Bundle
        as credenciais de acesso através de um objeto AccessCredentials.
         */
        viewModel.accessToken.observe(this, Observer {
            binding.textUser.setText("")
            binding.textSenha.setText("")
            val mIntent = Intent(this, TreeViewActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("credentials", it)
            mIntent.putExtras(bundle)
            startActivity(mIntent)
        })
    }

    private fun setupListener() {
        binding.buttonLogin.setOnClickListener {
            handleLogin()
        }
    }

    private fun handleLogin() {
        val username = binding.textUser.text.toString()
        val password = binding.textSenha.text.toString()
        viewModel.login(username, password)
    }

    //Forma encontrada para estilizar a imagem, para se aproximar da apresentada no exemplo do pdf.
    private fun setupImage() {
        binding.icPerson.shapeAppearanceModel = ShapeAppearanceModel.Builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, 150f)
            .setTopRightCorner(CornerFamily.ROUNDED, 50f)
            .setBottomRightCorner(CornerFamily.ROUNDED, 150f)
            .setBottomLeftCorner(CornerFamily.ROUNDED, 50f)
            .build()
    }
}