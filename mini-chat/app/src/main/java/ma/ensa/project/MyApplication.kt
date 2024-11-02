package com.example.chatmessenger

import android.app.Application
// Classe principale de l'application qui hérite de la classe Application
class MyApplication : Application() {
    // Objet compagnon pour stocker une instance statique de MyApplication
    companion object {
        // Variable pour garder une référence à l'instance de l'application
        lateinit var instance: MyApplication
    }
    // Méthode appelée lorsque l'application est créée
    override fun onCreate() {
        super.onCreate() // Appelle la méthode onCreate de la classe parente
        // Initialise l'instance de MyApplication
        instance = this
    }
}
