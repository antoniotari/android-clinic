package com.example.myapplication.networking

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.myapplication.*
import com.squareup.okhttp.*
import org.json.JSONObject
import java.io.IOException


class OkHttpExample(private val url: String) {
    private var urlBuilder: HttpUrl.Builder
    // avoid creating several instances, OkHttpClient should be a singleton
    private var client: OkHttpClient = OkHttpClient()

    init {
        urlBuilder = HttpUrl.parse(url).newBuilder()
    }

    companion object {
        val JSON: MediaType = MediaType.parse("application/json; charset=utf-8")
    }

    /**
     * add parameters to your get request
     */
    public fun addParameters(params: HashMap<String, String>): HttpUrl.Builder {
        for ((key, value) in params) {
            urlBuilder.addQueryParameter(key, value)
        }
        return urlBuilder
    }

    /**
     * simple get request, this request is synchronous, will block the UI and eventually crash the
     * app, to avoid this run it on the background or use async call
     */
    @Throws(IOException::class)
    fun doGetRequest(): String? {
        return client.newCall(buildRequest())
                .execute()
                .body()
                .string()
    }

    /**
     * simple post request, this request is synchronous, will block the UI and eventually crash the
     * app, to avoid this run it on the background or use async call
     */
    @Throws(IOException::class)
    fun doPostRequest(url: String?, json: String?): String? {
        val body = RequestBody.create(JSON, json)
        val request: Request = Request.Builder()
                .url(url)
                .post(body)
                .build()
        val response = client.newCall(request).execute()
        return response.body().string()
    }

    /**
     * add headers to your request builder
     */
    public fun addHeaders(headers: HashMap<String, String>?,
                          builder: Request.Builder): Request.Builder {
        if (headers == null) return builder

        for ((key, value) in headers) {
            builder.header(key, value)
        }
        return builder
    }


    // ctrl+o to generate callback methods
    /**
     * make an async call, this method uses a broadcast in the callback to notify whoeveer is listening
     * to it that we have received data, notice that I used this only to show how to use local broadcasters,
     * this is not the proper way to notify components, use interfaces callbacks or reactive programming instead
     */
    public fun asyncCall(context: Context?) {
        client.newCall(buildRequest()).enqueue(object : Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(response: Response?) {
                if (response?.isSuccessful == true) {
                    val responseJson = JSONObject(response.body().string())
                    val imageUrl = extractImageUrlFromJson(responseJson,1)
                    val title = extractTitleFromJson(responseJson,1)
                    sendMessage(context,
                            imageUrl,
                            title)
                } else {
                    throw IOException("Unexpected code $response");
                }
            }
        })
    }

    /**
     * method to extract info from a jsonobject, in our example the json looks like this:
     * {
     *      "images": [
     *          {
     *              "imageUrl": "https://www.someUrl.ca",
     *              "title": "android class"
     *          },
     *          {
     *              "imageUrl": "https://www.someOtherUrl.ca",
     *              "title": "another android kotlin image"
     *          }
     *      ]
     * }
     *
     */
    private fun extractImageUrlFromJson(json: JSONObject, position:Int): String? {
        return json
                .getJSONArray("images")
                .getJSONObject(position)
                .getString("imageUrl")
    }

    private fun extractTitleFromJson(json: JSONObject, position:Int): String? {
        return json
                .getJSONArray("images")
                .getJSONObject(position)
                .getString("title")
    }

    private fun buildRequest(): Request {
        val urlBuilt = urlBuilder.build().toString()
        return Request.Builder()
                .url(urlBuilt)
                .build()
    }


    // Send an Intent with an action named "custom-event-name". The Intent sent should
    // be received by the ReceiverActivity.
    private fun sendMessage(context: Context?, message: String?, title: String?) {
        val intent = Intent(BROADCAST_EVENT_NAME)
        // You can also include some extra data.
        intent.putExtra(BROADCAST_KEY_IMAGEURL, message)
        intent.putExtra(BROADCAST_KEY_TITLE, title)

        context?.let { LocalBroadcastManager.getInstance(it).sendBroadcast(intent) }
        // the previous like is the same as doing this
        // if (context != null) LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}