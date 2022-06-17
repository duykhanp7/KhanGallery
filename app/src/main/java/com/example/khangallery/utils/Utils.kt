package com.example.khangallery.utils

class Utils {
    companion object{
        //FRAGMENT NAME
        const val FRAGMENT_HOME:String = "FRAGMENT_HOME"
        const val FRAGMENT_FAVORITE:String = "FRAGMENT_FAVORITE"
        const val FRAGMENT_SETTING:String = "FRAGMENT_SETTING"
        const val FRAGMENT_PHOTO:String = "FRAGMENT_PHOTO"
        //TYPE PHOTO SAVED
        const val FIREBASE_DATABASE:String = "FIREBASE_DATABASE"
        const val SQLITE_DATABASE:String = "SQLITE_DATABASE"
        //DATABASE
        const val DATABASE_NAME:String = "PHOTO_DATABASE"
        const val PHOTO_TABLE_NAME:String = "PHOTOS"
        const val COLUMN_ID:String = "ID"
        const val COLUMN_TITLE:String = "TITLE"
        const val COLUMN_PHOTO:String = "PHOTO"
        const val COLUMN_DATE:String = "DATE_ADD"
        const val COLUMN_TYPE:String = "TYPE"
        //FOLDER
        const val KHAN_FOLDER = "KhanImageFolder"
        const val FAVORITE_FOLDER = "FavoriteImageFolder"
        const val KHAN_FIREBASE = "KhanImage"
        const val FAVORITE_FIREBASE = "FavoriteImage"
    }
}