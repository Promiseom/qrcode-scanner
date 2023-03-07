package com.promisetersoo.myapplication

interface QRCodeFoundListener{
    fun onQRCodeFound(qrCode: String)
    fun qrCodeNotFound()
}