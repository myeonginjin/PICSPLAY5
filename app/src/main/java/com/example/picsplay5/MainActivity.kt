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
import android.os.Build
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
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : AppCompatActivity() {


    lateinit var binding: ActivityMainBinding
    lateinit var filePath: String


    private var currentImage: Bitmap? = null // 현재 이미지를 저장하는 변수
    private var isGrayScaleEnabled = false
    private var grayScaleImg: Bitmap? = null


    // 카메라 앱 사진 촬영 후 다시 앱으로 돌아왔을 때 실행되는 매서드
    private val requestCameraFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        try {

            //경로 통해서 비트맵 이미지 객체 불러오기
            var bitmap = BitmapFactory.decodeFile(filePath)


            //이미지 회전 문제 해결
            val exif = ExifInterface(filePath)
            val exifOrientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
            val exifDegree = exifOrientationToDegrees(exifOrientation)
            bitmap = rotate(bitmap, exifDegree)

            //뷰어에 띄우기 및 데이터 보존
            bitmap.let {
                currentImage = bitmap
                binding.userImageView.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun exifOrientationToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }

    private fun rotate(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    // 갤러리에서 사진 선택 후  다시 앱으로 돌아왔을 때 실행되는 매서드
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data // 선택한 이미지의 주소
                // 이미지 파일 읽어와서 설정하기
                if (uri != null) {
                    // 사진 가져오기
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                    // 사진의 회전 정보 가져오기
                    val orientation = getOrientationOfImage(uri).toFloat()
                    // 이미지 회전하기
                    val newBitmap = getRotatedBitmap(bitmap, orientation)
                    // 회전된 이미지로 imaView 설정
                    currentImage = newBitmap
                    binding.userImageView.setImageBitmap(newBitmap)

                } else binding.userImageView.setImageResource(R.drawable.ic_launcher_background)
            }
        }

    private fun getOrientationOfImage(uri: Uri): Int {
        // uri -> inputStream
        val inputStream = contentResolver.openInputStream(uri)
        val exif: ExifInterface? = try {
            ExifInterface(inputStream!!)
        } catch (e: IOException) {
            e.printStackTrace()
            return -1
        }
        inputStream.close()

        // 회전된 각도 알아내기
        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        if (orientation != -1) {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> return 90
                ExifInterface.ORIENTATION_ROTATE_180 -> return 180
                ExifInterface.ORIENTATION_ROTATE_270 -> return 270
            }
        }
        return 0
    }

    @Throws(Exception::class)
    private fun getRotatedBitmap(bitmap: Bitmap?, degrees: Float): Bitmap? {
        if (bitmap == null) return null
        if (degrees == 0F) return bitmap
        val m = Matrix()
        m.setRotate(degrees, bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
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
        val downLoadButton : Button = findViewById(R.id.downLoadBtn)



        binding.cameraButton.setOnClickListener {

            requestCameraPermission()

        }



        galleryButton.setOnClickListener {

            openGallery()

        }

        // 그레이 스케일
        applyGrayScaleBtn.setOnClickListener {


            currentImage?.let {
                toggleGrayScaleFilter(userImageView)
            } ?: run {
                Toast.makeText(this, "이미지를 불러와주세요.", Toast.LENGTH_SHORT).show()
            }

        }

        downLoadButton.setOnClickListener {



            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //Q 버전 이상일 경우. (안드로이드 10, API 29 이상일 경우)
                grayScaleImg?.let {
                    saveImageOnAboveAndroidQ(grayScaleImg!!)
                    Toast.makeText(baseContext, "이미지 저장이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                } ?:run {  Toast.makeText(this, "흑백처리를 완료해주세요.", Toast.LENGTH_SHORT).show()}

            } else {
                // Q 버전 이하일 경우. 저장소 권한을 얻어온다.
                val writePermission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

                if(writePermission == PackageManager.PERMISSION_GRANTED) {
                    grayScaleImg?.let { it1 -> saveImageOnUnderAndroidQ(it1) }?:run {  Toast.makeText(this, "흑백처리를 완료해주세요.", Toast.LENGTH_SHORT).show()}
                    Toast.makeText(baseContext, "이미지 저장이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    val requestExternalStorageCode = 1

                    val permissionStorage = arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )

                    ActivityCompat.requestPermissions(this, permissionStorage, requestExternalStorageCode)
                }
            }

        }
    }
    // saveImageOnAboveAndroidQ 함수 수정
    private fun saveImageOnAboveAndroidQ(bitmap: Bitmap) {
        val fileName = System.currentTimeMillis().toString() + ".png" // 파일이름 현재시간.png

        val contentValues = ContentValues()
        contentValues.apply {
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/ImageSave")
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        Log.i("size11","w : ${bitmap.width}   h : ${bitmap.height}")


        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        try {
            if (uri != null) {
                val image = contentResolver.openFileDescriptor(uri, "w", null)

                if (image != null) {
                    val fos = FileOutputStream(image.fileDescriptor)

                    // PNG 형식으로 압축 (압축률 조절 가능, 100은 최대 압축)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos)

                    fos.close()

                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // saveImageOnUnderAndroidQ 함수 수정
    private fun saveImageOnUnderAndroidQ(bitmap: Bitmap) {
        val fileName = System.currentTimeMillis().toString() + ".png"
        val externalStorage = Environment.getExternalStorageDirectory().absolutePath
        val path = "$externalStorage/DCIM/imageSave"
        val dir = File(path)

        Log.i("size22","w : ${bitmap.width}   h : ${bitmap.height}")

        if (dir.exists().not()) {
            dir.mkdirs()
        }

        try {
            val fileItem = File("$dir/$fileName")
            fileItem.createNewFile()

            val fos = FileOutputStream(fileItem)

            // PNG 형식으로 압축 (압축률 조절 가능, 100은 최대 압축)
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos)

            fos.close()

            // 미디어 스캔을 통해 갤러리에 반영
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(fileItem)))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
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


        Log.i("size333","w: $width    h : $height")
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
        grayScaleImg = grayscaleBitmap
        binding.userImageView.setImageBitmap(grayscaleBitmap)


    }


    private fun openCamera() {


        //파일 만들기
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )

        //나중에 비트맵 이미지를 얻어내기 위해 파일경로 저장
        filePath = file.absolutePath

        //프로바이더 이용해 uri객체 만든 뒤 , 카매라 앱을 인텐트의 엑스트라 데이터로 설정
        val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "com.example.picsplay5.fileprovider",
            file
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)



        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        requestCameraFileLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
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