package com.example.instaapp.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.Adapter.NotificationAdapter
import com.example.instaapp.Model.Notification
import com.example.instaapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class NotificationFragment : Fragment() {

    private var notificationAdapter: NotificationAdapter?=null
    private var notificationList:MutableList<Notification>?=null
    private  var firebaseUser: FirebaseUser?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_notification, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        var recyclerView: RecyclerView?=null
        recyclerView=view.findViewById(R.id.recyclerview_notification)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager=linearLayoutManager

        notificationList=ArrayList()
        notificationAdapter=context?.let { NotificationAdapter(it,notificationList as ArrayList<Notification>) }
        recyclerView.adapter=notificationAdapter

        readNotification()
        return view
    }

    private fun readNotification() {

        val postRef= FirebaseDatabase.getInstance().reference.child("Notification").child(firebaseUser!!.uid)
        postRef.addValueEventListener(object : ValueEventListener
        {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot)
            {
                notificationList?.clear()
                for (snapshot in p0.children)
                {
                    val notification: Notification? = snapshot.getValue(Notification::class.java)
                    notificationList!!.add(notification!!)
                }
                    Collections.reverse(notificationList)
                    notificationAdapter!!.notifyDataSetChanged()

            }
        })
    }
}