@file:Suppress("DEPRECATION")

package com.example.chatmessenger.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.chatmessenger.MainActivity
import com.example.chatmessenger.R
import com.example.chatmessenger.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

    class SignInActivity : AppCompatActivity() {

        private lateinit var fbauth: FirebaseAuth
        private lateinit var pds: ProgressDialog
        private lateinit var binding: ActivitySignInBinding

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)

            fbauth = FirebaseAuth.getInstance()
            pds = ProgressDialog(this).apply {
                setMessage("Signing In")
            }

        // Rediriger vers MainActivity si l'utilisateur est déjà connecté
        fbauth.currentUser?.let {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Terminer cette activité pour éviter de revenir en arrière
        }

        binding.signInTextToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.loginButton.setOnClickListener {
            val email = binding.loginetemail.text.toString().trim()
            val password = binding.loginetpassword.text.toString().trim()

            if (email.isEmpty()) {
                showToast("Veuillez entrer votre email")
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                showToast("Veuillez entrer votre mot de passe")
                return@setOnClickListener
            }

            signIn(email, password)
        }
    }

    private fun signIn(email: String, password: String) {
        pds.show()

        fbauth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            pds.dismiss()
            if (task.isSuccessful) {
                startActivity(Intent(this, MainActivity::class.java))
                finish() // Terminer cette activité
            } else {
                showToast("Identifiants invalides")
            }
        }.addOnFailureListener { exception ->
            pds.dismiss()
            when (exception) {
                is FirebaseAuthInvalidCredentialsException -> {
                    showToast("Identifiants invalides")
                }
                else -> {
                    showToast("Échec de l'authentification")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        pds.dismiss()
        super.onBackPressed()
    }

    override fun onDestroy() {
        pds.dismiss()
        super.onDestroy()
    }
}
