package br.jpandolpho.semeq.ui.main

import android.app.Application
import android.util.JsonReader
import android.util.JsonWriter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import br.jpandolpho.semeq.data.model.AccessCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.io.StringWriter

class MainViewModel(application: Application) : AndroidViewModel(application) {

    //LiveData para envio de informações através de Toast
    private val _errorLogin = MutableLiveData<String>()
    val errorLogin: LiveData<String> = _errorLogin

    //LiveData para envio das credenciais de acesso para a próxima activity
    private val _accessToken = MutableLiveData<AccessCredentials>()
    val accessToken: LiveData<AccessCredentials> = _accessToken

    //Cliente para realização de requisições HTTP para acesso da API
    private val client = OkHttpClient()

    fun login(username: String, password: String) {
        //Caso os campos de username e password não estejam vazios
        if (username.isNotEmpty() && password.isNotEmpty()) {
            //abertura de thread para operações de I/O
            viewModelScope.launch(Dispatchers.IO) {
                //Criação do JSON que irá no corpo da requisição com user e senha
                val json = StringWriter()
                val writer = JsonWriter(json)
                writer.beginObject()
                writer.name("username").value(username)
                writer.name("password").value(password)
                writer.endObject()
                writer.close()

                //Criação da requisição
                val request = Request.Builder()
                    .url("https://internal-stream.semeq.com/api/token")
                    .post(
                        json.toString()
                            .toRequestBody("application/json; charset=utf-8".toMediaType())
                    )
                    .build()

                //Enviando a requisição
                client.newCall(request).execute().use { response ->
                    //Caso login não seja feito com sucesso, retornamos mensagem para o Toast
                    if (!response.isSuccessful) {
                        _errorLogin.postValue("Login Error. Verify username and/or password.")
                    } else {
                        //Caso o login tenha sido efetuado com sucesso, temos que ler a resposta
                        val reader = JsonReader(response.body!!.charStream())
                        try {
                            //variáveis de auxílio para leitura da resposta
                            var refresh = ""
                            var access = ""
                            reader.beginObject()
                            //lendo campos da requisição
                            while (reader.hasNext()) {
                                if (reader.nextName().equals("refresh")) {
                                    refresh = reader.nextString()
                                }
                                if (reader.nextName().equals("access")) {
                                    access = reader.nextString()
                                }
                            }
                            reader.endObject()
                            //Criando token de acesso para ser enviado para a próxima activity
                            val token = AccessCredentials(access, refresh, username)
                            _accessToken.postValue(token)
                        } catch (e: IOException) {
                            //Caso ocorra algum erro durante a leitura da requisição, lançamos uma
                            //exceção, e mandamos mensagem para o Toast
                            _errorLogin.postValue("Request Error. Try again later.")
                        } finally {
                            reader.close()
                        }
                    }
                }
            }
        } else {
            //Caso campos user e/ou senha estejam vazios, retornamos mensagem pro Toast
            _errorLogin.value = "Please insert values for username and password."
        }
    }
}