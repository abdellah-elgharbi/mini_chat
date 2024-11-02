package com.example.chatmessenger.modal

import android.os.Parcel
import android.os.Parcelable

// Data class qui représente un utilisateur dans le système de messagerie
class Users(
    val userid: String? = "",        // ID unique de l'utilisateur
    val status: String? = "",        // Statut de l'utilisateur (en ligne, hors ligne, etc.)
    val imageUrl: String? = "",      // URL de l'image de profil de l'utilisateur
    val username: String? = "",      // Nom d'utilisateur affiché
    val useremail: String? = "",     // Adresse email de l'utilisateur
    var hasNewMessage: Boolean = false // Indicateur si l'utilisateur a un nouveau message
) : Parcelable {

    // Constructeur secondaire pour créer un objet Users à partir d'un Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString(), // Lecture de l'ID utilisateur
        parcel.readString(), // Lecture du statut
        parcel.readString(), // Lecture de l'URL de l'image
        parcel.readString(), // Lecture du nom d'utilisateur
        parcel.readString()  // Lecture de l'email
    )

    // Écrit les propriétés de l'utilisateur dans le Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userid)
        parcel.writeString(status)
        parcel.writeString(imageUrl)
        parcel.writeString(username)
        parcel.writeString(useremail)
    }

    // Méthode nécessaire pour l'interface Parcelable
    override fun describeContents(): Int {
        return 0
    }

    // Créateur pour la classe Parcelable
    companion object CREATOR : Parcelable.Creator<Users> {
        override fun createFromParcel(parcel: Parcel): Users {
            return Users(parcel) // Création d'une instance Users à partir d'un Parcel
        }

        override fun newArray(size: Int): Array<Users?> {
            return arrayOfNulls(size) // Création d'un tableau d'objets Users
        }
    }
}
