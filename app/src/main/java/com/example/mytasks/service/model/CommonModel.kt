package com.example.mytasks.service.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
open class CommonModel {
    @Exclude
    var id: String? = null

    fun <T : CommonModel?> withId(id: String): T {
        this.id = id
        return this as T
    }
}