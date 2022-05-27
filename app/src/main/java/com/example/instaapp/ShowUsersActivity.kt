package com.example.instaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.Adapter.UserAdapter
import com.example.instaapp.Model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.view.*

class ShowUsersActivity : AppCompatActivity() {

    var id: String = ""
    var title: String = ""

    var userAdapter: UserAdapter?= null
    var userList: List<User>? = null
    var idList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        val intent = intent
        id = intent.getStringExtra("id").toString()
        title = intent.getStringExtra("title").toString()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        var recyclerView: RecyclerView
        recyclerView = findViewById(R.id.recycle_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<User>, false)
        recyclerView.adapter = userAdapter

        idList = ArrayList()

        when(title){
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()
        }
    }

    private fun getViews() {

        val ref= FirebaseDatabase.getInstance().reference
            .child("Story").child(id!!)
            .child(intent.getStringExtra("storyid").toString())
            .child("views")


        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (idList as ArrayList<String>).clear()

                for(datasnapshot in snapshot.children)
                {
                    (idList as ArrayList<String>).add(datasnapshot.key!!)
                }

                showUsers()


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id!!)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                (idList as ArrayList<String>).clear()
                for(snapshot in p0.children){
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }
        })
    }

    private fun getFollowing() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(id!!)
                .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                (idList as ArrayList<String>).clear()
                for(snapshot in p0.children){
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }
        })
    }

    private fun getLikes() {
        val LikesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(id!!)
        LikesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    (idList as ArrayList<String>).clear()
                    for(snapshot in p0.children){
                        (idList as ArrayList<String>).add(snapshot.key!!)
                    }
                    showUsers()
                }
            }
        })
    }

    private fun showUsers(){
        val usersRef=FirebaseDatabase.getInstance().reference.child("Users")//table name:Users
        usersRef.addValueEventListener(object:ValueEventListener
        {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (userList as ArrayList<User>).clear()
                for (snapShot in dataSnapshot.children) {
                    val user = snapShot.getValue(User::class.java)

                    for (id in idList!!){
                        if (user!!.getUid() == id) {
                            (userList as ArrayList<User>).add(user!!)
                        }
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }
        })
    }
}