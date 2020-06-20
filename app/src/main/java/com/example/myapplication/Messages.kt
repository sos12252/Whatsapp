package com.example.myapplication

class Messages {

//    var from: String? = null
//    var message: String? = null
//    var type: String? = null
//    var to: String? = null
//    var messageID: String? = null
//    var time: String? = null
//    var date: String? = null
//    var name: String? = null

    var from: String? = null
    var message: String? = null
    var type: String? = null
    var to: String? = null
    var messageID: String? = null
    var time: String? = null
    var date: String? = null
    var name: String? = null


    constructor() {}
    constructor(from: String?, message: String?, type: String?, to: String?, messageID: String?, time: String?, date: String?, name: String?) {
        this.from = from
        this.message = message
        this.type = type
        this.to = to
        this.messageID = messageID
        this.time = time
        this.date = date
        this.name = name

    }
//constructor(
//    override val getFrom: String,
//    override val getMessage: String,
//    override val getType:String,
//    override val getTime: String,
//    override val getDate: String,
//    override val getName: String
//)


}