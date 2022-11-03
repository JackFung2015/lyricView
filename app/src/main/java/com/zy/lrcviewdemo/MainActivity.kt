package com.zy.lrcviewdemo

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.zy.lrcviewdemo.lrc.tools.DefaultLrcBuilder
import com.zy.lrcviewdemo.lrc.widget.LrcView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.concurrent.timerTask

/**
 * @Author: fzy
 * @Date: 2022/11/3
 */
class MainActivity : AppCompatActivity() {
    lateinit var lrcView: LrcView
    private var mPlayer: MediaPlayer? = null
    private val words:MutableList<String> = mutableListOf()
    private val timeList:MutableList<Int> = mutableListOf()
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lrcView = findViewById(R.id.lrcView)

        initPlayer()
        //从assets目录下读取歌词文件内容
        val lrc = getFromAssets("sneb.lrc")
        //解析歌词构造器
        val builder = DefaultLrcBuilder()
        //解析歌词返回LrcRow集合
        val rows = builder.getLrcRows(lrc)
        //lrcView添加歌词和时间
        rows?.forEach {
            words.add(it.content)
            timeList.add(it.time.toInt())
        }
        try {
            mPlayer?.setDataSource(applicationContext, Uri.parse("android.resource://com.zy.lrcviewdemo/" + R.raw.sneblrc))
            mPlayer?.prepare()
        } catch (e: Exception) {
            Log.e("========","exception is:${e.message}")
        }
        lrcView.bindData(words)
        mPlayer?.start()
        //歌曲播放和歌词滚动同步
        scrollLrcView()
    }

    private fun scrollLrcView() {
        var index = 1
        Timer().schedule(timerTask {
            if(index<timeList.size){
                for(i in index until timeList.size)   {
                    if(mPlayer!!.currentPosition>=timeList[index]){
                        lrcView.updateLineNum(index)
                        index++
                    }
                }
            }
        },50,50)
    }


    /**
     * 初始化播放器
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initPlayer() {
        val attributes = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .build()

        mPlayer = MediaPlayer()
        mPlayer!!.setAudioAttributes(attributes)
        mPlayer!!.reset()
    }

    /**
     * 从assets目录下读取歌词文件内容
     * @param fileName
     * @return
     */
    private fun getFromAssets(fileName: String?): String? {
        try {
            val inputReader = InputStreamReader(resources.assets.open(fileName!!))
            val bufReader = BufferedReader(inputReader)
            var line = ""
            var result = ""
            while (bufReader.readLine()?.also { line = it } != null) {
                if (line.trim { it <= ' ' } == "") continue
                result += """
                $line
                
                """.trimIndent()
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }


    override fun onDestroy() {
        super.onDestroy()
        mPlayer?.stop()
        mPlayer?.release()
    }
}