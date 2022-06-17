package com.example.khangallery.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.khangallery.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    lateinit var bindingSettings:FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bindingSettings = FragmentSettingsBinding.inflate(layoutInflater)
        return bindingSettings.root
    }

    companion object {

    }
}