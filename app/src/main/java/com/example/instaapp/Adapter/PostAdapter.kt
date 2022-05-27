package com.example.instaapp.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.AddCommentActivity
import com.example.instaapp.Fragments.PostDetailFragment
import com.example.instaapp.Fragments.ProfileFragment
import com.example.instaapp.MainActivity
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
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_add_post.*


class PostAdapter
    (private val mContext:Context,private  val mPost:List<Post>):RecyclerView.Adapter<PostAdapter.ViewHolder>()
{
    private var firebaseUser:FirebaseUser?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(mContext).inflate(R.layout.posts_layout,parent,false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
       return  mPost.size
    }

    //code for events
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser= FirebaseAuth.getInstance().currentUser
        val post=mPost[position]
        val postid=post.getPostId()

        Picasso.get().load(post.getPostImage()).into(holder.postImage)
        holder.caption.text=post.getCaption()
        publisherInfo(holder.profileImage,holder.username,holder.publisher,post.getPublisher())
        isLiked(post.getPostId(),holder.likeButton,holder.postImage)
        isSaved(post.getPostId(),holder.saveButton)
        getCountofLikes(post.getPostId(),holder.likes)
        getComments(post.getPostId(),holder.comments)

        holder.publisher.setOnClickListener {

            val intent = Intent(mContext, MainActivity::class.java).apply {
                putExtra("PUBLISHER_ID", post.getPublisher())
            }
            mContext.startActivity(intent)
        }

        holder.profileImage.setOnClickListener {

            val intent = Intent(mContext, MainActivity::class.java).apply {
                putExtra("PUBLISHER_ID", post.getPublisher())
            }
            mContext.startActivity(intent)
        }

        holder.username.setOnClickListener {

            val intent = Intent(mContext, MainActivity::class.java).apply {
                putExtra("PUBLISHER_ID", post.getPublisher())
            }
            mContext.startActivity(intent)
        }

        holder.postImage.setOnClickListener {
            if (holder.postImage.tag.toString() == "like") {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getPostId())
                    .child(firebaseUser!!.uid)
                    .setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getPostId())
                    .child(firebaseUser!!.uid)
                    .removeValue()
            }

        }

        holder.postImage.setOnClickListener{
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("postid", post.getPostId())
            editor.apply()

            (mContext as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, PostDetailFragment()).commit()
        }

        holder.publisher.setOnClickListener {
            val editor=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("profileid",post.getPublisher())
            editor.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("postId", post.getPostId())
            editor.apply()

            (mContext as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, PostDetailFragment()).commit()
        }

        holder.likeButton.setOnClickListener{
            if (holder.likeButton.tag.toString()=="like")
            {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getPostId())
                    .child(firebaseUser!!.uid)
                    .setValue(true)
                pushNotification(post.getPostId(),post.getPublisher())
            }
            else
            {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getPostId())
                    .child(firebaseUser!!.uid)
                    .removeValue()
            }
        }

        holder.comments.setOnClickListener {

            val intent = Intent(mContext,AddCommentActivity::class.java).apply {
                putExtra("POST_ID",postid)
            }
            mContext.startActivity(intent)
        }

        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id",post.getPostId())
            intent.putExtra("title","likes")
            mContext.startActivity(intent)
        }

        holder.commentButton.setOnClickListener {

            val intent = Intent(mContext,AddCommentActivity::class.java).apply {
                putExtra("POST_ID",postid)
            }
            mContext.startActivity(intent)
        }

        holder.saveButton.setOnClickListener {
            if(holder.saveButton.tag=="Save")
            {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid).child(post.getPostId()).setValue(true)
                Toast.makeText(mContext,"Post Saved",Toast.LENGTH_SHORT).show()

            }
            else
            {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid).child(post.getPostId()).removeValue()
                Toast.makeText(mContext,"Post Unsaved",Toast.LENGTH_SHORT).show()
            }

        }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var profileImage:CircleImageView
        var postImage:ImageView
        var likeButton:ImageView
        var commentButton:ImageView
        var saveButton:ImageView
        var likes:TextView
        var comments:TextView
        var username:TextView
        var publisher:TextView
        var caption:TextView


        init {
            profileImage=itemView.findViewById(R.id.publisher_profile_image_post)
            postImage=itemView.findViewById(R.id.post_image_home)
            likeButton=itemView.findViewById(R.id.post_image_like_btn)
            saveButton=itemView.findViewById(R.id.post_save_comment_btn)
            commentButton=itemView.findViewById(R.id.post_image_comment_btn)
            likes=itemView.findViewById(R.id.likes)
            comments=itemView.findViewById(R.id.comments)
            username=itemView.findViewById(R.id.publisher_user_name_post)
            publisher=itemView.findViewById(R.id.publisher)
            caption=itemView.findViewById(R.id.caption)

        }

    }

    private fun getComments(postid:String, comment:TextView) {

        val commentRef=FirebaseDatabase.getInstance().reference.child("Comment").child(postid)

        commentRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                comment.text = "View all "+datasnapshot.childrenCount.toString()+" comments"
            }
        })
    }

    private fun pushNotification(postid:String, userid:String) {

        val ref = FirebaseDatabase.getInstance().reference.child("Notification").child(userid)

        val notifyMap = HashMap<String, Any>()
        notifyMap["userid"] = FirebaseAuth.getInstance().currentUser!!.uid
        notifyMap["text"] = "♥liked your post♥"
        notifyMap["postid"] = postid
        notifyMap["ispost"] = true

        ref.push().setValue(notifyMap)
    }


    private fun isLiked(postid:String,imageView: ImageView,postedImg:ImageView) {

        firebaseUser=FirebaseAuth.getInstance().currentUser
        val postRef=FirebaseDatabase.getInstance().reference.child("Likes").child(postid)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                if (datasnapshot.child(firebaseUser!!.uid).exists()) {
                    imageView.setImageResource(R.drawable.heart_clicked)
                    postedImg.tag =" liked"
                    imageView.tag = "liked"
                }
                else {
                    imageView.setImageResource(R.drawable.heart_not_clicked)
                    postedImg.tag = "like"
                    imageView.tag = "like"
                }
            }
        })
    }

    private fun getCountofLikes(postid:String,likesNo: TextView) {

        val postRef=FirebaseDatabase.getInstance().reference.child("Likes").child(postid)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                likesNo.text = datasnapshot.childrenCount.toString()+" likes"
            }
        })
    }

    private fun isSaved(postid:String,imageView: ImageView) {

        val savesRef= FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)

        savesRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.child(postid).exists()){
                    imageView.setImageResource(R.drawable.saved_post_filled)
                    imageView.tag="Saved"
                }
                else
                {
                    imageView.setImageResource(R.drawable.save_post_unfilled)
                    imageView.tag="Save"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun publisherInfo(profileImage: CircleImageView, username: TextView, publisher: TextView, publisherID: String) {

        val userRef=FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
        userRef.addValueEventListener(object :ValueEventListener
        {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profileImage)
                    username.text =(user.getUsername())
                    publisher.text =(user.getUsername())

                }
            }

        })
    }

}