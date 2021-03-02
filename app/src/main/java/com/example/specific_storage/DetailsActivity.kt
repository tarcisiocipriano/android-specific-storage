package com.example.specific_storage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.specific_storage.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileName = intent.getStringExtra("FILENAME")
        val fileContent = intent.getStringExtra("FILECONTENT")


        binding.apply {
            tvDetailsName.text = fileName
            tvDetailsContent.text = fileContent
        }
    }
}