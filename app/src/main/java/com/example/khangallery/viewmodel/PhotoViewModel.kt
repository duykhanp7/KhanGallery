package com.example.khangallery.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.khangallery.model.Photo
import com.google.firebase.database.*

class PhotoViewModel : ViewModel() {

    //PROPERTIES
    private var listPhotosLiveData:MutableLiveData<ArrayList<Photo>> = MutableLiveData()
    private var listPhotos:ArrayList<Photo> = ArrayList()
    private var database:FirebaseDatabase
    private var databaseReference:DatabaseReference


    //INITIALIZE
    init {
        listPhotosLiveData.value = listPhotos
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("KhanImage")
        loadDataFromFirebase()
    }

    //ADD PHOTO
    fun addPhoto(a:Photo) {
        listPhotos.add(a)
        listPhotosLiveData.value = listPhotos
    }

    //ADD LIST PHOTOS
    fun setPhotos(listPhotosTemp:ArrayList<Photo>){
        listPhotos.addAll(listPhotosTemp)
        listPhotosLiveData.value = listPhotos
    }

    //RETURN MUTABLE LIST LIVE DATA
    fun getMutableLiveData():MutableLiveData<ArrayList<Photo>>{
        return this.listPhotosLiveData
    }

    //LOAD DATA FROM FIREBASE REALTIME DATABASE
    private fun loadDataFromFirebase() {
        databaseReference.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.i("AAA","ITEM ADDED")
                val map:HashMap<String, String> = snapshot.value as HashMap<String, String>
                val photo:Photo = Photo(map["id"]!!,map["title"], map["path"]!!,map["date"]!!,map["type"]!!)
                listPhotos.add(photo)
                listPhotosLiveData.value = listPhotos
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}