package br.jpandolpho.semeq.ui.treeview

import android.app.Application
import android.util.JsonReader
import android.util.JsonToken
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import br.jpandolpho.semeq.data.model.AccessCredentials
import br.jpandolpho.semeq.data.model.Component
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.LinkedList
import java.util.stream.Collectors

class TreeViewViewModel(application: Application) : AndroidViewModel(application) {
    private val client = OkHttpClient()

    private val _credentials = MutableLiveData<AccessCredentials>()
    val credentials: LiveData<AccessCredentials> = _credentials

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _tree = MutableLiveData<List<Component>>()
    val tree: LiveData<List<Component>> = _tree

    private val _currentTree = MutableLiveData<List<Component>>()
    val currentTree: LiveData<List<Component>> = _currentTree

    fun storeCredentials(credentials: AccessCredentials) {
        _credentials.value = credentials
    }

    fun fetchTree(access: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://internal-stream.semeq.com/api/implantation/mobile/tree?site=20812")
                .addHeader("Authorization", "Bearer $access")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _error.postValue("Error requesting data.")
                } else {
                    val reader = JsonReader(response.body!!.charStream())
                    try {
                        val dataSet = LinkedList<Component>()
                        reader.beginObject()
                        while (reader.hasNext()) {
                            if (!reader.nextName().equals("tree")) {
                                reader.skipValue()
                            } else {
                                reader.beginArray()
                                while (reader.hasNext()) {
                                    val component = readNextComponent(reader)
                                    dataSet.add(component)
                                }
                                reader.endArray()
                            }
                        }
                        reader.endObject()
                        _tree.postValue(dataSet)
                    } catch (e: IOException) {
                        _error.postValue("Request Error. Try again later.")
                    } finally {
                        reader.close()
                    }
                }
            }
        }
    }

    private fun readNextComponent(reader: JsonReader): Component {
        val newComponent = Component(-1, "", -1, null)
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name.equals("id")) {
                newComponent.id = reader.nextInt()
            } else if (name.equals("name")) {
                newComponent.name = reader.nextString()
            } else if (name.equals("level")) {
                newComponent.level = reader.nextInt()
            } else if (name.equals("parent") && reader.peek() != JsonToken.NULL) {
                newComponent.parent = reader.nextInt()
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()
        return newComponent
    }

    fun showTree() {
        _currentTree.value =
            _tree.value!!.stream().filter { c -> c.level == 0 }.collect(Collectors.toList())
    }

    fun addChildren(position: Int) {
        val aux = mutableListOf<Component>()
        aux.addAll(_currentTree.value!!)
        val id = aux[position].id
        for (component in _tree.value!!) {
            if (component.id == id) {
                component.expanded = !component.expanded
            }
            if (component.parent == id) {
                aux.add(position + 1, component)
            }
        }
        _currentTree.value = aux
    }

    fun removeChildren(position: Int) {
        val aux = mutableListOf<Component>()
        aux.addAll(_currentTree.value!!)
        val level = aux[position].level
        aux[position].expanded = !aux[position].expanded
        var i = position + 1
        var keepRemoving = true
        while (i < aux.size) {
            if (aux[i].level > level && keepRemoving) {
                aux[i].expanded = !aux[i].expanded
                aux.removeAt(i)
            } else {
                if (aux[i].level == level) {
                    keepRemoving = false
                }
                ++i
            }
        }
        _currentTree.value = aux
    }

    fun editName(position: Int, editedName: String) {
        _currentTree.value!![position].name = editedName
        _currentTree.value = _currentTree.value!!
    }
}