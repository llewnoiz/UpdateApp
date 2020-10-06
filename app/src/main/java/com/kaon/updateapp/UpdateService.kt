package com.kaon.updateapp

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.JsonReader
import com.kaonmedia.krmslibrary.IKrmsAidlInterface
import okhttp3.Credentials
import org.json.JSONObject

class UpdateService : Service() {

    val receiver = MyBroadcastReceiver()

    override fun onCreate() {
        super.onCreate()
        val filtter = IntentFilter()
        filtter.addAction(Intent.ACTION_PACKAGE_ADDED)
        filtter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        filtter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        filtter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        registerReceiver(receiver,filtter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
    override fun onBind(intent: Intent): IBinder? {
        return mBiner
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    val mBiner = object : IKrmsAidlInterface.Stub(){

        override fun command(packageName: String?, commnads: String?): String {
//            var user = "krmsagent"
//            var password = "aleldjrkdhs2017@"
//            var url = "https://krms-demo.kaonmedia.com:9996/webdav/download/1596641096365innotv-release-v228.apk"

            println("commnad " + commnads)
            if(commnads == null)
            {
                return "fail"
            }
            var param = JSONObject(commnads)
            var user = param.getString("Username")?: ""
            var password = param.getString("Password") ?: ""
            var url = param.getString("URL") ?: ""
            var packageName:String = ""

            println("------------------ UpdateService ------------------")
            println("packageName : " + packageName)
            println("Command : " + commnads)
            println("user : " + user)
            println("password : " + password)
            println("url : " + url)

            var thread = Thread(object :Runnable{
                override fun run() {
                    val download = DownloadService(applicationContext)
                    var credentials = Credentials.basic(user,password)
                    download.url = url
                    download.authenticator = credentials
                    download.run()
                    println("--------------- complete download -----------------------")
                    var installer = InstallerService(applicationContext,download.downloadFile)

                    installer.runUpdate()
                }

            })

            thread.start()
            thread.join()

            println("complete url : " + url)

            return "success"
        }

    }
}

class MyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

    }
}
