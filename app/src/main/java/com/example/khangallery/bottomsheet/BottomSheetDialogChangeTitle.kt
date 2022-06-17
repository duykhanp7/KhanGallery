package com.example.khangallery.bottomsheet

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.khangallery.R
import com.example.khangallery.database.SaveToSpecificFolder
import com.example.khangallery.databinding.BottomSheetChangeInfomationPhotoBinding
import com.example.khangallery.listener.OnChangeTitleListener
import com.example.khangallery.model.Photo
import com.example.khangallery.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class BottomSheetDialogChangeTitle(onChangeTitleListenerTemp: OnChangeTitleListener, photoTemp:Photo,folderNameLocalTemp:String, folderNameFirebaseTemp:String):BottomSheetDialogFragment() {

    private var photo: Photo = photoTemp
    private lateinit var binding:BottomSheetChangeInfomationPhotoBinding
    //FIREBASE DATABASE
    private lateinit var storage: FirebaseStorage
    private lateinit var database: FirebaseDatabase
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    //SAVE TO SPECIFIC FOLDER
    private lateinit var saveToSpecificFolder: SaveToSpecificFolder
    //ON TITLE CHANGE LISTENER
    private val onChangeTitleListener:OnChangeTitleListener = onChangeTitleListenerTemp
    //FOLDER NAME
    private var folderNameLocal = folderNameLocalTemp
    private var folderNameFirebase = folderNameFirebaseTemp
    //ON CREATE VIEW
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetChangeInfomationPhotoBinding.inflate(layoutInflater)
        return binding.root
    }


    //ON VIEW CREATED
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //INITIALIZED
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        storageReference = storage.reference.child(folderNameFirebase)
        databaseReference = database.reference.child(folderNameFirebase)
        //SAVE TO SPECIFIC FOLDER
        saveToSpecificFolder = SaveToSpecificFolder(requireContext())

        binding.photo = photo


        //EVENT CLICK FOR BUTTON AGREE
        binding.buttonChangeTitle.setOnClickListener {
            val newTitle = binding.editText.text.toString()
            changePhotoInformation(photo,newTitle,folderNameLocal)
            onChangeTitleListener.updateTextTile(newTitle)
            dismiss()
            Toast.makeText(context,"Change title successfully !",Toast.LENGTH_LONG).show()
        }

    }



    //SET PHOTO
    fun setPhoto(a:Photo) {
        this.photo = a
        binding.photo = a
    }

    //CHANGE PHOTO INFORMATION
    private fun changePhotoInformation(a:Photo,b:String,nameFolderLocal:String) {
        //UPDATE IN STORAGE AND REALTIME DATABASE
        updateInStorageDatabase(a,b,nameFolderLocal)
        //UPDATE IN SQLITE DATABASE
        updateInSpecificFolder(a,b,nameFolderLocal)
    }

    //UPDATE IN STORAGE DATABASE
    private fun updateInStorageDatabase(a:Photo, b:String, nameFolderLocal: String) {
        //DELETE OLD PHOTO TO STORAGE DATABASE
        storageReference.child(a.title.toString()).delete()
        val file = File(saveToSpecificFolder.getImagePath(a.title.toString(),nameFolderLocal))
        val reference = storageReference.child(b)
        //ADD NEW PHOTO TO STORAGE DATABASE
        reference.putFile(Uri.fromFile(file))
            .addOnSuccessListener {
                reference.downloadUrl.addOnCompleteListener {
                    //UPDATE IN REALTIME DATABASE
                    updateInRealtimeDatabase(a,b,it.result)
                }
            }
            .addOnFailureListener{
                Log.i("AAA","FAILURE UPDATE IN STORAGE")
            }
    }
    //UPDATE IN REALTIME DATABASE
    private fun updateInRealtimeDatabase(a:Photo,b:String,uri: Uri) {
        //REMOVE OLD PHOTO TO REALTIME DATABASE
        databaseReference.child(a.title.toString()).removeValue()
        //ADD NEW PHOTO TO REALTIME DATABASE
        val photo:Photo = Photo(a.id,b,uri.toString(),a.date,a.type)
        databaseReference.child(b).setValue(photo)
            .addOnSuccessListener {
                Log.i("AAA","SUCCESS UPDATE IN REALTIME")
            }
            .addOnFailureListener {
                Log.i("AAA","FAILURE UPDATE IN REALTIME")
            }
    }
    //UPDATE N SQLITE DATABASE
    private fun updateInSpecificFolder(a:Photo,b:String,nameFolder:String) {
        val oldFileName = File(saveToSpecificFolder.getImagePath(a.title.toString(),nameFolder))
        val newFileName = File(saveToSpecificFolder.getImagePath(b, nameFolder))
        //CHANGE OLD NAME TO NEW NAME
        oldFileName.renameTo(newFileName)
    }

}