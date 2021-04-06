package com.example.mytasks.service.model

import java.io.Serializable

data class TaskModel (
    var userId: String? = null,
    var task: String? = null,
    var priority: Int? = null,
    var complete: Boolean? = null,
    var date: Long? = null,
    var description: String? = null,
    var image: String? = null,
    var namePhoto: String? = null,
    var notificationId: Int? = null
) : CommonModel(), Serializable


