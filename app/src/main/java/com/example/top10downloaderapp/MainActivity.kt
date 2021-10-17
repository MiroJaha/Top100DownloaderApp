package com.example.top10downloaderapp

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.URL
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URLEncoder


class MainActivity : AppCompatActivity() {

    private lateinit var myRV: RecyclerView
    private lateinit var list1: ArrayList<Group>
    private lateinit var list2: ArrayList<Group>
    private lateinit var adapter: RVAdapter
    private lateinit var refreshImage: ImageView
    private lateinit var searchView: SearchView
    private lateinit var xMLParser: XMLParser
    private var menuCheck=10

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.change_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.top10Apps -> {
                menuCheck=10
                return true
            }
            R.id.top100Apps -> {
                menuCheck=100
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        list1= arrayListOf()
        list2= arrayListOf()
        myRV= findViewById(R.id.myRv2)
        refreshImage= findViewById(R.id.refreshImage)
        searchView= findViewById(R.id.searchV)
        xMLParser= XMLParser()

        adapter= RVAdapter(list1)
        myRV.adapter= adapter
        myRV.layoutManager= LinearLayoutManager(this@MainActivity)

        adapter.setOnItemClickListener(object : RVAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
               /* AlertDialog.Builder(this@MainActivity)
                    .setMessage(list1[position].summary)
                    .setCancelable(false)
                    .setPositiveButton("OK"){dialog,_ -> dialog.cancel()}
                    .show()*/
                val escapedQuery = URLEncoder.encode(list1[position].title, "UTF-8");
                val uri = Uri.parse("https://www.google.com/search?q=$escapedQuery");
                val intent = Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent)
            }
        })

        refreshImage.setOnClickListener{
            if(menuCheck == 10)
                requestAPI("10")
            else
                requestAPI("100")
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                list1.clear()
                for (i in list2){
                    if(i.title.contains(p0!!,true))
                        list1.add(i)
                }
                adapter.notifyDataSetChanged()
                return false
            }

        })

    }

    private fun requestAPI(number: String){
        // we use Coroutines to fetch the data, then update the Recycler View if the data is valid
        CoroutineScope(Dispatchers.IO).launch {
            // we fetch the prices
            val data = async { fetchPrices(number) }.await()
            // once the data comes back, we populate our Recycler View
            if(data.isNotEmpty()){
                populateRV(data)
            }else{
                Log.d("MAIN", "Unable to get data")
            }
        }
    }

    private fun fetchPrices(number: String): String{
      var response = ""
        try{
            response = URL("https://itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=$number/xml").readText(/*Charsets.UTF_8*/)
        }catch(e: Exception){
            Log.d("MAIN", "ISSUE: $e")
        }
        return response
    }

    private suspend fun populateRV(result: String){
        withContext(Dispatchers.Main){
            list1.clear()
            val targetStream= ByteArrayInputStream(result.toByteArray()) as InputStream
            list1.addAll(targetStream.let { xMLParser.parse(it) })
            list2.clear()
            list2.addAll(list1)
            adapter.notifyDataSetChanged()
        }
    }

}