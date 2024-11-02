@file:Suppress("DEPRECATION")

package com.example.chatmessenger.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.chatmessenger.R
import com.example.chatmessenger.Utils
import com.example.chatmessenger.activities.SignInActivity
import com.example.chatmessenger.databinding.FragmentSettingBinding
import com.example.chatmessenger.mvvm.ChatAppViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.*

class SettingFragment : Fragment() {

    // ViewModel instance for managing UI-related data
    lateinit var viewModel: ChatAppViewModel
    // View binding for the layout
    lateinit var binding : FragmentSettingBinding

    // Firebase Storage reference
    private lateinit var storageRef: StorageReference
    // Firebase Storage instance
    lateinit var storage: FirebaseStorage
    // Variable to hold the image URI
    var uri: Uri? = null

    // Bitmap to hold the image data
    lateinit var bitmap: Bitmap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize the ViewModel
        viewModel = ViewModelProvider(this).get(ChatAppViewModel::class.java)
        // Bind the ViewModel to the lifecycle owner and the binding
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        // Observe the image URL from the ViewModel
        viewModel.imageUrl.observe(viewLifecycleOwner, Observer {
            loadImage(it) // Load the image when the URL changes
        })

        // Set a click listener for the back button to navigate to the home fragment
        binding.settingBackBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_settingFragment_to_homeFragment)
        }
        // Set a click listener for the update button to update the user's profile
        binding.settingUpdateButton.setOnClickListener {
            viewModel.updateProfile()
        }
        // Set a click listener for the update image button to choose a new profile picture
        binding.settingUpdateImage.setOnClickListener {
                        pickImageFromGallery() // Open gallery to choose an image
          // Show the dialog to choose an image
        }
        // Set a click listener for the delete button to show a confirmation dialog
        binding.settingdeleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }
    // Load the image using Glide library
    private fun loadImage(imageUrl: String) {
        Glide.with(requireContext()).load(imageUrl).placeholder(R.drawable.person).dontAnimate()
            .into(binding.settingUpdateImage)
    }
    @SuppressLint("QueryPermissionsNeeded")
    // Pick an image from the gallery
    private fun pickImageFromGallery() {
        val pickPictureIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (pickPictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(pickPictureIntent, Utils.REQUEST_IMAGE_PICK)
        }
    }
    // Open camera to take a photo

    // Handle the result of image selection or photo capture
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                Utils.REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    uploadImageToFirebaseStorage(imageBitmap) // Upload captured image
                }
                Utils.REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    val imageBitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, imageUri)
                    uploadImageToFirebaseStorage(imageBitmap) // Upload selected image
                }
            }
        }
    }

    // Upload the image to Firebase Storage
    private fun uploadImageToFirebaseStorage(imageBitmap: Bitmap?) {
        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos) // Compress the image
        val data = baos.toByteArray() // Convert the image to byte array
        bitmap = imageBitmap!! // Save the bitmap for later use
        binding.settingUpdateImage.setImageBitmap(imageBitmap) // Display the selected image
        // Create a reference to store the image in Firebase Storage
        val storagePath = storageRef.child("Photos/${UUID.randomUUID()}.jpg")
        val uploadTask = storagePath.putBytes(data) // Start the upload task
        uploadTask.addOnSuccessListener {
            // Get the download URL after successful upload
            val task = it.metadata?.reference?.downloadUrl
            task?.addOnSuccessListener {
                uri = it
                viewModel.imageUrl.value = uri.toString() // Update the image URL in ViewModel
            }

            // Show success message
            Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            // Show error message if upload fails
            Toast.makeText(context, "Failed to upload image!", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onResume() {
        super.onResume()
        // Observe the image URL again to ensure the image updates on resume
        viewModel.imageUrl.observe(viewLifecycleOwner, Observer {
            loadImage(it)
        })
    }
    // Show confirmation dialog before deleting the account
    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmer la suppression")
        builder.setMessage("Êtes-vous sûr de vouloir supprimer votre compte ?")
        builder.setPositiveButton("Oui") { _, _ ->
            deleteUser() // Call deleteUser function if confirmed
        }
        builder.setNegativeButton("Non", null) // Dismiss dialog if not confirmed
        builder.show()
    }

    // Delete the user account
    private fun deleteUser() {
        // Utiliser l'ID de l'utilisateur connecté pour supprimer le compte
        val userId = Utils.getUidLoggedIn() // Assurez-vous que cette méthode renvoie le bon ID
        val currentUser = Utils.auth.currentUser // Obtenir l'utilisateur actuel

        if (currentUser != null) {
            viewModel.deleteUser(userId,
                onSuccess = {
                    // Supprimer l'email de l'authentification
                    currentUser.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Compte supprimé avec succès.", Toast.LENGTH_SHORT).show()
                                // Rediriger vers l'écran de connexion après la suppression du compte
                                Utils.auth.signOut() // Déconnexion de l'utilisateur
                                startActivity(Intent(requireContext(), SignInActivity::class.java))
                                activity?.finish() // Fermer l'activité actuelle
                            } else {
                                // Afficher un message d'erreur si la suppression de l'email échoue
                                Toast.makeText(context, "Erreur lors de la suppression du compte : ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                },
                onFailure = { exception ->
                    // Afficher un message d'erreur si la suppression échoue
                    Toast.makeText(context, "Erreur lors de la suppression du compte : ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show()
        }
    }

}

