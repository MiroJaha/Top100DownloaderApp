package com.example.top10downloaderapp

import android.text.Html
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.lang.IllegalStateException

class XMLParser{

    private val ns: String? = null

    fun parse(inputStream: InputStream): ArrayList<Group>{
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return readRssFeed(parser)
        }
    }

    private fun readRssFeed(parser: XmlPullParser): ArrayList<Group> {
        val list= arrayListOf<Group>()

        parser.require(XmlPullParser.START_TAG, ns,"feed")

        while (parser.next() != XmlPullParser.END_TAG){
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }
            if(parser.name == "entry") {
                parser.require(XmlPullParser.START_TAG,ns,"entry")
                var title: String?= null
                var id: String?= null
                var summary: String?= null
                while (parser.next() != XmlPullParser.END_TAG){
                    if (parser.eventType != XmlPullParser.START_TAG){
                        continue
                    }
                    when(parser.name){
                        "id" -> id = readID(parser)
                        "im:name" -> title= readTitle(parser)
                        "summary" ->summary= readSummary(parser)
                        else -> skip(parser)
                    }
                }
                list.add(Group(title!!,id!!,summary!!))
            }
            else{
                skip(parser)
            }
        }
        return list
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG){
            throw IllegalStateException()
        }
        var depth=1
        while (depth != 0){
            when (parser.next()){
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    private fun readSummary(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG,ns,"summary")
        val summary= readText(parser)
        parser.require(XmlPullParser.END_TAG,ns,"summary")
        val message= Html.fromHtml(Html.fromHtml(summary!!).toString())
        return "$message"
    }

    private fun readID(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG,ns,"id")
        val name= readText(parser)
        parser.require(XmlPullParser.END_TAG,ns,"id")
        return name
    }

    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG,ns,"im:name")
        val title= readText(parser)
        parser.require(XmlPullParser.END_TAG,ns,"im:name")
        return title
    }

    private fun readText(parser: XmlPullParser): String {
        var result= ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }
}