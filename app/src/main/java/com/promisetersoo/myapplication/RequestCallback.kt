package com.promisetersoo.myapplication

import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.nio.ByteBuffer


class RequestCallback : UrlRequest.Callback() {
    override fun onRedirectReceived(request: UrlRequest?, info: UrlResponseInfo?, newLocationUrl: String?) {
        TODO("Not yet implemented")
    }

    override fun onResponseStarted(request: UrlRequest?, info: UrlResponseInfo?) {
        TODO("Not yet implemented")
    }

    override fun onReadCompleted(request: UrlRequest?, info: UrlResponseInfo?, byteBuffer: ByteBuffer?) {
        TODO("Not yet implemented")
    }

    override fun onSucceeded(request: UrlRequest?, info: UrlResponseInfo?) {
        TODO("Not yet implemented")
    }

    override fun onFailed(request: UrlRequest?, info: UrlResponseInfo?, error: CronetException?) {
        TODO("Not yet implemented")
    }

    override fun onCanceled(request: UrlRequest?, info: UrlResponseInfo?) {
        super.onCanceled(request, info)
    }
}