package com.example.instaapp.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.Adapter.PostAdapter
import com.example.instaapp.Model.Comment
import com.example.instaapp.Model.Post
import com.example.instaapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostDetailFragment : Fragment() {

    private var postAdapter: PostAdapter?=null
    private var postList:MutableList<Post>?=null
    private var postid:String?=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_post_detail, container, false)

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        postid=pref?.getString("postid","none")

        var recyclerView:RecyclerView?=null
        recyclerView=view.findViewById(R.id.recyclerview_postdetail)
        val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager=linearLayoutManager

        postList=ArrayList()
        postAdapter=context?.let { PostAdapter(it,postList as ArrayList<Post>) }
        recyclerView.adapter=postAdapter

        readPosts(postid)

        return view
    }

    private fun readPosts(postid: String?) {
        val postRef= FirebaseDatabase.getInstance().reference.child("Posts").child(postid!!)

        Log.d("Post id",postid)
        postRef.addValueEventListener(object : ValueEventListener
        {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot)
            {
                postList?.clear()
                val post: Post? = p0.getValue(Post::class.java)
                postList!!.add(post!!)
                postAdapter!!.notifyDataSetChanged()
                }
            })
        }
}