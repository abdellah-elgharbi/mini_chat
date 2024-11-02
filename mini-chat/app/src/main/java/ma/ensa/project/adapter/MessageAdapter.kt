package com.example.chatmessenger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatmessenger.R
import com.example.chatmessenger.Utils
import com.example.chatmessenger.modal.Messages

// Classe MessageAdapter qui étend RecyclerView.Adapter pour gérer l'affichage des messages
class MessageAdapter : RecyclerView.Adapter<MessageHolder>() {

    // Liste des messages à afficher
    private var listOfMessage = listOf<Messages>()

    // Constantes pour déterminer le type de message (gauche ou droite)
    private val LEFT = 0
    private val RIGHT = 1

    // Crée le ViewHolder en fonction du type de message (gauche ou droite)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == RIGHT) {
            val view = inflater.inflate(R.layout.chatitemright, parent, false)
            MessageHolder(view)
        } else {
            val view = inflater.inflate(R.layout.chatitemleft, parent, false)
            MessageHolder(view)
        }
    }

    // Retourne le nombre total de messages
    override fun getItemCount() = listOfMessage.size

    // Lie les données du message au ViewHolder
    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val message = listOfMessage[position]

        // Affiche le texte et l'heure d'envoi du message
        holder.messageText.visibility = View.VISIBLE
        holder.timeOfSent.visibility = View.VISIBLE

        holder.messageText.text = message.message
        holder.timeOfSent.text = message.time?.substring(0, 5) ?: ""
    }

    // Détermine le type de vue du message en fonction de l'expéditeur
    override fun getItemViewType(position: Int) =
        if (listOfMessage[position].sender == Utils.getUidLoggedIn()) RIGHT else LEFT

    // Met à jour la liste des messages
    fun setList(newList: List<Messages>) {
        this.listOfMessage = newList
    }
}

// ViewHolder pour chaque message
class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView.rootView) {
    val messageText: TextView = itemView.findViewById(R.id.show_message)
    val timeOfSent: TextView = itemView.findViewById(R.id.timeView)
}
