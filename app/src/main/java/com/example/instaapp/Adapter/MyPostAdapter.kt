package com.example.instaapp.Adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings.Global.putString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.Fragments.PostDetailFragment
import com.example.instaapp.Fragments.ProfileFragment
import com.example.instaapp.MainActivity
import com.example.instaapp.Model.Post
import com.example.instaapp.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.mypost_layout.view.*

class MyPostAdapter(private val mContext: Context, private  val mPost:List<Post>): RecyclerView.Adapter<MyPostAdapter.ViewHolder>() {

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var postedImg: ImageView
        init
        {
            postedImg = itemView.findViewById(R.id.my_posted_picture)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(mContext).inflate(R.layout.mypost_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post=mPost[position]

        Picasso.get().load(post.getPostImage()).into(holder.postedImg)
        holder.postedImg.setOnClickListener {

                val pref=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
                pref.putString("postid",post.getPostId())
                pref.apply()

                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PostDetailFragment()).commit()
        }
    }
}