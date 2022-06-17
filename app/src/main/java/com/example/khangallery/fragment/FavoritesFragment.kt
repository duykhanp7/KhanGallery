package com.example.khangallery.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.khangallery.R
import com.example.khangallery.adapter.PhotoAdapter
import com.example.khangallery.databinding.FragmentFavoritesBinding
import com.example.khangallery.listener.PhotoClickListener
import com.example.khangallery.main.MainActivity
import com.example.khangallery.main.MainActivity.Companion.photoFavoriteAdapter
import com.example.khangallery.model.Photo
import com.example.khangallery.utils.Utils
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.HashMap


class FavoritesFragment : Fragment(),PhotoClickListener {

    private lateinit var bindingFavorites:FragmentFavoritesBinding
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var database:FirebaseDatabase
    private lateinit var databaseReference:DatabaseReference
    //private lateinit var photoAdapter:PhotoAdapter
    //private var listPhotos:MutableList<Photo> = mutableListOf()
    //NAVIGATE CONTROLLER
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bindingFavorites = FragmentFavoritesBinding.inflate(layoutInflater)
        return bindingFavorites.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoFavoriteAdapter = PhotoAdapter(requireContext(),this)
        bindingFavorites.recyclerViewPhotosFavorites.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        bindingFavorites.recyclerViewPhotosFavorites.setHasFixedSize(true)
        bindingFavorites.recyclerViewPhotosFavorites.adapter = photoFavoriteAdapter

        navController = NavHostFragment.findNavController(this)

        loadDataFromFirebase()
    }


    //LOAD FAVORITE DATA FROM FIREBASE REALTIME DATABASE
    private fun loadDataFromFirebase() {
        Thread(Runnable {

            database = FirebaseDatabase.getInstance()
            databaseReference = database.reference.child(Utils.FAVORITE_FIREBASE)

            databaseReference.addChildEventListener(object: ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val map: HashMap<String, String> = snapshot.value as HashMap<String, String>
                    val photo: Photo = Photo(map["id"]!!,map["title"], map["path"]!!, map["date"]!!,map["type"]!!)

                    val boolean = photoFavoriteAdapter?.checkItemExisted(photo)
                    Log.i("AAA","ADD FAVORIEST STATE $boolean")
                    photoFavoriteAdapter?.listPhotos?.add(photoFavoriteAdapter!!.listPhotos.size,photo)
                    photoFavoriteAdapter?.notifyItemInserted(photoFavoriteAdapter!!.listPhotos.size - 1)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val map: HashMap<String, String> = snapshot.value as HashMap<String, String>
                    val photo: Photo = Photo(map["id"]!!,map["title"], map["path"]!!, map["date"]!!,map["type"]!!)
                    //photoFavoriteAdapter!!.listPhotos.add(photoFavoriteAdapter!!.listPhotos.size,photo)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val map:HashMap<String, String> = snapshot.value as HashMap<String, String>
                    val photo:Photo = Photo(map["id"]!!,map["title"], map["path"]!!, map["date"]!!,map["type"]!!)
                    for (item in photoFavoriteAdapter?.listPhotos?.indices!!){
                        if(photoFavoriteAdapter!!.listPhotos[item].title.equals(photo.title)){
                            photoFavoriteAdapter?.listPhotos?.removeAt(item)
                            photoFavoriteAdapter?.listItemHolder?.removeAt(item)
                            photoFavoriteAdapter?.notifyItemRemoved(item)
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