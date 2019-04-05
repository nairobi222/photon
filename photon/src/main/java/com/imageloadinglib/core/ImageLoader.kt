package com.imageloadinglib.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.imageloadinglib.cache.CacheRepository
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class ImageLoader private constructor(context: Context) {
    private val cache  = CacheRepository(context)
    private val executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    private val uiHandler = Handler(Looper.getMainLooper())


    fun displayImage(url: String, imageview: ImageView,placeholder:Int) {

        var bitmap = cache.get(url)
       bitmap?.let {
            imageview.setImageBitmap(it)
            return
        }
            ?: run {
                imageview.tag = url
                if (placeholder !=null)
                    imageview.setImageResource(placeholder)
                executorService.submit {
                    bitmap = downloadImage(url)
                    bitmap?.let {
                        if (imageview.tag == url){
                            updateImageView(imageview, it)
                        }

                        cache.put(url, it)
                    }

                }

            }


    }


    fun setImagePlaceHolder(imageview: ImageView, resId:Int)= imageview.setImageResource(resId)

    fun updateImageView(imageview: ImageView, bitmap: Bitmap) {
        uiHandler.post {
            imageview.setImageBitmap(bitmap)
        }
    }

    fun downloadImage(url: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val url = URL(url)
            val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            bitmap = BitmapFactory.decodeStream(conn.inputStream)
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bitmap

    }

    fun clearcache() {
        cache.clear()
    }

    companion object {
        private val INSTANCE :ImageLoader? = null
        @Synchronized
        fun getInstance(context: Context):ImageLoader{
            return INSTANCE?.let { return INSTANCE }
            ?:run {
                return ImageLoader(context)
            }
        }
    }
}