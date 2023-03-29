package kz.kuz.search

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.Future

// похоже, что этот класс не используется
class NewClass {
    @Throws(Exception::class)
    fun updateItems(query: String): String {
//        String query = QueryPreferences.getStoredQuery(getActivity());
        val searchUrl = "https://www.google.com/search?q=$query&tbm=isch"
        val urlString = arrayOfNulls<String>(1)
//        searchItem.setVisible(false);
//        textView.setText("Another text");
        val anotherString: String
        val executorService = Executors.newFixedThreadPool(5)
        val future: Future<*> = executorService.submit<String> {
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
            urlString[0]
        }
        anotherString = future.get() as String
        executorService.shutdown()
        return anotherString
//        textView.setText(urlString[0]);
//        ((MainActivity)getActivity()).ChangeFragment(new SecondFragment());
    }
}