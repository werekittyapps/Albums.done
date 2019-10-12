package ru.werekitty.albums

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_photos.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import android.text.method.TextKeyListener.clear
import java.util.*


// Адаптер списка
class ListAdapter(private val names: MutableList<String>,
                  private val id: MutableList<Int>,
                  context: Context
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>(){

    val mContext = context

    private lateinit var mTapAction : TapAction
    private lateinit var mLongTapAction : LongTapAction

    interface TapAction {
        fun onTap(text: String, id: Int){
        }
    }

    interface LongTapAction {
        fun onLongTap(text: String, id: Int){
        }
    }

    class ListViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val listText: TextView = v.findViewById(R.id.listTextView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ListAdapter.ListViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_helper, viewGroup, false)
        return ListViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ListViewHolder, position: Int) {
        viewHolder.listText.text = names[position]
        viewHolder.listText.setOnClickListener{
            mTapAction = mContext as TapAction
            mTapAction.onTap(names[position], id[position])
        }
        viewHolder.listText.setOnLongClickListener{
            mLongTapAction = mContext as LongTapAction
            mLongTapAction.onLongTap(names[position], id[position])
            false
        }
    }

    override fun getItemCount(): Int {
        return names.size
    }
}

// Фильтр поиска
fun filter(charText: String,
           album_titles_list: MutableList<String>,
           album_titles_list_full: MutableList<String>,
           id: MutableList<Int>,
           id_full: MutableList<Int>,
           adapterView: RecyclerView.Adapter<*>) {
    val mViewAdapter = adapterView
    var charText = charText
    charText = charText.toLowerCase(Locale.getDefault())
    album_titles_list.clear()
    id.clear()
    if (charText.length < 2) {
        album_titles_list.addAll(album_titles_list_full)
        id.addAll(id_full)
    } else {
        if (charText.length >= 2){
            for (wp in 0 until album_titles_list_full.size) {
                if (album_titles_list_full[wp].toLowerCase(Locale.getDefault()).contains(charText)) {
                    album_titles_list.add(album_titles_list_full[wp])
                    id.add(id_full[wp])
                }
            }
        }

    }
    mViewAdapter.notifyDataSetChanged()
}

// Настройка поиска
class listener (private val album_titles_list: MutableList<String>,
                private val album_titles_list_full: MutableList<String>,
                private val id: MutableList<Int>,
                private val id_full: MutableList<Int>,
                adapterView: RecyclerView.Adapter<*>) : android.widget.SearchView.OnQueryTextListener {
    val mViewAdapter = adapterView
    override fun onQueryTextChange(p0: String?): Boolean {
        filter(p0!!, album_titles_list, album_titles_list_full,id, id_full, mViewAdapter)
        return false
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }
}

// Настройка списка
fun recyclerHelper (context: Context, recyclerView: RecyclerView, adapterView: RecyclerView.Adapter<*>, list: MutableList<String>){
    recyclerView.apply {
        setHasFixedSize(true)
        adapter = adapterView
        layoutManager = LinearLayoutManager(this.context)
        addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
    }
}

// Адаптер картинок
class ImagesAdapter(private val images: MutableList<String>) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

    class ViewHolder(val image: ImageView) : RecyclerView.ViewHolder(image)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val image = LayoutInflater.from(parent.context).inflate(R.layout.image_helper, parent, false) as ImageView
        return ViewHolder(image)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        imagesLoader(holder.image).icons().execute(images[position])
    }

    override fun getItemCount(): Int = images.size

}

// Загрузка картинок
class imagesLoader(private val imageView: ImageView){
    inner class icons() : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg url: String?): Bitmap {

            var bitmap: Bitmap
            val connection = URL(url[0]).openConnection() as HttpURLConnection
            try {
                connection.connect()
                bitmap = BitmapFactory.decodeStream(connection.inputStream)
            } finally {
                connection.disconnect()
            }
            return bitmap
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            imageView.setImageBitmap(result)
        }
    }
}

// Настройка мозайки картинок
fun recyclerImageHelper (context: Context, recyclerView: RecyclerView, adapterView: RecyclerView.Adapter<*>){
    recyclerView.apply {
        setHasFixedSize(true)
        adapter = adapterView
        layoutManager = GridLayoutManager(this.context, 5)
    }
}

// Запрос фотографов
class photographsHandler(private val names: MutableList<String>, private val id: MutableList<Int>, adapterView: RecyclerView.Adapter<*>) {

    val mAdapterView = adapterView

    inner class photographsApi() : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg url: String?): String {

            var text: String
            val connection = URL(url[0]).openConnection() as HttpURLConnection
            try {
                connection.connect()
                text =
                    connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
            } catch (e: FileNotFoundException) {
                text = "error"
            } finally {
                connection.disconnect()
            }

            return text

        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            photographsJson(result)
        }
    }

    private fun photographsJson(jsonString: String?) {

        if (jsonString != "error") {
            val array = JSONArray(jsonString)

            for (i in 0 until array.length()){

                val curObject = array.getJSONObject(i)

                try {
                    val user_name = curObject.get("username")
                    val user_id = curObject.get("id")

                    names.add(user_name.toString())
                    id.add(user_id.toString().toInt())
                    mAdapterView.notifyDataSetChanged()

                } catch (e: JSONException) {
                    //
                }
            }

        } else {
            //
        }

    }
}

// Запрос альбомов
class albumsHandler(private val titles: MutableList<String>,
                    private val titles_full: MutableList<String>,
                    private val id: MutableList<Int>,
                    private val id_full: MutableList<Int>,
                    adapterView: RecyclerView.Adapter<*>) {

    val mAdapterView = adapterView

    inner class albumsApi() : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg url: String?): String {

            var text: String
            val connection = URL(url[0]).openConnection() as HttpURLConnection
            try {
                connection.connect()
                text =
                    connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
            } catch (e: FileNotFoundException) {
                text = "error"
            } finally {
                connection.disconnect()
            }

            return text

        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            albumsJson(result)
        }
    }

    private fun albumsJson(jsonString: String?) {

        if (jsonString != "error") {
            val array = JSONArray(jsonString)

            for (i in 0 until array.length()){

                val curObject = array.getJSONObject(i)

                try {
                    val album_title = curObject.get("title")
                    val album_id = curObject.get("id")

                    titles.add(album_title.toString())
                    titles_full.add(album_title.toString())
                    id.add(album_id.toString().toInt())
                    id_full.add(album_id.toString().toInt())
                    mAdapterView.notifyDataSetChanged()

                } catch (e: JSONException) {
                    //
                }
            }

        } else {
            //
        }
    }
}

// Запрос фотографий
class photosHandler(private val titles: MutableList<String>, private val id: MutableList<Int>, private val full_urls: MutableList<String>, private val urls: MutableList<String>, adapterView: RecyclerView.Adapter<*>) {

    val mViewAdapter = adapterView

    inner class photosApi() : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg url: String?): String {

            var text: String
            val connection = URL(url[0]).openConnection() as HttpURLConnection
            try {
                connection.connect()
                text =
                    connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
            } catch (e: FileNotFoundException) {
                text = "error"
            } finally {
                connection.disconnect()
            }

            return text

        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            photosJson(result)
        }
    }

    private fun photosJson(jsonString: String?) {

        if (jsonString != "error") {
            val array = JSONArray(jsonString)

            for (i in 0 until array.length()){

                val curObject = array.getJSONObject(i)

                try {
                    val photo_title = curObject.get("title")
                    val photo_id = curObject.get("id")
                    val photo_url = curObject.get("thumbnailUrl")

                    titles.add(photo_title.toString())
                    id.add(photo_id.toString().toInt())
                    full_urls.add(photo_url.toString())

                } catch (e: JSONException) {
                    //
                }
            }

            for (i in 0 until 10){
                urls.add(full_urls[i])
            }

            mViewAdapter.notifyDataSetChanged()

        } else {
            //
        }
    }
}