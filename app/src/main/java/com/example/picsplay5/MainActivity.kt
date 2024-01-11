package com.example.picsplay5



import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.renderscript.ScriptGroup.Input
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.picsplay5.R
import com.example.picsplay5.SubActivity
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private var selectedImageUri: Uri? = null


//    private val cameraLauncher: ActivityResultLauncher<Intent> =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val data: Intent? = result.data
//                val imageBitmap = data?.extras?.get("data") as Bitmap?
//
//                selectedImageUri = imageBitmap?.let { bitmap ->
//                    try {
//                        applicationContext.contentResolver.insert(
//                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                            ContentValues().apply {
//                                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//                                put(MediaStore.Images.Media.ORIENTATION, 0)
//                            }
//                        )?.also { uri ->
//                            applicationContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
//                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//                            }
//                        }
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                        null
//                    }
//                }
//
//                startEditingActivity()
//            }
//        }





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
        setContentView(R.layout.activity_main)

//        val requestGalleryLauncher = registerForActivityResult(
//            ActivityResultContracts.StartActivityForResult())
//
//        {
//
//            try {
//                val calRatio = calculateInSampleSize(
//                    it.data!!.data!!,
//                    resources.getDimensionPixelSize(R.dimen.imgSize),
//                    resources.getDimensionPixelSize(R.dimen.imgSize),
//                )
//                val option = BitmapFactory.Options()
//                option.inSampleSize = calRatio
//
//
//            }
//        }


        val cameraButton: Button = findViewById(R.id.cameraButton)
        val galleryButton: Button = findViewById(R.id.galleryButton)

        cameraButton.setOnClickListener {
            openCamera()
        }

        galleryButton.setOnClickListener {
            openGallery()
        }


    }
    private fun calculateInSampleSize(fileUri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            var inputStream = contentResolver.openInputStream(fileUri)

            //inJustDecodeBounds 값을 true 로 설정한 상태에서 decodeXXX() 를 호출.
            //로딩 하고자 하는 이미지의 각종 정보가 options 에 설정 된다.
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream!!.close()
            inputStream = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //비율 계산........................
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        //inSampleSize 비율 계산
        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun startEditingActivity() {

        val intent = Intent(this, SubActivity::class.java)
        intent.putExtra("imagePath", selectedImageUri?.toString())
        Log.i("?"," qwdqwdwqdqwdwqdwqdwqdwqdqwdwqdwqdwqdwdqwdwqdqwdwqdwqdwq>>>>>>>       $selectedImageUri          <<<<<qwdwqdwqdwqdqwdwqdwqdwqdwq")
        startActivity(intent)
    }
}