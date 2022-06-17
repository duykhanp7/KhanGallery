package com.example.khangallery.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.khangallery.R
import com.example.khangallery.databinding.PhotoItemLayoutBinding
import com.example.khangallery.listener.PhotoClickListener
import com.example.khangallery.main.MainActivity
import com.example.khangallery.model.Photo

class PhotoAdapter(contextTemp:Context,photoClickListenerTemp: PhotoClickListener) : RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    var listPhotos:MutableList<Photo> = mutableListOf()
    var context:Context = contextTemp
    private var photoClickListener:PhotoClickListener = photoClickListenerTemp
    var listItemHolder:ArrayList<ViewHolder> = arrayListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding:PhotoItemLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.photo_item_layout,parent,false)
        val holder = ViewHolder(context,binding,photoClickListener)
        listItemHolder.add(holder)
        return holder
    }




    @SuppressLint("ClickableViewAccessibility")
    @Synchronized
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //BIND VALUE
        if(holder.photo == null){
            Log.i("AAA","PHOTO NULL")
            val photo:Photo = listPhotos[position]
            holder.photo = photo
            holder.binding.setPhoto(photo)

            //SET EVENT CLICK, LONG PRESS AND DOUBLE TAP ON PHOTO
            holder.binding.photo.setOnClickListener {
                if(MainActivity.hovered){
                    Log.i("AAA","CLICK 1")
                    val checked = holder.photo?.check
                    holder.binding.radioButton.isChecked = !checked!!
                    holder.photo!!.check = !checked
                }
                else{
                    holder.photoClickListener.photoClick(holder.photo!!)
                }
            }
            //LONG PRESS
            holder.binding.photo.setOnLongClickListener {
                Log.i("AAA","CLICK 2")
                holder.photoClickListener.photoLongClick(holder.photo!!)
                holder.binding.radioButton.isChecked = true
                holder.photo!!.check = true
                showAllRadioButtonChecked(true)
                return@setOnLongClickListener true
            }
            //CHECK ON RADIO BUTTON
            holder.binding.radioButton.setOnClickListener {
                Log.i("AAA","CLICK 3")
                holder.binding.radioButton.post {
                    val checked = holder.photo!!.check
                    holder.binding.radioButton.isChecked = !checked
                    holder.photo!!.check = !checked
                }
            }
            //CHECK STATE RADIO BUTTON IS CHECKED OR NONE
            if(photo.check){
                holder.binding.radioButton.isChecked = true
                holder.showRadioButton(View.VISIBLE)
            }else{
                holder.binding.radioButton.isChecked = false
                holder.showRadioButton(View.GONE)
            }
            if(MainActivity.hovered){
                holder.binding.radioButton.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return listPhotos.size
    }


    fun changeStateCheckOfAllItemsPhoto(boolean: Boolean) {
        for (item in listItemHolder){
            item.photo!!.check = boolean
            item.binding.radioButton.isChecked = boolean
        }
    }

    fun showAllRadioButtonChecked(boolean: Boolean) {
        for (item in listItemHolder){
            //showOrHideByChecked(item.binding.radioButton,boolean)
            if(boolean){
                item.binding.radioButton.visibility = View.VISIBLE
            }
            else{
                item.binding.radioButton.visibility = View.GONE
            }
        }

    }

    fun allItemsIsChecked():Boolean {
        for (item in listItemHolder){
            if(!item.photo!!.check){
                return false
            }
        }
        return true
    }


    fun getItemsChecked():ArrayList<Photo> {
        val list:ArrayList<Photo> = arrayListOf()
        for (item in listItemHolder){
            if(item.photo!!.check){
                list.add(item.photo!!)
            }
        }
        return list
    }

    fun checkItemExisted(photo:Photo):Boolean {
        for (item in listPhotos){
            if(item.title == photo.title){
                return true
            }
        }
        return false
    }


    class ViewHolder(contextTemp: Context,bindingTemp: PhotoItemLayoutBinding,photoClickListenerTemp: PhotoClickListener) : RecyclerView.ViewHolder(bindingTemp.root) {
        var photo:Photo? = null
        var binding:PhotoItemLayoutBinding = bindingTemp
        var photoClickListener:PhotoClickListener = photoClickListenerTemp
        var context:Context = contextTemp


        fun showRadioButton(visibility:Int) {
            binding.radioButton.post {
                binding.radioButton.visibility = visibility
            }
        }

    }


}