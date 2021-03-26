package com.example.mytasks.service.model

class TaskModel (
    var userId: String? = null,
    var task: String? = null,
    var priority: Int? = null,
    var complete: Boolean? = false,
    var date: Long? = null,
    var description: String? = null,
    var image: String? = null
) : CommonModel()

//    constructor() {}
//
//    constructor(
//        userId: String,
//        task: String,
//        priority: Int,
//        complete: Boolean,
//        date: Long,
//        description: String,
//        image: String
//    ) {
//        this.userId = userId
//        this.task = task
//        this.priority = priority
//        this.complete = complete
//        this.date = date
//        this.description = description
//        this.image = image
//    }

