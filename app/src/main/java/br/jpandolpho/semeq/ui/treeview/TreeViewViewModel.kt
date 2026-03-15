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
    //cliente para requisições http
    private val client = OkHttpClient()

    //LiveData para controle das credenciais
    private val _credentials = MutableLiveData<AccessCredentials>()
    val credentials: LiveData<AccessCredentials> = _credentials

    //LiveData para exibição de mensagens através de Toast
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    //LiveData que armazena todos os dados recebidos da API
    private val _tree = MutableLiveData<List<Component>>()
    val tree: LiveData<List<Component>> = _tree

    //LiveData que armazena uma versão da Árvore que está sendo exibida no RecyclerView
    private val _currentTree = MutableLiveData<List<Component>>()
    val currentTree: LiveData<List<Component>> = _currentTree

    //salvando as credenciais que chegaram pelo Bundle
    fun storeCredentials(credentials: AccessCredentials) {
        _credentials.value = credentials
    }

    //fazendo requisição da árvore para a API
    fun fetchTree() {
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://internal-stream.semeq.com/api/implantation/mobile/tree?site=20812")
                //adicionando a token de acesso como header na requisição
                .addHeader("Authorization", "Bearer ${_credentials.value!!.access}")
                .build()

            //enviando requisição a API
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _error.postValue("Error requesting data.")
                } else {
                    //caso as informações cheguem, precisamos ler elas
                    val reader = JsonReader(response.body!!.charStream())
                    try {
                        //iremos salvar os valores provisoriamente numa variável, que depois será
                        //enviada para o LiveData
                        val dataSet = LinkedList<Component>()
                        /*Escolhi ignorar os primeiros valores que chegam na requisição, pois entendi
                        que eles não seriam relevantes para a solução do problema atual, que era apenas
                        a exibição da árvore.
                         */
                        reader.beginObject()
                        while (reader.hasNext()) {
                            if (!reader.nextName().equals("tree")) {
                                //ignorando os valores que não são a árvore de dados
                                reader.skipValue()
                            } else {
                                //começando a ler o array de dados da arvore
                                reader.beginArray()
                                while (reader.hasNext()) {
                                    //lendo as informações do componente e salvando ele
                                    val component = readNextComponent(reader)
                                    dataSet.add(component)
                                }
                                reader.endArray()
                            }
                        }
                        //finalizando leitura do objeto JSON da resposta
                        reader.endObject()
                        //salvando a lista no LiveData
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

    //Função para ler um componente
    private fun readNextComponent(reader: JsonReader): Component {
        //criando um componente auxiliar
        val newComponent = Component(-1, "", -1, null)
        //lendo as informações
        reader.beginObject()
        //apenas algumas informações são lidas e salvas. para mais informações sobre essa decisão,
        //consulte a classe Component
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
        //mandando o componente com as informações completas de volta
        return newComponent
    }

    //construindo uma exibição inicial da árvore. optei por começar exibindo apenas os elementos de
    //level = 0, que são as "raizes" da árvore.
    fun showTree() {
        _currentTree.value =
            _tree.value!!.stream().filter { c -> c.level == 0 }.collect(Collectors.toList())
    }

    //ao clicar em um componente, caso ele não esteja expandido, ele é expandido
    fun addChildren(position: Int) {
        //cria-se uma cópia da lista atual
        val aux = mutableListOf<Component>()
        aux.addAll(_currentTree.value!!)
        //salvamos o id do componente naquela posição clicada
        val id = aux[position].id
        //percorremos todos os componentes buscando aqueles que tem o atributo "parent" igual ao "id"
        //salvo, mostrando que aquele componente é filho do componente clicado.
        for (component in _tree.value!!) {
            if (component.id == id) {
                //como estamos expandindo o elemento clicado, alteramos o atributo expanded dele na
                //lista original
                component.expanded = !component.expanded
            }
            if (component.parent == id) {
                //caso o componente seja filho, adicionamos ele na lista numa posição a frente do pai
                aux.add(position + 1, component)
            }
        }
        //a cópia, que foi alterada, passa a ser a lista que deve ser exibida.
        _currentTree.value = aux
    }

    //ao clicar em um componente, caso ele esteja expandido, ele é recolhido. todos os filhos que estavam
    //expandidos também são recolhidos.
    fun removeChildren(position: Int) {
        //cria-se uma cópia da lista que está sendo exibida
        val aux = mutableListOf<Component>()
        aux.addAll(_currentTree.value!!)
        //anotamos qual o level o componente a ser recolhido
        val level = aux[position].level
        aux[position].expanded = !aux[position].expanded
        //variáveis de controle para saber quando parar de recolher/remover itens da lista
        var i = position + 1
        var keepRemoving = true
        while (i < aux.size) {
            //se o elemento possui um level maior que o level salvo e ainda temos que continuar removendo,
            //alteramos o valor de expanded dele e o removes da lista.
            if (aux[i].level > level && keepRemoving) {
                aux[i].expanded = !aux[i].expanded
                aux.removeAt(i)
            } else {
                //caso o elemento que estamos vendo tenha o mesmo level que o salvo, é sinal que devemos
                //parar de remover
                if (aux[i].level == level) {
                    keepRemoving = false
                }
                ++i
            }
        }
        //após as alterações, atualizamos a lista que deve ser exibida.
        _currentTree.value = aux
    }

    fun editName(position: Int, editedName: String) {
        //alteramos o nome do componente. como ambas as listas possuem as referências para os objetos,
        //essa alteração também é refletida no LiveData _tree.
        _currentTree.value!![position].name = editedName
        //reatribuímos o valor para garantir a atualização do adapter.
        _currentTree.value = _currentTree.value!!
    }
}