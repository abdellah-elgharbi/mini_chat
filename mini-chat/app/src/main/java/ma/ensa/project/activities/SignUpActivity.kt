@file:Suppress("DEPRECATION")

package com.example.chatmessenger.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatmessenger.R
import com.example.chatmessenger.Utils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var pd: ProgressDialog

    private lateinit var nameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var signUpBtn: Button
    private lateinit var signInLink: TextView
    private lateinit var profileImageView: ImageView

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        // Initialisation du ProgressDialog
        pd = ProgressDialog(this)
        pd.setMessage("Registering User")

        nameEt = findViewById(R.id.signUpEtName)
        emailEt = findViewById(R.id.signUpEmail)
        passwordEt = findViewById(R.id.signUpPassword)
        signUpBtn = findViewById(R.id.signUpBtn)
        signInLink = findViewById(R.id.signUpTextToSignIn)
        profileImageView = findViewById(R.id.image)

        signInLink.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        signUpBtn.setOnClickListener {
            val name = nameEt.text.toString()
            val email = emailEt.text.toString()
            val password = passwordEt.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Select a profile image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createAnAccount(name, email, password)
        }

        profileImageView.setOnClickListener {
            showImagePickerDialog()
        }
    }

    private fun showImagePickerDialog() {

              openGallery()

    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun takePhotoWithCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, Utils.REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    selectedImageUri = data?.data
                    selectedImageUri?.let { uri ->
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                            profileImageView.setImageBitmap(bitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
                Utils.REQUEST_IMAGE_CAPTURE -> {
                    data?.extras?.let { extras ->
                        val imageBitmap = extras.get("data") as Bitmap
                        // Here you might want to save the bitmap to a file or display it
                        profileImageView.setImageBitmap(imageBitmap)
                        // Optional: Convert Bitmap to Uri if you need to upload it
                        selectedImageUri = getImageUri(imageBitmap)
                    }
                }
            }
        }
    }

    private fun getImageUri(bitmap: Bitmap): Uri {
        // Implement a method to convert the Bitmap to a Uri if necessary
        // This can involve saving the bitmap to a file and returning the Uri
        return Uri.EMPTY // Placeholder, replace with actual implementation
    }

    private fun createAnAccount(name: String, email: String, password: String) {
        pd.show()

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser

                val imageRef = storageReference.child("users/${user!!.uid}/profile.jpg")
                imageRef.putFile(selectedImageUri!!).addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val dataMap: Map<String, Any> = mapOf(
                            "userid" to user.uid,
                            "username" to name,
                            "useremail" to email,
                            "status" to "default",
                            "imageUrl" to uri.toString()
                        )

                        firestore.collection("Users").document(user.uid).set(dataMap)
                            .addOnSuccessListener {
                                pd.dismiss()
                                startActivity(Intent(this, SignInActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                pd.dismiss()
                                Toast.makeText(this, "Failed to store user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }.addOnFailureListener { e ->
                    pd.dismiss()
                    showSnackbar("Failed to upload profile image: ${e.message}")
                }
            } else {
                pd.dismiss()
                showSnackbar("Registration Failed: ${task.exception?.message}")
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 1000
    }
}
