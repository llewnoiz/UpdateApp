package com.kaon.updateapp

import android.app.Service
import android.content.Context
import android.system.Os.chmod
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception

class DownloadService(var context: Context) {

    private val downloadClient = OkHttpClient()
    var downloadFile:String = ""
        get() = field

    var authenticator: String = ""
        get() = field
        set(value) {
            field = value
        }
    var url:String = ""
        get() = field
        set(value) {
            field = value
        }
    private fun makeRequeset(): Request {
        downloadClient.newBuilder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        return Request.Builder()
                .url(url)
                .header("Authorization",authenticator)
                .build()
    }
    private fun makeFile(): File? {
        var parseURL = url.split("/")
        var filename = parseURL[parseURL.lastIndex]

        downloadFile = context?.filesDir.canonicalPath + "/$filename"
        var appFile: File? = File(downloadFile)
        appFile?.createNewFile()
        return appFile
    }

    private fun StreamToFile(stream:InputStream?): String {

        var output = FileOutputStream(makeFile())
        var data: ByteArray = ByteArray(2048)

        try {
            var count: Int? = stream?.read(data)
            while( count != -1)
            {
                output.write(data,0 , count!!)
                count = stream?.read(data)
            }
        }
        catch (e : Exception){
            e.printStackTrace()
            return "fail"
        }
        finally {
            output.flush()
            output.close()
            stream?.close()
        }
        return "success"
    }

    fun run(): String{

        try {
            downloadClient.newCall(makeRequeset()).execute().use {
            if (!it.isSuccessful) throw IOException("Unexpected code $it")


            return StreamToFile(it.body()?.byteStream())
        }

    }
        catch (e : Exception)
        {
                e.printStackTrace()
                return "fail"
        }
    }

}