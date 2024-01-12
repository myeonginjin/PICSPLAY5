package com.example.picsplay5



import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.picsplay5.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {
    private var selectedImageUri: Uri? = null

    lateinit var binding: ActivityMainBinding
    lateinit var filePath: String


    private var currentImage: Bitmap? = null // 현재 이미지를 저장하는 변수
    private var isGrayScaleEnabled = false


    private val  requestCameraFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        val calRatio = calculateInSampleSize(
            Uri.fromFile(File(filePath)),
            resources.getDimensionPixelSize(R.dimen.imgSize),
            resources.getDimensionPixelSize(R.dimen.imgSize)
        )
        val option = BitmapFactory.Options()
        option.inSampleSize = calRatio

        var bitmap = BitmapFactory.decodeFile(filePath, option)

        val exif = ExifInterface(filePath)
        val exifOrientation: Int = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )

        val exifDegree = exifOrientationToDegrees(exifOrientation)
        bitmap = rotate(bitmap, exifDegree)


        bitmap?.let {
            currentImage = bitmap
            binding.userImageView.setImageBitmap(bitmap)
        }
    }
    fun exifOrientationToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }

    private fun rotate(bitmap: Bitmap, degree: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }



    private val galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { selectedImageUri ->
                    filePath = getRealPathFromURI(selectedImageUri)

                    // 이미지 선택 시 메인 뷰어에 이미지 설정
                    val calRatio = calculateInSampleSize(
                        selectedImageUri,
                        resources.getDimensionPixelSize(R.dimen.imgSize),
                        resources.getDimensionPixelSize(R.dimen.imgSize)
                    )
                    val option = BitmapFactory.Options()
                    option.inSampleSize = calRatio
                    var bitmap: Bitmap? = BitmapFactory.decodeFile(filePath, option)

                    val exif = ExifInterface(filePath)
                    val exifOrientation: Int = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                    )

                    val exifDegree = exifOrientationToDegrees(exifOrientation)
                    bitmap = rotate(bitmap!!, exifDegree)

                    bitmap?.let {
                        currentImage = it
                        binding.userImageView.setImageBitmap(it)
                    }
                }
            }
        }
    private fun getRealPathFromURI(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            it.moveToFirst()
            val idx = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            val path = it.getString(idx)
            it.close()
            return path
        }
        return uri.path ?: ""
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setContentView(R.layout.activity_main)

//        val cameraButton: Button = findViewById(R.id.cameraButton)
        val galleryButton: Button = findViewById(R.id.galleryButton)
        val applyGrayScaleBtn: Button = findViewById(R.id.applyGrayScale)
        val userImageView : ImageView = findViewById(R.id.userImageView)



        binding.cameraButton.setOnClickListener {
            //camera app......................
            //파일 준비...............

            Log.i(">",">>>>>>>>>>>>>>>>>>>>>>>L>L>L>L>L>L>L>L>L>L>L>L")

            requestCameraPermission()

        }



        galleryButton.setOnClickListener {
            Log.i(">","654646844644654984454")
            openGallery()
            //requestCameraPermission()
        }

        // 그레이 스케일
        applyGrayScaleBtn.setOnClickListener {


            currentImage?.let {
                toggleGrayScaleFilter(userImageView)
            } ?: run {
                Toast.makeText(this, "이미지를 불러와주세요.", Toast.LENGTH_SHORT).show()
            }


        }



    }

    private fun toggleGrayScaleFilter(imageView: ImageView) {
        // 흑백 필터 토글
        isGrayScaleEnabled = !isGrayScaleEnabled

        // 현재 이미지 상태에 따라 흑백 필터를 적용하거나 해제
        if (isGrayScaleEnabled) {
            applyGrayScaleFilter(imageView)
        } else {
            // 원본 이미지로 복원
            binding.userImageView.setImageBitmap(currentImage)
        }
    }
    private fun applyGrayScaleFilter(imageView: ImageView) {
        val originalBitmap = (binding.userImageView.drawable as BitmapDrawable).bitmap

        // Get the width and height of the bitmap
        val width: Int = originalBitmap.width
        val height: Int = originalBitmap.height

        // Get the pixels of the bitmap
        val pixels: IntArray = IntArray(width * height)
        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Constants for grayscale conversion
        val rRatio = 0.299
        val gRatio = 0.587
        val bRatio = 0.114

        // Apply grayscale filter
        for (i in 0 until width * height) {
            val pixel = pixels[i]

            // Extract RGB values
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)

            // Calculate grayscale value
            val gray1 = (red * rRatio + green * gRatio + blue * bRatio).toInt()

            // Create new RGB value (all color channels have the same value)
            pixels[i] = Color.rgb(gray1, gray1, gray1)
        }

        // Create a new bitmap with the modified pixels
        val grayscaleBitmap =
            Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

        // Set the grayscale image to the ImageView
        binding.userImageView.setImageBitmap(grayscaleBitmap)


    }
    private fun calculateInSampleSize(fileUri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            var inputStream = contentResolver.openInputStream(fileUri)

            //inJustDecodeBounds 값을 true 로 설정한 상태에서 decodeXXX() 를 호출.
            //로딩 하고자 하는 이미지의 각종 정보가 optionsㅊ에 설정 된다.
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
        Log.i(">",">>>>>>>>>>>>>>>>>>>>>>>L>L>L>L>L>L>L>L>L>L>L>L openCamera()")
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
        filePath = file.absolutePath
        val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "com.example.picsplay5.fileprovider",
            file
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        Log.i(">",">>>>>>>>>>>>>>>>>>>>>ddddddddcccddcccccccx>L $photoURI <<<<<<<>< openCamera()")



        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        requestCameraFileLauncher.launch(intent)
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

    // MainActivity에서 런타임 권한을 요청하는 부분 추가
    private val CAMERA_PERMISSION_REQUEST = 100

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            openCamera()
        }
    }


    // 런타임 권한 요청 결과를 처리하는 부분
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                // 권한이 거부되었을 때의 처리
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}