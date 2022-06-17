package com.example.khangallery.bindingutils

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import androidx.databinding.BindingAdapter
import com.example.khangallery.model.Photo
import com.squareup.picasso.Picasso



//LOAD PHOTO FROM FIREBASE INTO IMAGEVIEW
@BindingAdapter("loadPhoto")
fun loadPhotoFromPath(imageView: ImageView, photo: Photo) {
    if(photo.path.isNotBlank() && photo.path.isNotEmpty()){
        Picasso.get().load(photo.path).fit().centerCrop().into(imageView)
    }
}


@BindingAdapter("hide")
fun showOrHideByChecked(radioButton: RadioButton,state:Boolean) {
    if(state){
        radioButton.post {
            radioButton.visibility = View.VISIBLE
        }
    }
    else{
        radioButton.post {
            radioButton.visibility = View.GONE
        }
    }
}