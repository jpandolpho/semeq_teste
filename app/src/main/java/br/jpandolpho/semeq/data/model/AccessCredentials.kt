package br.jpandolpho.semeq.data.model

import java.io.Serializable

class AccessCredentials(val access: String, val refresh: String, val username: String) : Serializable {
}