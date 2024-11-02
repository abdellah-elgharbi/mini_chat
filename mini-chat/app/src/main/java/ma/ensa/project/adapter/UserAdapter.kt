package com.example.chatmessenger.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatmessenger.R
import com.example.chatmessenger.Utils
import com.example.chatmessenger.modal.Users
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList

class UserAdapter : RecyclerView.Adapter<UserHolder>() {

    private var listOfUsers = ArrayList<Users>() // Liste principale
    private var filteredUsers = ArrayList<Users>() // Liste filtrée

    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.userlistitem, parent, false)
        return UserHolder(view)
    }

    override fun getItemCount(): Int {
        return filteredUsers.size
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val user = filteredUsers[position]
        if (user.userid!=Utils.getUidLoggedIn()){

        }
        // Afficher uniquement le prénom
        val name = user.username?.split("\\s".toRegex())?.get(0) ?: "Unknown"
        holder.profileName.text = name

        // Modifier l'icône de statut selon l'état en ligne/hors ligne
        if (user.status == "Online") {
            holder.statusImageView.setImageResource(R.drawable.onlinestatus)
        } else {
            holder.statusImageView.setImageResource(R.drawable.offlinestatus)
        }

        // Charger l'image avec Glide
        Glide.with(holder.itemView.context).load(user.imageUrl).into(holder.imageProfile)

        // Changer la couleur si un message est reçu


        // Gérer le clic sur l'utilisateur
        holder.itemView.setOnClickListener {
            listener?.onUserSelected(position, user)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(users: List<Users>) {
        listOfUsers.clear()
        listOfUsers.addAll(users)
        filteredUsers.clear()
        filteredUsers.addAll(users)
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    // Fonction pour filtrer la liste en fonction d'une requête
    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        filteredUsers = if (lowerCaseQuery.isEmpty()) {
            ArrayList(listOfUsers) // Réinitialiser la liste filtrée
        } else {
            val filteredList = ArrayList<Users>()
            for (user in listOfUsers) {
                // Vérifier si le nom correspond à la requête
                if (user.username?.lowercase(Locale.getDefault())?.contains(lowerCaseQuery) == true) {
                    filteredList.add(user)
                }
            }
            filteredList
        }
        notifyDataSetChanged() // Mettre à jour l'adaptateur
    }
}

// ViewHolder pour la liste des utilisateurs
class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val profileName: TextView = itemView.findViewById(R.id.userName)
    val imageProfile: CircleImageView = itemView.findViewById(R.id.imageViewUser)
    val statusImageView: ImageView = itemView.findViewById(R.id.statusOnline)
}

// Interface pour gérer les clics
interface OnItemClickListener {
    fun onUserSelected(position: Int, users: Users)
}
