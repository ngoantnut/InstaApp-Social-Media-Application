package com.example.instaapp.Model

class Post {
    private var postid:String=""
    private var postimage:String=""
    private var publisher:String=""
    private var caption:String=""

    constructor()

    constructor(postid: String, postimage: String, publisher: String, caption: String) {
        this.postid = postid
        this.postimage = postimage
        this.publisher = publisher
        this.caption = caption
    }

    //getters
    fun getPostId():String{
        return postid
    }

    fun getPostImage():String{
        return postimage
    }
    fun getPublisher():String{
        return publisher
    }
    fun getCaption():String{
        return caption
    }

    //setters
    fun setPostId(postid: String)
    {
        this.postid=postid
    }

    fun setPostImage(postimage: String)
    {
        this.postimage=postimage
    }

    fun setPublisher(publisher: String)
    {
        this.publisher=publisher
    }

    fun setCaption(caption: String)
    {
        this.caption=caption
    }
}