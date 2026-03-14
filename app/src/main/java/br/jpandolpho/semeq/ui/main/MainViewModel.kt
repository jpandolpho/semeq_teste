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

    private val _errorLogin = MutableLiveData<String>()
    val errorLogin: LiveData<String> = _errorLogin

    private val _accessToken = MutableLiveData<AccessCredentials>()
    val accessToken: LiveData<AccessCredentials> = _accessToken

    private val client = OkHttpClient()

    fun login(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = StringWriter()
            val writer = JsonWriter(json)
            writer.beginObject()
            writer.name("username").value(username)
            writer.name("password").value(password)
            writer.endObject()
            writer.close()

            val request = Request.Builder()
                .url("https://internal-stream.semeq.com/api/token")
                .post(
                    json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                )
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _errorLogin.postValue("Login Error. Verify username and/or password.")
                } else {
                    val reader = JsonReader(response.body!!.charStream())
                    try {
                        var refresh = ""
                        var access = ""
                        reader.beginObject()
                        while (reader.hasNext()) {
                            if (reader.nextName().equals("refresh")) {
                                refresh = reader.nextString()
                            }
                            if (reader.nextName().equals("access")) {
                                access = reader.nextString()
                            }
                        }
                        reader.endObject()
                        val token = AccessCredentials(access, refresh, username)
                        _accessToken.postValue(token)
                    } catch (e: IOException) {
                        _errorLogin.postValue("Request Error. Try again later.")
                    } finally {
                        reader.close()
                    }
                }
            }
        }
    }
}