package com.example.khangallery.model


import java.io.Serializable

data class Photo(var id:String,var title: String?, var path:String,var date:String,var type:String,var check:Boolean = false) : Serializable
