package com.example.instaapp.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.Adapter.UserAdapter
import com.example.instaapp.Model.User
import com.example.instaapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*

class SearchFragment : Fragment() {

    private var recyclerView:RecyclerView?=null
    private var userAdapter:UserAdapter?=null
    private var mUser:MutableList<User>?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        recyclerView = view.findViewById(R.id.recyclerview_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        //to show a user on search
        userAdapter = context?.let { UserAdapter(it, mUser as ArrayList<User>, true)}
        recyclerView?.adapter = userAdapter

        view.searchitem.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (searchitem.text.toString() == "") {
                } else {
                    recyclerView?.visibility = View.VISIBLE
                    retrieveUser()
                    searchUser(s.toString().toLowerCase())
                }
            }
        })
        return view
    }

    private fun searchUser(input:String) {

        val query=FirebaseDatabase.getInstance().reference
            .child("Users")
            .orderByChild("username")
            .startAt(input)
            .endAt(input + "\uf8ff")

        query.addValueEventListener(object:ValueEventListener
        {
            override fun onCancelled(error: DatabaseError) {

            }
            override fun onDataChange(datasnapshot: DataSnapshot) {
                mUser?.clear()

                for(snapshot in datasnapshot.children)
                {
                    //searching all users
                    val user=snapshot.getValue(User::class.java)
                    if(user!=null)
                    {
                        mUser?.add(user)
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }
        })
    }

    private fun retrieveUser()
    {
        val usersSearchRef=FirebaseDatabase.getInstance().reference.child("Users")//table name:Users
        usersSearchRef.addValueEventListener(object:ValueEventListener
        {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"Could not read from Database",Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (view!!.searchitem.text.toString().equals("")) {
                    mUser?.clear()
                    for (snapShot in dataSnapshot.children) {
                        val user = snapShot.getValue(User::class.java)
                        if (user != null) {
                            mUser?.add(user)
                        }
                        userAdapter?.notifyDataSetChanged()
                    }
                }
            }
        })
    }
}