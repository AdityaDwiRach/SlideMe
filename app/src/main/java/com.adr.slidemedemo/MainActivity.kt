package com.adr.slidemedemo

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.adr.slidemedemo.databinding.ActivityMainBinding

class MainActivity: Activity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.csSliderDefault.setValueChangeListener {
            binding.tvValueDefault.text = it.toString()
        }

        binding.csSliderCustomColor.setValueChangeListener {
            binding.tvValueCustomColor.text = it.toString()
        }
    }
}