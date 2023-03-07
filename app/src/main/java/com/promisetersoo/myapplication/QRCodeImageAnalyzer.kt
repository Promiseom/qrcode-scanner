package com.promisetersoo.myapplication

import androidx.annotation.NonNull
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

import com.google.zxing.BinaryBitmap
import com.google.zxing.ChecksumException
import com.google.zxing.FormatException
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader

import java.nio.ByteBuffer

import android.graphics.ImageFormat.YUV_420_888
import android.graphics.ImageFormat.YUV_422_888
import android.graphics.ImageFormat.YUV_444_888
import android.util.Log

class QRCodeImageAnalyzer(listener: QRCodeFoundListener):ImageAnalysis.Analyzer{
    private var _listener: QRCodeFoundListener = listener

    companion object{
        val TAG = "com.promise.myapplication.QRCodeImageAnalyzer"
    }

    override fun analyze(image: ImageProxy) {
        val imFormat = image.format
        if(imFormat == YUV_420_888 || imFormat == YUV_422_888 || imFormat == YUV_444_888){
            val byteBuffer = image.planes[0].buffer
            val imageData = ByteArray(byteBuffer.capacity())
            byteBuffer.get(imageData)

            val source: PlanarYUVLuminanceSource = PlanarYUVLuminanceSource(
                imageData,
                image.width, image.height,
                0, 0,
                image.width, image.height,
                false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            try{
                val result = QRCodeMultiReader().decode(binaryBitmap)
                _listener.onQRCodeFound(result.text)
            }catch(exc: Exception){
                _listener.qrCodeNotFound()
                Log.e(TAG,"QRCode not found")
            }
        }
        image.close()
    }
}