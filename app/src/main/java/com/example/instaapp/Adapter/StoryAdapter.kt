package com.example.instaapp.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.AddStoryActivity
import com.example.instaapp.MainActivity
import com.example.instaapp.Model.Story
import com.example.instaapp.Model.User
import com.example.instaapp.R
import com.example.instaapp.StoryActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.view.*

class StoryAdapter (private val mContent: Context, private val mStory: List<Story>) :
RecyclerView.Adapter<StoryAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return if (viewType==0)
        {
            val view= LayoutInflater.from(mContent).inflate(R.layout.add_story_item,parent,false)
            ViewHolder(view)
        }
        else
        {
            val view= LayoutInflater.from(mContent).inflate(R.layout.story_item,parent,false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mStory.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val story=mStory[position]

        userInfo(holder,story.getUserId(),position)


        if (holder.adapterPosition!==0)
        {
            seenStory(holder,story.getUserId())
        }
        if (holder.adapterPosition===0)
        {
            myStory(holder.add_story_text!!,holder.story_plus_btn!!, false)
        }



        holder.itemView.setOnClickListener{
            if (holder.adapterPosition===0)
            {
                myStory(holder.add_story_text!!,holder.story_plus_btn!!, true)

            }
            else
            {
                val intent=Intent(mContent,StoryActivity::class.java)
                intent.putExtra("userId",story.getUserId())
                mContent.startActivity(intent)
            }
        }

    }







    inner class ViewHolder(@NonNull itemView: View) :RecyclerView.ViewHolder(itemView)
    {

        //In the following inner class we are acessing two layouts in a single Adapter(Stories/Add Story Function)

        //Story Item
        var story_image_seen: CircleImageView?=null
        var story_image: CircleImageView?=null
        var story_user_name: TextView?=null


        //Add Story Item layout
        var story_plus_btn: ImageView?=null
        var add_story_text: TextView?=null



        init {
            //Story Item
            story_image_seen=itemView.findViewById(R.id.story_image_seen)
            story_image=itemView.findViewById(R.id.story_image)
            story_user_name=itemView.findViewById(R.id.story_username)


            //Add Story Item layout
            story_plus_btn=itemView.findViewById(R.id.story_add)
            add_story_text=itemView.findViewById(R.id.add_story_text)

        }

    }

    override fun getItemViewType(position: Int): Int {
        if(position==0)
        {
            return 0
        }
        return 1
    }

    private fun userInfo(viewHolder: ViewHolder,userid:String,position: Int)
    {
        val userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(userid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                /* if(context!=null)
                 {
                     return
                 }*/

                if(snapshot.exists())
                {
                    val user=snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(viewHolder.story_image)

                    if (position!=0){
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(viewHolder.story_image_seen)
                        viewHolder.story_user_name!!.text=user.getUsername()
                    }
                }


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }


    private fun myStory(textView: TextView,imageView: ImageView,click:Boolean)//to differentiate between story of online users and following users
    {

        val storyRef= FirebaseDatabase.getInstance().reference.child("Story")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        storyRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {

                var counter=0
                var timeCurrent=System.currentTimeMillis()

                for (snapshot in datasnapshot.children)
                {
                    val story= snapshot.getValue(Story::class.java)

                    if(timeCurrent>story!!.getTimeStart() && timeCurrent<story!!.getTimeEnd())
                    {
                        counter++
                    }
                }

                if(click)
                {
                    if (counter>0){
                        val alertDialog=AlertDialog.Builder(mContent).create()
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,"View Story")
                        {
                                dialog, which ->
                            val intent=Intent(mContent,StoryActivity::class.java)
                            intent.putExtra("userId",FirebaseAuth.getInstance().currentUser!!.uid)
                            mContent.startActivity(intent)
                            dialog.dismiss()
                        }
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"Add Story")
                        {
                                dialog, which ->
                            val intent=Intent(mContent,AddStoryActivity::class.java)
                            intent.putExtra("userId",FirebaseAuth.getInstance().currentUser!!.uid)
                            mContent.startActivity(intent)
                            dialog.dismiss()
                        }
                        alertDialog.show()

                    }
                    else
                    {
                        val intent=Intent(mContent,AddStoryActivity::class.java)
                        intent.putExtra("userId",FirebaseAuth.getInstance().currentUser!!.uid)
                        mContent.startActivity(intent)

                    }

                }
                else
                {
                    if (counter>0){

                        textView.text="My Story"
                        imageView.visibility=View.GONE
                    }
                    else
                    {
                        textView.text="Add Story"
                        imageView.visibility=View.VISIBLE
                    }


                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }










    private fun seenStory(viewHolder:ViewHolder,userId: String) //to check whether story is seen or not
    {
        val storyRef= FirebaseDatabase.getInstance().reference.child("Story")
            .child(userId)

        storyRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {

                var i=0

                for(snapshot in datasnapshot.children)
                {
                    if (!snapshot.child("views").child(FirebaseAuth.getInstance().currentUser!!.uid).exists()
                        && System.currentTimeMillis()<snapshot.getValue(Story::class.java)!!.getTimeEnd()) //checking if not seen and not expired
                    {
                        i++


                    }
                }

                if (i>0)
                {
                    viewHolder.story_image!!.visibility= View.VISIBLE
                    viewHolder.story_image_seen!!.visibility= View.GONE
                }
                else
                {
                    viewHolder.story_image!!.visibility= View.GONE
                    viewHolder.story_image_seen!!.visibility= View.VISIBLE

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}