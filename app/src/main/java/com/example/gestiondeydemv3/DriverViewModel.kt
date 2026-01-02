package com.example.gestiondeydemv3

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.mutableStateOf
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class DriverViewModel(application: Application) : AndroidViewModel(application) {

    val drivers = mutableStateListOf<Driver>()
    val loading = mutableStateOf(false)

    private val queue = Volley.newRequestQueue(application)

    init {
        loadDrivers() // 🔥 ICI, UNE SEULE FOIS
    }

    fun loadDrivers() {
        loading.value = true

        val url = "https://pisco.alwaysdata.net/get_drivers.php"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                drivers.clear()

                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)

                    drivers.add(
                        Driver(
                            id = o.getInt("id"),
                            phone = o.getString("phone"),
                            solde = o.getInt("solde"),
                            status = o.getString("status"),
                            isOnline = o.getInt("is_online"),
                            docsStatus = o.getString("docs_status"),
                            bloque = o.getInt("bloque_par_admin")
                        )
                    )
                }

                loading.value = false
            },
            {
                loading.value = false
            }
        )

        queue.add(request)
    }


    fun approveDriver(driverId: Int) {

        val url = "https://pisco.alwaysdata.net/approve_driver.php"

        Log.d("APPROVE_DRIVER", "➡️ Envoi approbation chauffeur ID = $driverId")

        val req = object : StringRequest(
            Method.POST,
            url,
            { response ->

                Log.d("APPROVE_DRIVER", "✅ Réponse serveur : $response")

                // Refresh réel de la liste
                loadDrivers()
            },
            { error ->

                Log.e(
                    "APPROVE_DRIVER",
                    "❌ Erreur Volley : ${error.message}",
                    error
                )
            }
        ) {

            override fun getParams(): Map<String, String> {
                val params = mapOf(
                    "driver_id" to driverId.toString()
                )

                Log.d("APPROVE_DRIVER", "📤 Params envoyés : $params")

                return params
            }
        }

        queue.add(req)
    }


    fun updateSolde(driverId: Int, solde: Int) {
        val url = "https://pisco.alwaysdata.net/update_driver_solde.php"

        val req = object : StringRequest(
            Method.POST, url,
            { response ->
                Log.e("SOLDE_UPDATE", response) // 🔥 AJOUTE ÇA
                loadDrivers()
            },
            { error ->
                Log.e("SOLDE_ERR", error.toString())
            }
        ) {
            override fun getParams() = mapOf(
                "driver_id" to driverId.toString(),
                "solde" to solde.toString()
            )
        }

        queue.add(req)
    }

}



