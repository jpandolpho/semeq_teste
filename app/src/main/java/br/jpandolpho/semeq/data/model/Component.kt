package br.jpandolpho.semeq.data.model

class Component(
    var id: Int,
    var name: String,
    var level: Int,
    var parent: Int?,
    var expanded: Boolean = true
) {
    override fun toString(): String {
        return "{id:$id; name:$name; level:$level; parent:$parent; expanded:$expanded}"
    }
}