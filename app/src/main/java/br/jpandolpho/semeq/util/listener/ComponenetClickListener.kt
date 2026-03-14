package br.jpandolpho.semeq.util.listener

interface ComponenetClickListener {
    fun toggleItem(position: Int, expand:Boolean)
    fun editName(position: Int, name:String)
}