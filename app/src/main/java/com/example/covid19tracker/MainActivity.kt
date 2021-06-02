package com.example.covid19tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit



class MainActivity : AppCompatActivity() {
lateinit var stateAdapter: StateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
list.addHeaderView(LayoutInflater.from(this).inflate(R.layout.list_header,list,false))
        fetchResults()

    }

    fun fetchResults() {
        GlobalScope.launch {

            val response = withContext(Dispatchers.IO) { Client.api.clone().execute() }
            if (response.isSuccessful) {
                swipeToRefresh.isRefreshing = false
                val data= Gson().fromJson(response.body?.string(),com.example.covid19tracker.Response::class.java)
                launch(Dispatchers.Main) {
                    bindCombinedData(data.statewise[0])
                   bindStatewiseData(data.statewise.subList(0,data.statewise.size))
                }
            }
        }
    }

    private fun bindStatewiseData(subList: List<StatewiseItem>) {
stateAdapter= StateAdapter(subList)
        list.adapter=stateAdapter
    }

    fun bindCombinedData(data: StatewiseItem) {
        val lastUpadatedTime=data.lastupdatedtime
        val simpleDateFormat=SimpleDateFormat("dd/MM/yy HH:mm:ss")
        lastUpdatedTv.text="Last Updated \n ${getTimeAgo(simpleDateFormat.parse(lastUpadatedTime))}"
        confirmedTv.text=data.confirmed
        recoveredTv.text=data.recovered
        activeTv.text=data.active
        deceasedTv.text=data.deaths
    }

    fun getTimeAgo(past: Date): String {
        val now = Date()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
        val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)

        return when {
            seconds < 60 -> {
                "Few seconds ago"
            }
            minutes < 60 -> {
                "$minutes minutes ago"
            }
            hours < 24 -> {
                "$hours hour ${minutes % 60} min ago"
            }
            else -> {
                SimpleDateFormat("dd/MM/yy, hh:mm a").format(past).toString()
            }
        }
    }
}