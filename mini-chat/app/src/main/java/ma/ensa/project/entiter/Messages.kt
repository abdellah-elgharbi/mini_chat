package com.example.chatmessenger.modal

// Classe Messages pour représenter un message envoyé entre utilisateurs
class Messages(
    val sender: String? = "",       // Identifiant de l'expéditeur du message
    val receiver: String? = "",     // Identifiant du destinataire du message
    val message: String? = "",      // Contenu du message
    val time: String? = ""          // Heure d'envoi du message
) {
    // Propriété calculée 'id' qui génère un identifiant unique pour chaque message
    val id: String get() = "$sender-$receiver-$message-$time"
}
