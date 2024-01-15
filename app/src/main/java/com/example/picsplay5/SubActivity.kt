package com.example.picsplay5


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import java.io.IOException


class SubActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val relativeLayout = RelativeLayout(this)
        relativeLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )


        val imageView = ImageView(this)
        val imageParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        imageView.layoutParams = imageParams
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imageView.setBackgroundColor(resources.getColor(android.R.color.white))


        val imagePath = intent.getStringExtra("imagePath")
        val byteArray = intent.getByteArrayExtra("사진")

        if (imagePath != null) {
            // 갤러리에서 넘어온 경우
            val imageUri = Uri.parse(imagePath)
            val bitmap = getImageBitmap(imageUri)
            // bitmap이 null이 아닌 경우에만 이미지뷰에 설정

            Log.i("test44 ","w : ${bitmap?.width}     h : ${bitmap?.height}")
            bitmap?.let {
                imageView.setImageBitmap(it)
            } ?: run {
                Toast.makeText(this, "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } else if (byteArray != null) {
            // 카메라에서 넘어온 경우

            Log.i("YourTag", "\n\n\n\n\n\n\n\n\n\n\nExceptionwdqwdqwdqwdwqdwqdwqddd/n/n/n/n/\n\n\n\n\n\n\n\n\n\n\n\n")

            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            // bitmap이 null이 아닌 경우에만 이미지뷰에 설정
            bitmap?.let {
                imageView.setImageBitmap(it)
            } ?: run {
                Toast.makeText(this, "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "이미지 데이터가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
        }


        if (!imagePath.isNullOrEmpty()) {
            val imageUri = Uri.parse(imagePath)
            val bitmap: Bitmap? = getImageBitmap(imageUri)

            // bitmap이 null이 아닌 경우에만 이미지뷰에 설정
            bitmap?.let {
                imageView.setImageBitmap(it)
            } ?: run {
                Toast.makeText(this, "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "이미지 경로가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
        }


        val button1 = Button(this)
        button1.text = "흑백 전환하기"
        val button1Params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        button1Params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        button1Params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        button1Params.bottomMargin = 400
        button1Params.leftMargin = 150
        relativeLayout.addView(button1, button1Params)

        // 그레이 스케일
        button1.setOnClickListener {
//            val bitmapImg: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.test)
//            val options = BitmapFactory.Options()
//            val bitmap2: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.test, options)
////            Log.i("test11","${bitmapImg.width}  \n    ${bitmapImg.height}  !")
//
////
//
//            val width :Int = options.outWidth
//            val height :Int =options.outHeight

            val drawable = imageView.drawable
            val bitmapImg: Bitmap = (drawable as BitmapDrawable).bitmap
            val width :Int = bitmapImg.width
            val height :Int =bitmapImg.height

            var pixels: IntArray = IntArray(width* height)
            bitmapImg.getPixels(pixels, 0, width, 0, 0, width, height)

            Log.i("?","$width   $height")

            val rRatio = 0.0;
            val gRatio = 0.5;
            val bRatio : Double = 1 - (rRatio + gRatio);




            for (i in 0 until width * height) {
                val pixel = pixels[i]

                // RGB 값을 추출
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)



                // RGB 값을 이용하여 흑백 값 계산
                val gray1  = (red * rRatio  + green  * gRatio + blue * bRatio)
                val gray : Int =  gray1.toInt()

                // 새로운 RGB 값 생성 (모든 색상 채널이 같은 값)
                pixels[i] = Color.rgb(gray, gray, gray)
            }

            // 새로운 비트맵 생성
            val grayscaleBitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

            // 그레이스케일 이미지를 ImageView에 표시

            Log.i("Grayscale", "${grayscaleBitmap.height} !!!!!!!!!!!!!\n ${grayscaleBitmap.width}  그레이스케일 이미지 생성 완료")

            imageView.setImageBitmap(grayscaleBitmap)
            imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

            Log.i("test","\n  !!!!! >>>>> ${imageView.width} //// ${imageView.height}         \n")

//            Log.i("Grayscale", "그레이스케일 이미지 생성 완료")

//            bitmapImg.getPixels(pixels, 0, w, 0, 0, w, h)
//            Log.i("?"," ////   ${pixels.size}   ////   what / ${options.outWidth} : ${options.outHeight}")
        }

        val button2 = Button(this)
        button2.text = "Button 2"
        val button2Params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        button2Params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        button2Params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        button2Params.bottomMargin = 400
        button2Params.rightMargin = 150
        relativeLayout.addView(button2, button2Params)

        button2.setOnClickListener {
            Log.d("test","?!")
        }


        relativeLayout.addView(imageView)
        setContentView(relativeLayout)


    }

    private fun getImageBitmap(uri: Uri): Bitmap? {
        return try {
            // getContentResolver().openInputStream(uri)를 사용하여 Uri에서 InputStream을 열어서 Bitmap으로 변환
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

}