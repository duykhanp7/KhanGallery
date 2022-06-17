package com.example.khangallery.listener

import com.example.khangallery.model.Photo

interface PhotoClickListener {
    fun photoClick(photo:Photo)
    fun photoLongClick(photo: Photo)
}