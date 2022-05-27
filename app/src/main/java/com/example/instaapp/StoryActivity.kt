package com.example.instaapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.instaapp.Adapter.StoryAdapter
import com.example.instaapp.Model.Story
import com.example.instaapp.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.activity_story.*

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {

    var currentUserId:String=""
    var userId:String=""
    var counter=0
    var pressTime=0L
    var limit=500L

    var imagesList:List<String>?=null
    var storyIdsList:List<String>?=null

    private val onTouchListener=View.OnTouchListener { view, motionevent ->
        when(motionevent.action)
        {
            MotionEvent.ACTION_DOWN->{

                pressTime=System.currentTimeMillis()
                storiesProgressView!!.pause()
                return@OnTouchListener false

            }
            MotionEvent.ACTION_UP->{

                val now=System.currentTimeMillis()
                storiesProgressView!!.resume()
                return@OnTouchListener limit<now-pressTime
            }
        }

        false
    }

    //The instance for progress view added from github shts StoriesProgressView
    var storiesProgressView:StoriesProgressView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        currentUserId=FirebaseAuth.getInstance().currentUser!!.uid
        userId= intent.getStringExtra("userId").toString()

        storiesProgressView=findViewById(R.id.stories_progress)

        layout_seen.visibility=View.GONE
        story_delete.visibility=View.GONE

        if(userId==currentUserId)
        {
            layout_seen.visibility=View.VISIBLE
            story_delete.visibility=View.VISIBLE
        }
        getStories(userId!!)
        userInfo(userId!!)


        val reverse:View=findViewById(R.id.reverse)
        reverse.setOnClickListener{  //On click
            storiesProgressView!!.reverse()
        }
        reverse.setOnTouchListener(onTouchListener) //on touch


        val skip:View=findViewById(R.id.skip)
        skip.setOnClickListener{  //On click
            storiesProgressView!!.skip()
        }
        skip.setOnTouchListener(onTouchListener) //on touch


        seen_number.setOnClickListener {
            val intent= Intent(this,ShowUsersActivity::class.java)
            intent.putExtra("id",userId)
            intent.putExtra("storyid",storyIdsList!![counter])
            intent.putExtra("title","views")
            startActivity(intent)
        }

        story_delete.setOnClickListener {

            val ref=FirebaseDatabase.getInstance().reference.child("Story").child(userId!!)
                .child(storyIdsList!![counter])

            ref.removeValue().addOnCompleteListener{task->

                if (task.isSuccessful)
                {
                    Toast.makeText(this,"Deleted Sucessfully",Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

    private fun addViewToStory(storyId:String)
    {
        val ref=FirebaseDatabase.getInstance().reference.child("Story").child(userId!!).child(storyId).child("views").child(currentUserId)
            .setValue(true)

    }

    private fun getStories(useId: String)
    {

        imagesList=ArrayList()
        storyIdsList=ArrayList()

        val ref=FirebaseDatabase.getInstance().reference.child("Story").child(userId!!)

        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {

                (imagesList as ArrayList<String>).clear()
                (storyIdsList as ArrayList<String>).clear()

                for(snapshot in datasnapshot.children)
                {
                    val story:Story?=snapshot.getValue<Story>(Story::class.java)
                    val timeCurrent=System.currentTimeMillis()

                    if(timeCurrent>story!!.getTimeStart() && timeCurrent<story.getTimeEnd())
                    {
                        (imagesList as ArrayList<String>).add(story.getImageUrl())
                        (storyIdsList as ArrayList<String>).add(story.getStoryId())


                    }

                }
                storiesProgressView!!.setStoriesCount((imagesList as ArrayList<String>).size)
                storiesProgressView!!.setStoryDuration(7000L)
                storiesProgressView!!.setStoriesListener(this@StoryActivity)
                storiesProgressView!!.startStories(counter)
                Picasso.get().load(imagesList!!.get(counter)).placeholder(R.drawable.profile).into(image_story)

                addViewToStory(storyIdsList!!.get(counter))
                seenNumber(storyIdsList!!.get(counter))


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }


    private fun seenNumber(storyId:String)
    {
        val ref=FirebaseDatabase.getInstance().reference.child("Story").child(userId!!).child(storyId).child("views")

        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                seen_number.text=" "+snapshot.childrenCount

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun userInfo(userid:String)
    {
        val userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(userid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                if(snapshot.exists())
                {
                    val user=snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(story_profile_image)

                    story_username!!.text=user.getUsername()
                }


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onComplete() {
        finish()
    }

    override fun onPrev() {

        if (counter-1<0) return

        Picasso.get().load(imagesList!![--counter]).placeholder(R.drawable.profile).into(image_story)
        seenNumber(storyIdsList!![counter])
    }

    override fun onNext() {

        Picasso.get().load(imagesList!![++counter]).placeholder(R.drawable.profile).into(image_story)
        addViewToStory(storyIdsList!![counter])
        seenNumber(storyIdsList!![counter])


    }


    override fun onDestroy() {
        super.onDestroy()
        storiesProgressView!!.destroy()
    }

    override fun onResume() {
        super.onResume()
        storiesProgressView!!.resume()
    }

    override fun onPause() {
        super.onPause()
        storiesProgressView!!.pause()
    }


}

