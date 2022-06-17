package com.example.khangallery.database

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.khangallery.R
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.file.Files
import kotlin.coroutines.coroutineContext

class SaveToSpecificFolder(contextTemp:Context){

    private val context = contextTemp


    //SAVE PHOTO TO FOLDER
    fun saveToFolder(bitmap: Bitmap,title:String,folderName:String) {

        val folder:File = File(this.context.getExternalFilesDir(null)!!.absolutePath,folderName)
        folder.mkdir()
        val file:File = File(folder, "$title.jpeg")
        Log.i("AAA","FILE PATH : "+file.absolutePath)
        val outputStream:FileOutputStream = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
        outputStream.flush()
        outputStream.close()
    }


    //REMOVE FILE IN FOLDER
    fun removeFile(file:File) {
        Files.deleteIfExists(file.toPath())
    }

    //RENAME FILE
    fun renameFile(oldName:String, newName:String) {

    }

    //RETURN PATH OF PHOTO
    fun getImagePath(name:String,nameFolder:String):String {
        return context.getExternalFilesDir(null).toString()+"/"+nameFolder+"/"+name+".jpeg"
    }


    //DOWN LOAD FILE PHOTO FROM FIREBASE STORAGE
    fun downloadPhotoFromFirebase(childName:String,folderName:String) {

        //DOWNLOAD PHOTOS
        Thread(Runnable {
            val storageTemp = FirebaseStorage.getInstance()
            val storageReferenceTemp = storageTemp.reference.child(childName)

            storageReferenceTemp.listAll().addOnSuccessListener {
                for (item in it.items){
                    val folder:File = File(context.getExternalFilesDir(null)!!.absolutePath,folderName)
                    folder.mkdir()
                    val file:File = File(folder, "${item.name}.jpeg")
                    if(!file.exists()){
                        item.getFile(file).addOnSuccessListener {
                            Log.i("AAA","LOAD ${item.name} SUCCESS")
                        }
                    }
                }
            }
        }).start()
    }


}