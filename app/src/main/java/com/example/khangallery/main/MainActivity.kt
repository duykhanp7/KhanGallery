package com.example.khangallery.main

import android.annotation.SuppressLint
import  android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.khangallery.R
import com.example.khangallery.adapter.PhotoAdapter
import com.example.khangallery.database.SaveToSpecificFolder
import com.example.khangallery.databinding.ActivityMainBinding
import com.example.khangallery.model.Photo
import com.example.khangallery.utils.Utils
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import okhttp3.internal.Util
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    //ACTIVITY RESULT
    private lateinit var launcher:ActivityResultLauncher<Intent>
    //REALTIME DATABASE
    private lateinit var database:FirebaseDatabase
    private lateinit var databaseReference:DatabaseReference
    private lateinit var databaseFavorite:FirebaseDatabase
    private lateinit var databaseReferenceFavorite:DatabaseReference
    //STORAGE DATABASE
    private lateinit var storage:FirebaseStorage
    private lateinit var storageReference:StorageReference
    private lateinit var storageFavorite:FirebaseStorage
    private lateinit var storageReferenceFavorite:StorageReference
    //NAVIGATION
    private lateinit var navHost:NavHostFragment
    private lateinit var navController:NavController
    //ID PHOTOS
    private var indexPhotos:Int = 0
    //SAVE TO SPECIFIC FOLDER
    private lateinit var saveToSpecificFolder:SaveToSpecificFolder
    //BOOLEAN VARIABLE TO CHECK ITEM IS HOVERED


    companion object{
        //PHOTO ADAPTER
        @SuppressLint("StaticFieldLeak")
        lateinit var photoAdapter:PhotoAdapter
        @SuppressLint("StaticFieldLeak")
        var photoFavoriteAdapter:PhotoAdapter? = null
        //BINDING
        private lateinit var binding:ActivityMainBinding
        var hovered = false
        //POSITION FRAGMENT
        var currentFragment = Utils.FRAGMENT_HOME

        //TOOL BAR ADD NEW PHOTO
        fun showToolbar(visibility:Int) {
            binding.topBarLayout.post {
                binding.topBarLayout.visibility = visibility
            }
        }

        //TOOLBAR ADD PHOTO TO FAVORITES
        fun showToolBarAddOrDelete(visibility: Int) {
            binding.toolBarAddOrDelete.post {
                binding.topBarLayoutAddOrDelete.visibility = visibility
            }
            hovered = (visibility == View.VISIBLE)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //GET IMAGES FROM GALLERY RETURN (ONE OR MORE IMAGE)
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            var bool:Boolean = false
            if(it.data?.clipData != null){
                uploadImagesToFirebaseStorage(it.data!!)
            }
            else{
                Toast.makeText(applicationContext,"No pictures to upload !",Toast.LENGTH_LONG).show()
            }
        }

        //INITIALIZE FIREBASE REALTIME DATABASE
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("KhanImage")
        databaseFavorite = FirebaseDatabase.getInstance()
        databaseReferenceFavorite = databaseFavorite.reference.child(Utils.FAVORITE_FIREBASE)
        //STORAGE DATABASE
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference.child("KhanImage")
        storageFavorite = FirebaseStorage.getInstance()
        storageReferenceFavorite = storageFavorite.reference.child(Utils.FAVORITE_FIREBASE)
        //INITIALIZE SQLITE DATABASE
        saveToSpecificFolder = SaveToSpecificFolder(applicationContext)
        //BINDING MAIN
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.main = this

        //NOTE : BUT INITIALIZE NAVIGATION AFTER BINDING LAYOUT OR AFTER SET CONTENT VIEW
        //INITIALIZE NAVIGATION
        navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        //ONCLICK FOR ITEMS OF TOOLBAR
        binding.toolBar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.addImage -> {
                    //CHECK PERMISSION IS GRANTED
                    //IF GRANTED -> OPEN GALLERY
                    if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        openGallery();
                    }
                    //IF NOT GRANTED, THEN ASK PERMISSION
                    else{
                        askForPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
                    }
                    return@setOnMenuItemClickListener true
                }
                else->{return@setOnMenuItemClickListener false}
            }
        }

        //ON CLICK TOOLBAR ADD TO FAVORITES OR DELETE
        binding.toolBarAddOrDelete.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.addToFavorite->{
                    if(currentFragment == Utils.FRAGMENT_HOME){
                        val list = photoAdapter.getItemsChecked()
                        allPhotosToFavorites(list)
                        showToolbar(View.VISIBLE)
                        showToolBarAddOrDelete(View.GONE)
                        navController.navigate(R.id.favoritesFragment)
                        binding.bottomNavigation.menu[1].isChecked = true
                        Toast.makeText(applicationContext,"ADD TO FAVORITES",Toast.LENGTH_LONG).show()
                    }
                    else{
                        Toast.makeText(applicationContext,"BEEN ADDED TO FAVORITES",Toast.LENGTH_LONG).show()
                    }
                    return@setOnMenuItemClickListener true
                }
                R.id.deleteImage->{
                    if(currentFragment == Utils.FRAGMENT_HOME){
                        val list = photoAdapter.getItemsChecked()
                        deletePhotos(Utils.KHAN_FOLDER,Utils.KHAN_FIREBASE,list)
                        photoAdapter.showAllRadioButtonChecked(false)
                        Toast.makeText(applicationContext,"DELETE PHOTO SUCCESSFULLY",Toast.LENGTH_LONG).show()
                    }
                    else{
                        val list = photoFavoriteAdapter?.getItemsChecked()
                        if (list != null) {
                            deletePhotos(Utils.FAVORITE_FOLDER,Utils.FAVORITE_FIREBASE,list)
                        }
                        photoFavoriteAdapter?.showAllRadioButtonChecked(false)
                        Toast.makeText(applicationContext,"DELETE OUT OF FAVORITES SUCCESSFULLY",Toast.LENGTH_LONG).show()
                    }
                    showToolbar(View.VISIBLE)
                    showToolBarAddOrDelete(View.GONE)
                    return@setOnMenuItemClickListener true
                }
                R.id.checkAllItems->{
                    if(currentFragment == Utils.FRAGMENT_HOME){
                        val check = photoAdapter.allItemsIsChecked()
                        photoAdapter.changeStateCheckOfAllItemsPhoto(!check)
                        photoAdapter.showAllRadioButtonChecked(true)
                    }
                    else{
                        val check = photoFavoriteAdapter?.allItemsIsChecked()
                        photoFavoriteAdapter?.changeStateCheckOfAllItemsPhoto(!check!!)
                        photoFavoriteAdapter?.showAllRadioButtonChecked(true)
                    }
                    return@setOnMenuItemClickListener true
                }
                else->{ return@setOnMenuItemClickListener true }
            }
        }

        //ONCLICK FOR ITEMS OF BOTTOM NAVIGATION
        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                //HOME SELECTED
                R.id.home -> {
                    currentFragment = Utils.FRAGMENT_HOME
                    navController.navigate(R.id.homeFragment)
                    return@setOnItemSelectedListener true
                }
                //FAVORITES SELECTED
                R.id.favorites -> {
                    hovered = false
                    showToolbar(View.VISIBLE)
                    showToolBarAddOrDelete(View.GONE)
                    currentFragment = Utils.FRAGMENT_FAVORITE
                    navController.navigate(R.id.favoritesFragment)
                    return@setOnItemSelectedListener true
                }
                //SETTINGS SELECTED
                R.id.settings -> {
                    hovered = false
                    showToolbar(View.VISIBLE)
                    showToolBarAddOrDelete(View.GONE)
                    currentFragment = Utils.FRAGMENT_SETTING
                    navController.navigate(R.id.settingsFragment)
                    return@setOnItemSelectedListener true
                }
                //ELSE
                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }

        //GET SIZE OF ITEM
        getSizeOfItemsDatabase()
        //LOAD PHOTOS FROM FIREBASE TO LOCAL
        saveToSpecificFolder.downloadPhotoFromFirebase(Utils.KHAN_FIREBASE,Utils.KHAN_FOLDER)
        saveToSpecificFolder.downloadPhotoFromFirebase(Utils.FAVORITE_FIREBASE,Utils.FAVORITE_FOLDER)
    }

    private fun allPhotosToFavorites(listTemp:ArrayList<Photo>) {
        for (item in listTemp) {
            val file =
                File(saveToSpecificFolder.getImagePath(item.title.toString(), Utils.KHAN_FOLDER))
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(file))
            saveToSpecificFolder.saveToFolder(bitmap, item.title.toString(), Utils.FAVORITE_FOLDER)
        }
        addPhotoToFavoriteFirebase(listTemp)
    }


    private fun addPhotoToFavoriteFirebase(list:ArrayList<Photo>) {
        for (item in list){
            val reference = storageReferenceFavorite.child(item.title.toString())
            val file = File(saveToSpecificFolder.getImagePath(item.title.toString(),Utils.FAVORITE_FOLDER))
            reference.putFile(Uri.fromFile(file)).addOnSuccessListener {
                reference.downloadUrl.addOnSuccessListener {
                    val photo  = Photo(item.id,item.title,it.toString(),getTimeCurrentInString(),Utils.FIREBASE_DATABASE)
                    databaseReferenceFavorite.child(item.title.toString()).setValue(photo)
                }
            }
        }
        Toast.makeText(applicationContext, "Add to favorite successfully !", Toast.LENGTH_LONG).show()
    }

    //DELETE PHOTOS
    private fun deletePhotos(folderNameLocal:String, folderNameFirebase:String, listPhotoDeleted:ArrayList<Photo>) {
        val storageReferenceDelete:StorageReference
        val databaseReferenceDelete:DatabaseReference

        //INITIALIZED
        val storageDelete:FirebaseStorage = FirebaseStorage.getInstance()
        storageReferenceDelete = storageDelete.reference.child(folderNameFirebase)
        val databaseDelete:FirebaseDatabase = FirebaseDatabase.getInstance()
        databaseReferenceDelete = databaseDelete.reference.child(folderNameFirebase)
        for (item in listPhotoDeleted){
            //DELETE IN STORAGE DATABASE
            val referenceStorageDelete = storageReferenceDelete.child(item.title.toString()).delete()
            //DELETE IN DATABASE REALTIME
            val referenceDatabaseDelete = databaseReferenceDelete.child(item.title.toString()).removeValue()
            //DELETE IN LOCAL FOLDER
            val file = File(saveToSpecificFolder.getImagePath(item.title.toString(),folderNameLocal))
            saveToSpecificFolder.removeFile(file)
        }
        Toast.makeText(applicationContext,"DELETE PHOTOS SUCCESSFULLY !",Toast.LENGTH_LONG).show()

        if(currentFragment == Utils.FRAGMENT_HOME){
            photoAdapter.showAllRadioButtonChecked(false)
            photoAdapter.changeStateCheckOfAllItemsPhoto(false)
        }
        else{
            photoFavoriteAdapter?.showAllRadioButtonChecked(false)
            photoFavoriteAdapter?.changeStateCheckOfAllItemsPhoto(false)
        }
    }

    //OPEN GALLERY
    private fun openGallery() {
        val intent:Intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        launcher.launch(Intent.createChooser(intent,"Select Your Pictures"))
    }

    //ASK PERMISSIONS
    private fun askForPermissions(permission:Array<String>) {
        requestPermissions(permission,1000)
    }


    //RESULT OF ASK PERMISSIONS
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //IF REQUEST CODE RETURN EQUAL WITH REQUEST CODE SENT AND GRANT RESULT[0] EQUAL PackageManager.PERMISSION_GRANTED
        //THEN OPEN GALLERY
        if(requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            openGallery()
        }
    }

    //UPLOAD PHOTOS TO FIREBASE STORAGE
    @Synchronized
    fun uploadImagesToFirebaseStorage(it:Intent) {
        runOnUiThread {
            val size:Int = it.clipData!!.itemCount
            Log.i("AAA", "PHOTOS : $size")
            for (j in 1..size){

                val uri:Uri = it.clipData!!.getItemAt(j-1).uri
                val index = ++indexPhotos
                val photo: Photo = Photo(index.toString(),"Photo $index",uri.toString(),getTimeCurrentInString(),Utils.FIREBASE_DATABASE)
                //PUT IMAGE TO FIREBASE REALTIME DATABASE
                val reference = storageReference.child(photo.title!!)
                val bytes = getBytesArray(uri)
                Log.i("AAA", "BYTE ARRAY INSERT : $bytes")
                //PUT BYTE ARRAY OF PHOTO TO STORAGE AND REALTIME DATABASE
                reference.putBytes(bytes).addOnSuccessListener {
                        reference.downloadUrl.addOnSuccessListener {
                        photo.path = it.toString()
                        photo.type = Utils.FIREBASE_DATABASE
                        //SAVE PHOTO TO FIREBASE DATABASE
                        databaseReference.child(photo.title!!).setValue(photo)
                    }
                }
                //SAVE IMAGE TO SPECIFIC FOLDER
                Thread(Runnable {
                    try {
                        val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver,uri))
                        saveToSpecificFolder.saveToFolder(bitmap,photo.title.toString(),applicationContext.getString(R.string.khan_image_folder))
                    }
                    catch (exception:SQLiteException){
                        Log.i("AAA","ERRORRRRRRRRRRRR : "+exception.printStackTrace())
                    }
                }).start()
            }
            Toast.makeText(applicationContext,"Upload photos successfully !",Toast.LENGTH_LONG).show()


        }
    }


    //GET BYTE ARRAY OF PHOTO BY URI
    private fun getBytesArray(uri:Uri):ByteArray {
        val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun getSizeOfItemsDatabase() {
        storageReference.listAll().addOnSuccessListener {
            indexPhotos = it.items.size
        }
    }


    //GET TIME CURRENT IN STRING
    @SuppressLint("SimpleDateFormat")
    fun getTimeCurrentInString():String {
        val calendar:Calendar = Calendar.getInstance()
        val format = SimpleDateFormat("E dd/MM/yyyy hh:mm:ss")
        return format.format(calendar.time)
    }




    //ON BACK PRESSED
    override fun onBackPressed() {
        Log.i("AAA", "CURRENT FRAGMENT : $currentFragment")
        if(hovered){
            showToolbar(View.VISIBLE)
            showToolBarAddOrDelete(View.GONE)
            photoAdapter.showAllRadioButtonChecked(false)
            photoAdapter.changeStateCheckOfAllItemsPhoto(false)
            photoFavoriteAdapter?.showAllRadioButtonChecked(false)
            photoFavoriteAdapter?.changeStateCheckOfAllItemsPhoto(false)
        }
        else{
            if(currentFragment == Utils.FRAGMENT_HOME){
                finish()
            }
            else{
                showToolbar(View.VISIBLE)
                binding.bottomNavigation.menu.getItem(0).isChecked = true
                currentFragment = Utils.FRAGMENT_HOME
                navController.navigate(R.id.homeFragment)
            }
        }
        hovered = false
    }
}
