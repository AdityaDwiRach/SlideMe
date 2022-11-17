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

        binding.csSlider.setValueChangeListener {
            Toast.makeText(this, "Current value : $it", Toast.LENGTH_SHORT).show()
        }
    }
}