package com.kaon.updateapp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream
import java.lang.Exception

class InstallerService(var context: Context?,var path:String) {

    val InstallContext = context?.applicationContext
    val packageManager = context?.packageManager
    var packageInstaller : PackageInstaller? = null

    private fun makeSessionID(): Int? {
        packageInstaller = packageManager?.packageInstaller
        var sessionId = packageInstaller?.createSession(android.content.pm.PackageInstaller.SessionParams(android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL))

        return sessionId
    }

    private fun openAppSession(sessionId: Int?) : PackageInstaller.Session?
    {
        val file = File(path)
        var inputStream = FileInputStream(file)
        var session = packageInstaller?.openSession(sessionId!!)
        var sizeByte = 0L
        var out = session?.openWrite("package_installer_session",0,sizeByte)

        println("path : " + path)
        val buffer = ByteArray(8192)
        while( true)
        {
            val length = inputStream.read(buffer)
            if(length<= 0)
            {
                break;
            }
            out?.write(buffer,0,length)
        }
        session?.fsync(out!!)
        inputStream.close()
        out?.close()

        return session
    }
    private fun callAndroidInstaller(session:PackageInstaller.Session? ,sessionId:Int?)
    {
        val intent = Intent(Intent.ACTION_PACKAGE_ADDED)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        session?.commit(PendingIntent.getBroadcast(context,sessionId!!,intent,PendingIntent.FLAG_UPDATE_CURRENT).intentSender)
        session?.close()
    }

    private fun exactPackageName():String {
        val pm = packageManager
        var name:String = ""
        try {
            val info = pm?.getPackageArchiveInfo(path,0)
            if(info != null)
            {
                name = info.packageName
            }
        }
        catch (e:Exception)
        {
            e.printStackTrace()
            println("Error message : " +  e.message)
        }
        return name
    }

    private fun checkInstallPackage(packageName:String):String {


        println("checkInstallPackage : " + packageName)
        if(!packageName.equals(""))
        {
            val pm = packageManager
            val packge_info = pm?.getInstalledPackages(android.content.pm.PackageManager.GET_META_DATA)

            val installed = packge_info?.find {
                it.packageName == packageName
            }
            return installed?.packageName?:  ""
        }

        return ""
    }

    fun runUpdate() : String
    {
        var packageName = exactPackageName()
        var sessionId = makeSessionID()
        var AppSession = openAppSession(sessionId)
        println("packageName : " + packageName)
        println("SessionID : " + sessionId)
        println("AppSession : " + AppSession)

        callAndroidInstaller(AppSession,sessionId)

        return checkInstallPackage(packageName)?: packageName
    }
}