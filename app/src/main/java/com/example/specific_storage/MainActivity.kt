package com.example.specific_storage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Adapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.example.specific_storage.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val adapter by lazy {
        FileAdapter({ file ->
            file.delete()
            updateList()
        }, { file ->
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("FILENAME", file.name)
            intent.putExtra("FILECONTENT", readSafeFile(file))
            startActivity(intent)
        })
    }


    private fun openDetailsActivity(file: File) {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("FILENAME", file.name)
        intent.putExtra("FILECONTENT", readSafeFile(file))
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        updateList()
    }

    private fun initUI() {
        binding.apply {
            btnCreate.setOnClickListener {
                createFile()
            }

            rgDirectories.setOnCheckedChangeListener { _, _ ->
                updateList()
            }

            lvFiles.adapter = adapter
            lvFiles.layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun createFile() {
        val file = File(getDir(), binding.edtName.text.toString())
        if (binding.cbSafe.isChecked) {
            createSafeFile(file, binding.edtContent.text.toString())
        } else {
            createNormalFile(file, binding.edtContent.text.toString())
        }
        updateList()
    }

    private fun createNormalFile(file: File, fileContent: String) {
        file.writeText(fileContent)
    }

    private fun createSafeFile(file: File, fileContent: String) {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        if (file.exists()) {
            file.delete()
        }

        val encryptedFile = EncryptedFile.Builder(
            file,
            applicationContext,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        encryptedFile.openFileOutput().use { writer ->
            writer.write(fileContent.toByteArray())
        }

        // Read
        Log.d("File", readSafeFile(file))
    }

    private fun readSafeFile(file: File): String {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        val encryptedFile = EncryptedFile.Builder(
            file,
            this,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        var result = ""

        encryptedFile.openFileInput().use { inputStream ->
            result = inputStream.readBytes().decodeToString()
        }

        return result
    }

    private fun updateList() {
        adapter.submitList(getDir()?.listFiles()?.toList())
    }

    private fun getDir() = when (binding.rgDirectories.checkedRadioButtonId) {
        binding.rBtnInternal.id -> filesDir
        else -> getExternalFilesDir(null)
    }
}