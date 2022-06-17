package com.example.khangallery.fragment

import android.R.attr
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.khangallery.R
import com.example.khangallery.adapter.PhotoAdapter
import com.example.khangallery.database.SaveToSpecificFolder
import com.example.khangallery.databinding.FragmentHomeBinding
import com.example.khangallery.listener.PhotoClickListener
import com.example.khangallery.main.MainActivity
import com.example.khangallery.main.MainActivity.Companion.photoAdapter
import com.example.khangallery.model.Photo
import com.example.khangallery.utils.Utils
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import java.io.File
import java.io.FileOutputStream
import java.util.*


class HomeFragment() : Fragment(),PhotoClickListener {

    //PROPERTIES
    private lateinit var bindingHome:FragmentHomeBinding
    //private lateinit var photoAdapter:PhotoAdapter
    private var listPhotos:MutableList<Photo> = mutableListOf()
    //FIREBASE DATABASE
    //REALTIME DATABASE
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    //STORAGE DATABASE
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    //NAVIGATE CONTROLLER
    lateinit var navController:NavController
    //INT
    private var sizeOfListPhotos:Int = 0
    //SAVE TO SPECIFIC FOLDER
    private lateinit var saveToSpecificFolder: SaveToSpecificFolder
    //ON CREATE VIEW
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        bindingHome = FragmentHomeBinding.inflate(layoutInflater)
        return bindingHome.root
    }

    companion object{
        var checkChangeFragment = false
    }

    //ON VIEW CREATED
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindingHome.main = this

        photoAdapter = PhotoAdapter(requireContext(),this)
        bindingHome.recyclerViewPhotos.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        bindingHome.recyclerViewPhotos.setHasFixedSize(true)
        bindingHome.recyclerViewPhotos.adapter = photoAdapter
        navController = NavHostFragment.findNavController(this)
        //INITIALIZED SAVE TO SPECIFIC FOLDER
        saveToSpecificFolder = SaveToSpecificFolder(requireContext())
        //STORAGE FIREBASE
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference.child(getString(R.string.khan_image))
        //LOAD DATA FROM FIREBASE REALTIME DATABASE
        loadDataFromFirebase()
        Log.i("AAA","ON VIEW CREATED")
    }



    //LOAD DATA FROM FIREBASE REALTIME DATABASE
    private fun loadDataFromFirebase() {
        Thread(Runnable {

            database = FirebaseDatabase.getInstance()
            databaseReference = database.reference.child(getString(R.string.khan_image))

            databaseReference.addChildEventListener(object: ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val map:HashMap<String, String> = snapshot.value as HashMap<String, String>
                    val photo:Photo = Photo(map["id"]!!,map["title"], map["path"]!!, map["date"]!!,map["type"]!!)
                    //listPhotos.add(listPhotos.size,photo)
                    val bool = photoAdapter.checkItemExisted(photo)
                    Log.i("AAA","STATE ADD $bool")
                    photoAdapter.listPhotos.add(photoAdapter.listPhotos.size,photo)
                    photoAdapter.notifyItemInserted(photoAdapter.listPhotos.size - 1)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val map:HashMap<String, String> = snapshot.value as HashMap<String, String>
                    val photo:Photo = Photo(map["id"]!!,map["title"], map["path"]!!, map["date"]!!,map["type"]!!)
                    //photoAdapter.listPhotos.add(photoAdapter.listPhotos.size,photo)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val map:HashMap<String, String> = snapshot.value as HashMap<String, String>
                    val photo:Photo = Photo(map["id"]!!,map["title"], map["path"]!!, map["date"]!!,map["type"]!!)

                    for (item in photoAdapter.listPhotos.indices){
                        if(photoAdapter.listPhotos[item].title.equals(photo.title)){
                            photoAdapter.listPhotos.removeAt(item)
                            photoAdapter.listItemHolder.removeAt(item)
                            photoAdapter.notifyItemRemoved(item)
                            Log.i("AAA", "POSITION $item")
                            break
                        }
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }).start()
    }


    override fun photoClick(photo:Photo) {
        val bundle:Bundle = Bundle()
        bundle.putSerializable("photo",photo)
        MainActivity.currentFragment = Utils.FRAGMENT_PHOTO
        MainActivity.showToolbar(View.GONE)
        navController.navigate(R.id.photoFragment,bundle)
    }

    override fun photoLongClick(photo: Photo) {
        //OPEN BOTTOM SHEET DIALOG : ADD TO FAVORITE OR ADD TO NEW FOLDER
        MainActivity.showToolBarAddOrDelete(View.VISIBLE)
        MainActivity.showToolbar(View.GONE)
    }

}