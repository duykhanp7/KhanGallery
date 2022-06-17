package com.example.khangallery.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment.STYLE_NORMAL
import com.example.khangallery.R
import com.example.khangallery.bottomsheet.BottomSheetDialogChangeTitle
import com.example.khangallery.databinding.FragmentPhotoBinding
import com.example.khangallery.listener.OnChangeTitleListener
import com.example.khangallery.model.Photo
import com.example.khangallery.utils.Utils
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class PhotoFragment : Fragment(),OnChangeTitleListener {

    //PROPERTIES
    private lateinit var photo: Photo
    private lateinit var binding:FragmentPhotoBinding
    //REALTIME DATABASE
    private lateinit var database:FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    //STORAGE DATABASE
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    //SQLITE DATABASE
    //BOTTOM SHEET CHANGE TITLE
    private lateinit var bottomSheetChangeInfo:BottomSheetDialogChangeTitle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPhotoBinding.inflate(layoutInflater)
        return binding.root
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photo = arguments?.getSerializable("photo") as Photo
        binding.photo = photo
        //INITIALIZED SQLITE DATABASE
        bottomSheetChangeInfo = BottomSheetDialogChangeTitle(this,photo,Utils.KHAN_FOLDER,Utils.KHAN_FIREBASE)
        //INITIALIZED REALTIME AND STORAGE VARIABLES
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        databaseReference = database.reference.child("KhanImage")
        storageReference = storage.reference.child("KhanImage")

        //EVENT CLICK FOR BUTTON CHANGE NAME OF PHOTO
        binding.changePhotoInfo.setOnClickListener {
            bottomSheetChangeInfo.setStyle(STYLE_NORMAL,R.style.CustomBottomSheetDialog)
            bottomSheetChangeInfo.show(parentFragmentManager,"AAA")
        }


    }

    override fun updateTextTile(newTile:String) {
        binding.textTitlePhoto.post {
            binding.textTitlePhoto.text = newTile
        }
    }

}