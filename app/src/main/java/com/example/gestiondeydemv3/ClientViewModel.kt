import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.gestiondeydemv3.Client
import org.json.JSONObject

class ClientViewModel(application: Application) : AndroidViewModel(application) {

    val loading = mutableStateOf(false)
    private val queue = Volley.newRequestQueue(application)

    private val _clients = mutableStateListOf<Client>()
    val clients: List<Client> get() = _clients

    init {
        fetchClients()
    }

    fun fetchClients() {
        loading.value = true

        val url = "https://pisco.alwaysdata.net/get_clients.php"

        val request = JsonArrayRequest(
            url,
            { response ->
                _clients.clear()

                for (i in 0 until response.length()) {
                    val o = response.getJSONObject(i)
                    _clients.add(parseClient(o))
                }

                loading.value = false
            },
            {
                loading.value = false
            }
        )

        queue.add(request)
    }

    private fun parseClient(o: JSONObject): Client {
        return Client(
            id = o.getInt("id"),
            phone = o.getString("phone"),
            name = o.getString("nom_profil"),
            createdAt = o.getString("created_at")
        )
    }
}
