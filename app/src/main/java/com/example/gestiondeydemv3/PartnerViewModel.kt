package com.example.gestiondeydemv3

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class PartnerViewModel(application: Application) : AndroidViewModel(application) {

    val loading = mutableStateOf(false)

    private val queue = Volley.newRequestQueue(application)

    private val _partners = mutableStateListOf<Partner>()
    val partners: List<Partner> get() = _partners

    init {
        loadPartners()
    }

    // ================= LOAD =================
    fun loadPartners() {

        loading.value = true

        val url = "https://pisco.alwaysdata.net/get_partners.php"

        val request = JsonArrayRequest(
            url,
            { response ->

                _partners.clear()

                for (i in 0 until response.length()) {
                    val o = response.getJSONObject(i)

                    _partners.add(
                        Partner(
                            id = o.getInt("id"),
                            nom = o.getString("nom"),
                            email = o.optString("email", ""),
                            telephone = o.optString("telephone", ""),
                            adresse = o.optString("adresse", ""),
                            commissionPercent = o.optInt("commission_percent", 20),
                            solde = o.optInt("solde", 0),
                            createdAt = o.optString("created_at", "")
                        )
                    )
                }

                loading.value = false
            },
            { error ->
                loading.value = false
                Toast.makeText(
                    getApplication(),
                    "Erreur chargement partenaires",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        queue.add(request)
    }

    // ================= CREATE =================
    fun createPartner(
        nom: String,
        email: String,
        password: String,
        telephone: String,
        adresse: String,
        commission: Int
    ) {

        val url = "https://pisco.alwaysdata.net/create_partner.php"

        val request = object : StringRequest(
            Method.POST,
            url,
            { response ->

                try {
                    val json = JSONObject(response)
                    val message = json.optString("message", "Réponse serveur")

                    Toast.makeText(
                        getApplication(),
                        message,
                        Toast.LENGTH_LONG
                    ).show()

                    if (json.optBoolean("success", false)) {
                        loadPartners()
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        getApplication(),
                        "Erreur parsing serveur",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            { error ->
                Toast.makeText(
                    getApplication(),
                    "Erreur création partenaire",
                    Toast.LENGTH_SHORT
                ).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "nom" to nom,
                    "email" to email,
                    "password" to password,
                    "telephone" to telephone,
                    "adresse" to adresse,
                    "commission_percent" to commission.toString()
                )
            }
        }

        queue.add(request)
    }

    // ================= DELETE =================
    fun deletePartner(id: Int) {

        val url = "https://pisco.alwaysdata.net/delete_partner.php"

        val request = object : StringRequest(
            Method.POST,
            url,
            { response ->

                try {
                    val json = JSONObject(response)
                    val message = json.optString("message", "Réponse serveur")

                    Toast.makeText(
                        getApplication(),
                        message,
                        Toast.LENGTH_LONG
                    ).show()

                    if (json.optBoolean("success", false)) {
                        loadPartners()
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        getApplication(),
                        "Erreur parsing serveur",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            {
                Toast.makeText(
                    getApplication(),
                    "Erreur suppression",
                    Toast.LENGTH_SHORT
                ).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf("id" to id.toString())
            }
        }

        queue.add(request)
    }

    fun updatePartnerSolde(
        partnerId: Int,
        amount: Int,
        type: String // "add" ou "remove"
    ) {

        val url = "https://pisco.alwaysdata.net/update_partner_solde.php"

        val request = object : StringRequest(
            Method.POST,
            url,
            { response ->

                try {
                    val json = JSONObject(response)
                    val message = json.optString("message", "Réponse serveur")

                    Toast.makeText(
                        getApplication(),
                        message,
                        Toast.LENGTH_LONG
                    ).show()

                    if (json.optBoolean("success", false)) {
                        loadPartners()
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        getApplication(),
                        "Erreur parsing serveur",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            {
                Toast.makeText(
                    getApplication(),
                    "Erreur serveur",
                    Toast.LENGTH_SHORT
                ).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "partner_id" to partnerId.toString(),
                    "amount" to amount.toString(),
                    "type" to type
                )
            }
        }

        queue.add(request)
    }

    fun updatePartner(
        id: Int,
        nom: String,
        email: String,
        telephone: String,
        adresse: String,
        commission: Int
    ) {

        val url = "https://pisco.alwaysdata.net/update_partner.php"

        val request = object : StringRequest(
            Method.POST,
            url,
            { response ->

                try {
                    val json = JSONObject(response)
                    val message = json.optString("message", "Réponse serveur")

                    Toast.makeText(
                        getApplication(),
                        message,
                        Toast.LENGTH_LONG
                    ).show()

                    if (json.optBoolean("success", false)) {
                        loadPartners()
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        getApplication(),
                        "Erreur parsing serveur",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            {
                Toast.makeText(
                    getApplication(),
                    "Erreur serveur",
                    Toast.LENGTH_SHORT
                ).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "id" to id.toString(),
                    "nom" to nom,
                    "email" to email,
                    "telephone" to telephone,
                    "adresse" to adresse,
                    "commission_percent" to commission.toString()
                )
            }
        }

        queue.add(request)
    }
}