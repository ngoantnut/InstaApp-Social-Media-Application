package com.example.instaapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.Adapter.CommentAdapter
import com.example.instaapp.Adapter.PostAdapter
import com.example.instaapp.Model.Comment
import com.example.instaapp.Model.Post
import com.example.instaapp.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_comment.*
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.posts_layout.*

class AddCommentActivity : AppCompatActivity() {

    private var firebaseUser: FirebaseUser?=null
    private var commentAdapter:CommentAdapter?=null
    private var commentList:MutableList<Comment>?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_comment)

        val toolbar=findViewById<androidx.appcompat.widget.Toolbar>( R.id.comments_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Comments"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })



        var recyclerView:RecyclerView?=null
        recyclerView=findViewById(R.id.recyclerview_comments)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager=linearLayoutManager

        commentList=ArrayList()
        commentAdapter= this.let { CommentAdapter(it,commentList as ArrayList<Comment>) }
        recyclerView.adapter=commentAdapter


        firebaseUser= FirebaseAuth.getInstance().currentUser

         val add_comment=findViewById<EditText>(R.id.add_comment)
         val post_comment=findViewById<TextView>(R.id.post_comment)
         val postid = intent.getStringExtra("POST_ID")

        getImage()
        readComments(postid!!)
        getPostImage(postid!!)


        post_comment.setOnClickListener {
            if(add_comment.text.toString().equals(""))
            {
              Toast.makeText(this,"You can't send an empty comment",Toast.LENGTH_SHORT).show()
            }
            else
            {
                postComment(postid!!)
            }
        }

    }

    private fun postComment(postid:String) {

        val commentRef : DatabaseReference = FirebaseDatabase.getInstance().reference.child("Comment").child(postid)

        val commentMap = HashMap<String, Any>()
        commentMap["publisher"] = firebaseUser!!.uid
        commentMap["comment"] = add_comment.text.toString()

        commentRef.push().setValue(commentMap)
        pushNotification(postid)
        add_comment.setText("")
        Toast.makeText(this, "posted!!", Toast.LENGTH_LONG).show()
    }

    private fun getImage() {
        val ref : DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        ref.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(user_profile_image)
                }
            }
        })
    }

    private fun pushNotification(postid: String) {

        val ref = FirebaseDatabase.getInstance().reference.child("Notification").child(firebaseUser!!.uid)

        val notifyMap = HashMap<String, Any>()
        notifyMap["userid"] = FirebaseAuth.getInstance().currentUser!!.uid
        notifyMap["text"] = "commented :"+add_comment.text.toString()
        notifyMap["postid"] = postid
        notifyMap["ispost"] = true

        ref.push().setValue(notifyMap)
    }

    private fun readComments(postid: String) {
        val ref: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Comment").child(postid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                    commentList?.clear()
                    for (snapshot in p0.children) {
                        val cmnt: Comment? = snapshot.getValue(Comment::class.java)
                        commentList!!.add(cmnt!!)
                    }
                        commentAdapter!!.notifyDataSetChanged()
                    }
        })
    }

    private fun getPostImage(postid: String){
        val postRef = FirebaseDatabase.getInstance()
            .reference.child("Posts")
            .child(postid).child("postimage")

        postRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    val image = p0.value.toString()

                    Picasso.get().load(image).placeholder(R.drawable.profile).into(post_image_comment)
                }
            }
        })
    }
}