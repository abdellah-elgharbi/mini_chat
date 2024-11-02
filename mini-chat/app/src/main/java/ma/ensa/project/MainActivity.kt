package com.example.chatmessenger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation des instances FirebaseAuth et FirebaseFirestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    // Définir le statut de l'utilisateur comme "Online" quand l'activité devient visible
    override fun onStart() {
        super.onStart()
        updateUserStatus("Online")
    }

    // Définir le statut de l'utilisateur comme "Offline" quand l'activité n'est plus au premier plan
    override fun onPause() {
        super.onPause()
        updateUserStatus("Offline")
    }

    /**
     * Met à jour le statut de l'utilisateur dans Firestore.
     *
     * @param status Le statut à attribuer à l'utilisateur ("Online" ou "Offline").
     */
    private fun updateUserStatus(status: String) {
        // Vérifie si un utilisateur est connecté
        auth.currentUser?.let {
            firestore.collection("Users")
                .document(Utils.getUidLoggedIn())  // Récupère l'ID de l'utilisateur connecté
                .update("status", status)  // Met à jour le statut dans Firestore
                .addOnFailureListener { e ->
                    // Gère les erreurs en les affichant dans la console
                    e.printStackTrace()
                }
        }
    }
}
