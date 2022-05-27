package com.example.instaapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_add_post.*

class AddStoryActivity : AppCompatActivity() {

    private var myUrl = ""

    private var imageUri: Uri? = null

    private var storageStoryRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)


        storageStoryRef = FirebaseStorage.getInstance().reference.child("Story Pictures")




        CropImage.activity()
            .setAspectRatio(9, 16)
            .start(this@AddStoryActivity)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {

            val result = CropImage.getActivityResult(data)
            imageUri = result.uri


            uploadStory()

        }
        else
        {
            Toast.makeText(this,"Some Error Occured!! Try Again",Toast.LENGTH_SHORT).show()
            finish()
        }


    }

    private fun uploadStory() {
        when
        {
            imageUri==null ->{
                Toast.makeText(this,"Please select Image", Toast.LENGTH_SHORT).show()
            }

            else->
            {
                val progressDialog= ProgressDialog(this)
                progressDialog.setTitle("Adding Story")
                progressDialog.setMessage("Please wait while your story is added")
                progressDialog.show()


                val fileRef=storageStoryRef!!.child(System.currentTimeMillis().toString()+".jpg")

                val uploadTask: StorageTask<*>
                uploadTask=fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->

                    if (!task.isSuccessful){
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }

                    return@Continuation fileRef.downloadUrl
                })
                    .addOnCompleteListener ( OnCompleteListener<Uri> {task ->
                        if (task.isSuccessful){

                            val downloadUrl=task.result
                            myUrl=downloadUrl.toString()


                            val ref= FirebaseDatabase.getInstance().reference
                                .child("Story")
                                .child(FirebaseAuth.getInstance().currentUser!!.uid)

                            val storyId=(ref.push().key).toString()

                            val timeEnd=System.currentTimeMillis()+ 86400000 //864000 is the millisec conversion for 24hrs//The timeSpan to expire the story

                            val storymap = HashMap<String, Any>()

                            storymap["userid"] = FirebaseAuth.getInstance().currentUser!!.uid
                            storymap["timestart"] = ServerValue.TIMESTAMP
                            storymap["timeend"] = timeEnd
                            storymap["imageurl"] = myUrl
                            storymap["storyid"] = storyId

                            ref.child(storyId).updateChildren(storymap)

                            Toast.makeText(this, "Story Added!!", Toast.LENGTH_SHORT)
                                .show()

                            finish()


                            progressDialog.dismiss()

                        }
                        else
                        {
                            progressDialog.dismiss()
                        }


                    })





            }
        }
    }
}