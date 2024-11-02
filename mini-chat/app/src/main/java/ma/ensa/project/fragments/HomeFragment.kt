package com.example.chatmessenger.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatmessenger.R
import com.example.chatmessenger.Utils
import com.example.chatmessenger.activities.SignInActivity
import com.example.chatmessenger.adapter.OnItemClickListener
import com.example.chatmessenger.adapter.UserAdapter
import com.example.chatmessenger.modal.Users
import com.example.chatmessenger.mvvm.ChatAppViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

class HomeFragment : Fragment(), OnItemClickListener {

    private lateinit var rvUsers: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var viewModel: ChatAppViewModel
    private lateinit var toolbar: Toolbar
    private lateinit var circleImageView: CircleImageView

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Indiquer que le fragment possède un menu
        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this).get(ChatAppViewModel::class.java)
        toolbar = view.findViewById(R.id.toolbarMain)
        toolbar.setTitle("");
        val logoutImage = toolbar.findViewById<ImageView>(R.id.logOut)
        circleImageView = toolbar.findViewById(R.id.tlImage)

        firestore = FirebaseFirestore.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        logoutImage.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(requireContext(), SignInActivity::class.java))
        }

        rvUsers = view.findViewById(R.id.rvUsers)
        adapter = UserAdapter()

        rvUsers.layoutManager = LinearLayoutManager(activity)

        // Observer pour récupérer la liste des utilisateurs
        viewModel.getUsers().observe(viewLifecycleOwner, Observer { usersList ->
            adapter.setList(usersList)
            rvUsers.adapter = adapter
        })

        // Charger l'image du profil de l'utilisateur connecté
        loadUserProfileImage(Utils.getUidLoggedIn())

        // Gestion du clic sur le profil de l'utilisateur
        circleImageView.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_settingFragment)
        }

        adapter.setOnClickListener(this)
    }

    // Fonction pour charger l'image de profil depuis Firestore
    private fun loadUserProfileImage(userId: String?) {
        if (userId != null) {
            firestore.collection("Users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val imageUrl = document.getString("imageUrl")
                        if (!imageUrl.isNullOrEmpty()) {
                            // Charger l'image avec Glide
                            Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.profil) // Image par défaut
                                .into(circleImageView)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Utilisateur introuvable", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onUserSelected(position: Int, users: Users) {
        val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(users)
        view?.findNavController()?.navigate(action)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setBackgroundResource(R.drawable.serachview)
        searchView.isIconified = false
        searchView.alpha = 0f
        searchView.visibility = View.VISIBLE
        searchView.translationX = (-searchView.width).toFloat()
        searchView.visibility = View.VISIBLE

        searchView.animate()
            .translationX(0f)
            .setDuration(300)
            .alpha(1f)

        searchView.animate().alpha(1f).setDuration(300)




        searchView.queryHint = "Rechercher un utilisateur..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })
    }
}
