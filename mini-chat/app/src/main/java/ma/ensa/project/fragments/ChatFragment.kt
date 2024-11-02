package com.example.chatmessenger.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatmessenger.R
import com.example.chatmessenger.Utils
import com.example.chatmessenger.adapter.MessageAdapter
import com.example.chatmessenger.databinding.FragmentChatBinding
import com.example.chatmessenger.modal.Messages
import com.example.chatmessenger.mvvm.ChatAppViewModel
import de.hdodenhof.circleimageview.CircleImageView

// Déclaration de la classe ChatFragment, qui hérite de Fragment
class ChatFragment : Fragment() {

    // Déclaration des variables nécessaires
    lateinit var args: ChatFragmentArgs // Pour récupérer les arguments de la navigation
    lateinit var binding: FragmentChatBinding // Liaison de données pour le layout

    lateinit var viewModel: ChatAppViewModel // Le ViewModel associé
    lateinit var adapter: MessageAdapter // Adaptateur pour afficher les messages
    lateinit var toolbar: Toolbar // Toolbar de la vue

    // Méthode appelée pour créer la vue du fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate le layout pour ce fragment en utilisant la liaison de données
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        return binding.root // Retourne la vue racine
    }
    @SuppressLint("NotifyDataSetChanged")
    // Méthode appelée après que la vue a été créée
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisation de la toolbar et des éléments de l'interface
        toolbar = view.findViewById(R.id.toolBarChat)
        val circleImageView = toolbar.findViewById<CircleImageView>(R.id.chatImageViewUser)
        val textViewName = toolbar.findViewById<TextView>(R.id.chatUserName)
        val textViewStatus = view.findViewById<TextView>(R.id.chatUserStatus)
        val chatBackBtn = toolbar.findViewById<ImageView>(R.id.chatBackBtn)
        // Initialisation du ViewModel
        viewModel = ViewModelProvider(this).get(ChatAppViewModel::class.java)
        // Récupération des arguments passés au fragment
        args = ChatFragmentArgs.fromBundle(requireArguments())

        // Liaison du ViewModel à la vue
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner // Définit le cycle de vie pour la liaison

        // Chargement de l'image de l'utilisateur avec Glide
        Glide.with(view.context).load(args.users.imageUrl!!).placeholder(R.drawable.person).dontAnimate().into(circleImageView)
        // Mise à jour des éléments de texte avec les informations de l'utilisateur
        textViewName.text = args.users.username
        textViewStatus.text = args.users.status

        // Navigation vers le fragment précédent lors du clic sur le bouton de retour
        chatBackBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_chatFragment_to_homeFragment)
        }

        // Action lors du clic sur le bouton d'envoi
        binding.sendBtn.setOnClickListener {
            // Envoi du message via le ViewModel
            viewModel.sendMessage(Utils.getUidLoggedIn(), args.users.userid!!, args.users.username!!, args.users.imageUrl!!)
            // Réinitialisation du champ de texte
            binding.editTextMessage.setText("")
        }

        // Observation des messages pour le chat
        viewModel.getMessages(args.users.userid!!).observe(viewLifecycleOwner, Observer {
            // Initialisation de la RecyclerView avec la liste des messages
            initRecyclerView(it)
        })
    }

    // Méthode pour initialiser la RecyclerView
    private fun initRecyclerView(list: List<Messages>) {
        adapter = MessageAdapter() // Création de l'adaptateur

        // Configuration du layout manager pour la RecyclerView
        val layoutManager = LinearLayoutManager(context)
        binding.messagesRecyclerView.layoutManager = layoutManager
        layoutManager.stackFromEnd = true // Pour afficher le dernier message en bas

        // Mise à jour de l'adaptateur avec la liste de messages
        adapter.setList(list)
        adapter.notifyDataSetChanged() // Notifie l'adaptateur des changements
        binding.messagesRecyclerView.adapter = adapter // Associe l'adaptateur à la RecyclerView
    }
}
