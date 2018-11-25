package com.MMGS.radiogram.classes

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

interface ImageDownloaderListener {
    fun onDownload(bitmap: Bitmap, fromData:Boolean)
}