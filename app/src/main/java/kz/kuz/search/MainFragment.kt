package kz.kuz.search

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

// создаю в заголовке поле поиска, при вводе выводится html результатов google поиска
// после отправки запроса скрывается клавиатура, убирается поле поиска, выводится надпись
// please wait, после получения результатов вместо надписи выходит html
class MainFragment : Fragment() {
    private lateinit var searchItem: MenuItem
    lateinit var textView: TextView
    var handler = Handler(Looper.getMainLooper())

    // методы фрагмента должны быть открытыми
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.setTitle(R.string.toolbar_title)
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        textView = view.findViewById(R.id.textView)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_main, menu)
        searchItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.getActionView() as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                textView.text = "Please wait"
                searchView.clearFocus() // скрываю клавиатуру
                searchView.onActionViewCollapsed() // скрываю панель поиска
                QueryPreferences.setStoredQuery(activity, query)
                handler.post {
                    val executorService = Executors.newFixedThreadPool(5)
                    try {
                        val myCallable = MyCallable(query)
                        val future: Future<*> = executorService.submit(myCallable)
                        val content = future.get() as String
                        textView.text = content
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.d("Search", "QueryTextChange: $newText")
                return false
            }
        })
        searchView.setOnSearchClickListener {
            val query = QueryPreferences.getStoredQuery(activity)
            searchView.setQuery(query, false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                QueryPreferences.setStoredQuery(activity, null)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

internal class MyCallable(var mQuery: String) : Callable<String?> {
    private val urlString = arrayOfNulls<String>(1)
    private var searchUrl = "https://www.google.com/search?q=$mQuery"
    override fun call(): String? {
        var url: URL? = null
        try {
            url = URL(searchUrl)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        var connection: HttpURLConnection? = null
        try {
            connection = url!!.openConnection() as HttpURLConnection
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val out = ByteArrayOutputStream()
        var `in`: InputStream? = null
        try {
            `in` = connection!!.inputStream
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            if (connection!!.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException(connection.responseMessage +
                        ": with " + searchUrl)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var bytesRead = 0
        val buffer = ByteArray(1024)
        while (true) {
            try {
                if (`in`!!.read(buffer).also { bytesRead = it } <= 0) break
            } catch (e: IOException) {
                e.printStackTrace()
            }
            out.write(buffer, 0, bytesRead)
        }
        try {
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        connection!!.disconnect()
        val urlBytes = out.toByteArray()
        urlString[0] = String(urlBytes)
        return urlString[0]
    }
}