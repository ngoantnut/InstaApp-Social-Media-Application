package com.example.instaapp.Model

class User {
    private  var username:String=""
    private  var uid:String=""
    private  var bio:String=""
    private  var fullname:String=""
    private  var image:String=""

    constructor()

    constructor(username:String,fullname:String,bio:String,uid:String,image:String)
    {
        this.username=username
        this.uid=uid
        this.fullname=fullname
        this.bio=bio
        this.image=image
    }

    //getters and Setters
    fun getUsername():String{
        return username
    }
    fun setUsername(username:String){
        this.username= username
    }

    fun getUid():String{
        return uid
    }
    fun setUid(uid:String){
        this.uid= uid
    }

    fun getFullname():String{
        return fullname
    }
    fun setFullname(fullname:String){
        this.fullname=fullname
    }

    fun getBio():String{
        return bio
    }
    fun setBio(bio:String){
        this.bio= bio
    }

    fun getImage():String{
        return image
    }
    fun setImage(image:String){
        this.image= image
    }
}