package com.example.instaapp.Model

class Comment {
    private var publisher:String=""
    private var comment:String=""

    constructor()

    constructor(publisher: String, comment: String) {
        this.publisher = publisher
        this.comment = comment
    }

    fun getPublisher():String{
        return publisher
    }
    fun getComment():String{
        return comment
    }

    fun setPublisher(publisher: String)
    {
        this.publisher=publisher
    }

    fun setComment(comment: String)
    {
        this.comment=comment
    }
}