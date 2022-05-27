package com.example.instaapp.Fragments

import android.R.attr.spacing
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.AccountSettings
import com.example.instaapp.Adapter.MyPostAdapter
import com.example.instaapp.Model.Post
import com.example.instaapp.Model.User
import com.example.instaapp.R
import com.example.instaapp.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_comment.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment: Fragment() {
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    var postList:List<Post>?=null
    var myPostAdapter:MyPostAdapter?=null


    var postListSaved:List<Post>?=null
    var myImagesAdapterSavedImg:MyPostAdapter?=null
    var mySavedImg:List<String>?=null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none")!!
        }

        if (profileId == firebaseUser.uid) {
            view.edit_profile_Button.text = "Edit Profile"
        } else if (profileId != firebaseUser.uid) {
            checkFollowOrFollowingButtonStatus()
        }
        //to call account profile setting activity
        view.edit_profile_Button.setOnClickListener {
            val getButtontext = view.edit_profile_Button.text.toString()
            when {
                getButtontext == "Edit Profile" -> startActivity(
                    Intent(
                        context,
                        AccountSettings::class.java
                    )
                )

                getButtontext == "Follow" -> {

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .setValue(true)

                        pushNotification()
                    }

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .setValue(true)
                    }
                }

                getButtontext == "Following" -> {

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .removeValue()
                    }

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .removeValue()
                    }
                }
            }
        }

        view.total_followers.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title","followers")
            startActivity(intent)
        }
        view.total_following.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title","following")
            startActivity(intent)
        }
        //to get own feeds
        var recyclerView: RecyclerView?=null
        recyclerView=view.findViewById(R.id.recyclerview_profile)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(context,3,GridLayoutManager.VERTICAL,false)
        postList=ArrayList()
        myPostAdapter= context?.let { MyPostAdapter(it, postList as ArrayList<Post>) }
        recyclerView.adapter=myPostAdapter

        //Adding recycler view for saved posts
        var recyclerViewSavedImages:RecyclerView
        recyclerViewSavedImages=view.findViewById(R.id.recycler_view_saved_pic)
        recyclerViewSavedImages.setHasFixedSize(true)
        val linearLayoutManager2:LinearLayoutManager=GridLayoutManager(context,3)
        recyclerViewSavedImages.layoutManager=linearLayoutManager2

        postListSaved=ArrayList()
        myImagesAdapterSavedImg=context?.let { MyPostAdapter( it,postListSaved as ArrayList<Post>) }
        recyclerViewSavedImages.adapter=myImagesAdapterSavedImg

        //Default
        recyclerViewSavedImages.visibility=View.GONE
        recyclerView.visibility=View.VISIBLE

        //To view savedimages button function
        val uploadedImagesBtn: ImageButton
        uploadedImagesBtn=view.findViewById(R.id.postGrid)
        uploadedImagesBtn.setOnClickListener{
            recyclerViewSavedImages.visibility=View.GONE
            recyclerView.visibility=View.VISIBLE
        }


        //To view uploadedimages button function
        val savedImagesBtn: ImageButton
        savedImagesBtn=view.findViewById(R.id.images_save_btn)
        savedImagesBtn.setOnClickListener{
            recyclerViewSavedImages.visibility=View.VISIBLE
            recyclerView.visibility=View.GONE
        }

        //to fill in data in profile page
        getFollowers()
        getFollowing()
        getNoofPosts()
        getUserInfo(view)
        myPosts()
        mySaves()

        return view
    }

    private fun mySaves() {

        mySavedImg=ArrayList()
        val savesRef=FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)
        savesRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(pO in snapshot.children)
                    {
                        ( mySavedImg as ArrayList<String>).add(pO.key!!)
                    }

                    readSavedImagesData()//Following is thr function to get the details of the saved posts
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun readSavedImagesData() {

        val PostsRef=FirebaseDatabase.getInstance().reference.child("Posts")
        PostsRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(datasnapshot.exists())
                {
                    (postListSaved as ArrayList<Post>).clear()

                    for(snapshot in datasnapshot.children)
                    {
                        val post=snapshot.getValue(Post::class.java)

                        for(key in mySavedImg!!)
                        {
                            if (post!!.getPostId()==key)
                            {
                                (postListSaved as ArrayList<Post>).add(post!!)
                            }
                        }
                    }
                    myImagesAdapterSavedImg!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun checkFollowOrFollowingButtonStatus() {

        val followingRef = firebaseUser.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        if (followingRef != null) {
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.child(profileId).exists()) {
                        view?.edit_profile_Button?.text = "Following"
                    } else {
                        view?.edit_profile_Button?.text = "Follow"
                    }
                }
            })
        }
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    view?.total_followers?.text = snapshot.childrenCount.toString()
                }
            }
        })
    }

    private fun getFollowing() {
        val followingsRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")

        followingsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    view?.total_following?.text = snapshot.childrenCount.toString()
                }
            }
        })
    }

    private fun getNoofPosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var i:Int=0
                for(snapshot in p0.children)
                {
                    val post=snapshot.getValue(Post::class.java)
                    if(post!!.getPublisher().equals(profileId))
                    {
                        i=i+1
                    }
                }
                view?.total_posts?.text = ""+i
            }
        })
    }

    private fun myPosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                (postList as ArrayList<Post>).clear()
                for(snapshot in p0.children)
                {
                    val post=snapshot.getValue(Post::class.java)
                    if(post!!.getPublisher().equals(profileId))
                        (postList as ArrayList<Post>).add(post)
                }
                Collections.reverse(postList)
                myPostAdapter!!.notifyDataSetChanged()
            }
        })
    }





    private fun pushNotification() {

        val ref = FirebaseDatabase.getInstance().reference.child("Notification").child(profileId)

        val notifyMap = HashMap<String, Any>()
        notifyMap["userid"] = FirebaseAuth.getInstance().currentUser!!.uid
        notifyMap["text"] = "➱Started following you "
        notifyMap["postid"] = ""
        notifyMap["ispost"] = true

        ref.push().setValue(notifyMap)
    }


    private fun getUserInfo(view: View) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(view.profile_image_profile)
                    view.profile_toolbar_username?.text=user.getUsername()
                    view.fullname_in_profile?.text= user.getFullname()
                    view.username_in_profile?.text= user.getUsername()
                    view.bio_profile?.text= user.getBio()

                }
            }
        })
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
            pref?.putString("profileId", firebaseUser.uid)
            pref?.apply()
        }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }
}