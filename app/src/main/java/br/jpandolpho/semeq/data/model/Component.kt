package br.jpandolpho.semeq.data.model

/*Classe de negócio representando os objetos do sistema.
Optei por colher apenas essas 4 características dentre as 15 disponíveis através da API
para simplificar o problema, visto que, a meu ver, eram as 4 características relevantes.
Foi adicionado também uma característica "expanded" para possibilitar a manipulação da árvore,
sendo possível expandir ou retrair nós.
*/

class Component(
    var id: Int,
    var name: String,
    var level: Int,
    var parent: Int?,
    var expanded: Boolean = false
) {
}