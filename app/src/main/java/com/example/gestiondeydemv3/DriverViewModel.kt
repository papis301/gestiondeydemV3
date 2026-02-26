package com.example.gestiondeydemv3

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.mutableStateOf
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class DriverViewModel(application: Application) : AndroidViewModel(application) {

    val drivers = mutableStateListOf<Driver>()
    val loading = mutableStateOf(false)
    val searchQuery = mutableStateOf("")
    val filterStatus = mutableStateOf("all")
// all | blocked | active

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
                            bloque = o.getInt("bloque_par_admin"),

                            // ✅ Champs ajoutés correctement
                            typeVehicule = o.optString("type_vehicule", "Non défini"),
                            totalCourses = o.optInt("total_courses", 0),
                            ratingAverage = o.optDouble("rating_average", 0.0),
                            createdAt = o.optString("created_at", "")
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

//    fun filteredDrivers(): List<Driver> {
//        val query = searchQuery.value.trim()
//
//        if (query.isEmpty()) return drivers
//
//        return drivers.filter {
//            it.phone.contains(query, ignoreCase = true)
//        }
//    }

    fun filteredDrivers(): List<Driver> {

        val filtered = when (filterStatus.value) {
            "blocked" -> drivers.filter { it.bloque == 1 }
            "active" -> drivers.filter { it.bloque == 0 }
            else -> drivers
        }

        return if (searchQuery.value.isBlank()) {
            filtered
        } else {
            filtered.filter {
                it.phone.contains(searchQuery.value)
            }
        }
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

    fun toggleBlockDriver(driverId: Int) {

        val url = "https://pisco.alwaysdata.net/toggle_driver_block.php"

        val request = object : StringRequest(
            Method.POST,
            url,
            { response ->
                Log.d("SERVER_RESPONSE", response)
                try {
                    val json = JSONObject(response)

                    val success = json.optBoolean("success", false)
                    val message = json.optString("message", "Réponse inconnue")

                    Toast.makeText(
                        getApplication(),
                        message,
                        Toast.LENGTH_LONG
                    ).show()

                    if (success) {
                        loadDrivers() // 🔄 refresh seulement si succès
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        getApplication(),
                        "Erreur parsing JSON",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            { error ->
                Toast.makeText(
                    getApplication(),
                    "Erreur serveur: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf("driver_id" to driverId.toString())
            }
        }

        queue.add(request)
    }

}



