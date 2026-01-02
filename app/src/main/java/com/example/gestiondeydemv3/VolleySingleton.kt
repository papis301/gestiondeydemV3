package com.example.gestiondeydemv3

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleySingleton private constructor(context: Context) {

    private val requestQueue: RequestQueue =
        Volley.newRequestQueue(context.applicationContext)

    fun <T> add(request: Request<T>) {
        requestQueue.add(request)
    }

    companion object {
        @Volatile private var instance: VolleySingleton? = null

        fun getInstance(context: Context): VolleySingleton =
            instance ?: synchronized(this) {
                instance ?: VolleySingleton(context).also { instance = it }
            }
    }
}
