package com.example.chatmessenger.mvvm

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatmessenger.Utils
import com.example.chatmessenger.modal.Messages
import com.example.chatmessenger.modal.Users
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineExceptionHandler

class ChatAppViewModel : ViewModel() {

    val message = MutableLiveData<String>()
    val firestore = FirebaseFirestore.getInstance()
    val name = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String>()

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    init {
        getCurrentUser()
    }

    fun getUsers(): LiveData<List<Users>> {
        val usersLiveData = MutableLiveData<List<Users>>()
        firestore.collection("Users")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("ChatAppViewModel", "Error fetching users: ${exception.message}")
                    return@addSnapshotListener
                }
                val usersList = mutableListOf<Users>()
                snapshot?.documents?.forEach { document ->
                    val user = document.toObject(Users::class.java)
                    user?.let { usersList.add(it) }
                }
                usersLiveData.value = usersList
            }
        return usersLiveData
    }

    fun sendMessage(sender: String, receiver: String, friendname: String, friendimage: String) =
        viewModelScope.launch(Dispatchers.IO) {

            val hashMap = hashMapOf<String, Any>(
                "sender" to sender,
                "receiver" to receiver,
                "message" to message.value!!,
                "time" to Utils.getTime()
            )

            val uniqueId = listOf(sender, receiver).sorted().joinToString(separator = "")

            firestore.collection("Messages").document(uniqueId).collection("chats")
                .document(Utils.getTime()).set(hashMap).addOnCompleteListener { taskmessage ->
                    // Handle message addition result if necessary
                }
        }

    fun getMessages(friendid: String): LiveData<List<Messages>> {
        val messages = MutableLiveData<List<Messages>>()

        val uniqueId = listOf(Utils.getUidLoggedIn(), friendid).sorted().joinToString(separator = "")
        firestore.collection("Messages").document(uniqueId).collection("chats")
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }

                val messagesList = mutableListOf<Messages>()
                if (snapshot != null && !snapshot.isEmpty) {
                    snapshot.documents.forEach { document ->
                        val messageModel = document.toObject(Messages::class.java)
                        if (messageModel != null && (
                                    (messageModel.sender == Utils.getUidLoggedIn() && messageModel.receiver == friendid) ||
                                            (messageModel.sender == friendid && messageModel.receiver == Utils.getUidLoggedIn())
                                    )) {
                            messagesList.add(messageModel)
                        }
                    }
                    messages.value = messagesList
                }
            }

        return messages
    }

    fun getCurrentUser() = viewModelScope.launch(Dispatchers.IO) {
        firestore.collection("Users").document(Utils.getUidLoggedIn())
            .addSnapshotListener { value, error ->
                if (value != null && value.exists()) {
                    val users = value.toObject(Users::class.java)
                    name.value = users?.username!!
                    imageUrl.value = users?.imageUrl!!
                }
            }
    }
    fun updateProfile() = viewModelScope.launch(Dispatchers.IO) {
        val hashMapUser = hashMapOf<String, Any>("username" to name.value!!, "imageUrl" to imageUrl.value!!)
        firestore.collection("Users").document(Utils.getUidLoggedIn()).update(hashMapUser).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(Utils.context, "Updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("Users").document(userId).delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
