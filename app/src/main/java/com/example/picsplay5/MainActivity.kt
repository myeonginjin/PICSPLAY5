package com.example.picsplay5

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
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
        imageView.setImageResource(R.drawable.test)
        imageView.setBackgroundColor(resources.getColor(android.R.color.white))


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
            val options = BitmapFactory.Options()
            val bitmapImg: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.test, options)
            val width :Int = options.outWidth
            val height :Int = options.outHeight

            var pixels: IntArray = IntArray(width* height)
            bitmapImg.getPixels(pixels, 0, width, 0, 0, width, height)

            Log.i("?","$width   $height")

            for (i in 0 until width * height) {
                val pixel = pixels[i]

                // RGB 값을 추출
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)

                // RGB 값을 이용하여 흑백 값 계산
                val gray = (red + green + blue) / 3

                // 새로운 RGB 값 생성 (모든 색상 채널이 같은 값)
                pixels[i] = Color.rgb(gray, gray, gray)
            }

            // 새로운 비트맵 생성
            val grayscaleBitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

            // 그레이스케일 이미지를 ImageView에 표시
            imageView.setImageBitmap(grayscaleBitmap)
            imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

            Log.i("Grayscale", "그레이스케일 이미지 생성 완료")

//            bitmapImg.getPixels(pixels, 0, w, 0, 0, w, h)
            Log.i("?"," ////   ${pixels.size}   ////   what / ${options.outWidth} : ${options.outHeight}")
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
}
