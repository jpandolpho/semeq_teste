package br.jpandolpho.semeq.data.model

import java.io.Serializable

//Classe de negócio para representar username e tokens de acesso a API
//É serializável para que seja possível enviá-lo num Bundle entre activities.
class AccessCredentials(val access: String, val refresh: String, val username: String) :
    Serializable {
}