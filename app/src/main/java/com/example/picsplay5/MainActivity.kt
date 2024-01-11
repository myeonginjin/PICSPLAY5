package com.example.picsplay5



import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.picsplay5.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : AppCompatActivity() {
    private var selectedImageUri: Uri? = null
    lateinit var binding: ActivityMainBinding
    lateinit var filePath: String

    private val cameraLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = Intent(this, SubActivity::class.java)
                intent.putExtra("imagePath", filePath)

                Log.i("?","\n\n\n\n\n\n >>>>>>>>> $filePath <<<<<<<<<<<<<<\n\n\n\n\n\n\n")

//                startActivity(intent)
            }
        }

    private val galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let {
                    selectedImageUri = it
                }
                startEditingActivity()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val cameraButton: Button = findViewById(R.id.cameraButton)
        val galleryButton: Button = findViewById(R.id.galleryButton)

        cameraButton.setOnClickListener {
            openCamera()
        }

        galleryButton.setOnClickListener {
            openGallery()
        }
    }

    private fun openCamera() {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        filePath = file.absolutePath
        val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "com.example.ch16_provider.fileprovider",
            file
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        cameraLauncher.launch(intent)
    }



    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun startEditingActivity() {

        val intent = Intent(this, SubActivity::class.java)
        intent.putExtra("imagePath", selectedImageUri?.toString())

        startActivity(intent)
    }
    private fun getImagePath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val imagePath = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return imagePath
    }
}