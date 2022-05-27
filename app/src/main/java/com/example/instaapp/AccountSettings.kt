package com.example.instaapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.instaapp.Model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*

class AccountSettings : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker=""
    private  var myUrl=""
    private  var imageUri: Uri?=null
    private  var storageProfileRef:StorageReference?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfileRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")
        getUserInfo()

        accountSettings_logoutbtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettings, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        close_button.setOnClickListener {
            val intent=Intent(this@AccountSettings,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }


        accountSettings_change_profile.setOnClickListener {
             checker="clicked"

            CropImage.activity()
                .setAspectRatio(1,1)
                .start(this@AccountSettings)
        }

        save_edited_info.setOnClickListener {
            if (checker == "clicked")
            {
              uploadProfileImageandInfo()
            }
            else
            {
                 updateUserInfoOnly()
            }

        }
    }

    private fun uploadProfileImageandInfo() {

        when {
            imageUri == null -> Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT)
                .show()

            TextUtils.isEmpty(accountSettings_fullname_profile.text.toString()) -> {
                Toast.makeText(this, "Full Name is required", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(accountSettings_username_profile.text.toString()) -> {
                Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val progressDialog=ProgressDialog(this)
                progressDialog.setTitle("Profile Settings")
                progressDialog.setMessage("Please wait! Updating...")
                progressDialog.show()

                val fileRef = storageProfileRef!!.child(firebaseUser!!.uid+ ".png")

                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful)
                    {
                        task.exception?.let {
                            throw it
                            Toast.makeText(this, "exception:--"+it, Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener ( OnCompleteListener<Uri>{task ->
                    if(task.isSuccessful)
                    {
                        val downloadUrl=task.result
                        myUrl=downloadUrl.toString()

                        val ref=FirebaseDatabase.getInstance().reference.child("Users")
                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = accountSettings_fullname_profile.text.toString()
                        userMap["username"] = accountSettings_username_profile.text.toString().toLowerCase()
                        userMap["bio"] = accountSettings_bio_profile.text.toString()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)
                        Toast.makeText(this, "Account is updated", Toast.LENGTH_SHORT).show()

                        val intent=Intent(this@AccountSettings,MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()

                    }
                    else {
                        progressDialog.dismiss()
                    }
                } )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ) {
            if (resultCode == Activity.RESULT_OK) {
                val result = CropImage.getActivityResult(data)
                if (data != null){
                    imageUri= result.uri
                    accountSettings_image_profile.setImageURI(imageUri)
                }
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                val error = result.error
            }
        }
    }

    private fun updateUserInfoOnly() {

        when {
            TextUtils.isEmpty(accountSettings_fullname_profile.text.toString()) -> {
                Toast.makeText(this, "Full Name is required", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(accountSettings_username_profile.text.toString()) -> {
                Toast.makeText(this, "username is required", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
                //using hashmap to store values
                val userMap = HashMap<String, Any>()
                userMap["fullname"] = accountSettings_fullname_profile.text.toString()
                userMap["username"] = accountSettings_username_profile.text.toString().toLowerCase()
                userMap["bio"] = accountSettings_bio_profile.text.toString()

                userRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this, "Account is updated", Toast.LENGTH_SHORT).show()

                val intent=Intent(this@AccountSettings,MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun getUserInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(accountSettings_image_profile)
                    accountSettings_fullname_profile?.setText(user.getFullname())
                    accountSettings_username_profile?.setText(user.getUsername())
                    accountSettings_bio_profile?.setText(user.getBio())

                }
            }
        })
    }
}