package ru.werekitty.albums

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteCursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
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
import android.util.AttributeSet
import com.bumptech.glide.Glide
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
            //mLongTapAction.onLongTap(names[position], id[position])
            mLongTapAction.onLongTap(names[position], position)
            false
        }
    }

    override fun getItemCount(): Int {
        return names.size
    }
}

// Адаптер для списка, в котором ведется полный поиск
class SecondListAdapter(private val full: MutableList<String>
) : RecyclerView.Adapter<SecondListAdapter.SecondListViewHolder>(){

    class SecondListViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val listText: TextView = v.findViewById(R.id.listTextView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): SecondListAdapter.SecondListViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_helper, viewGroup, false)
        return SecondListViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: SecondListViewHolder, position: Int) {
        viewHolder.listText.text = full[position]
    }

    override fun getItemCount(): Int {
        return full.size
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

// Фильтр поиска всего
fun filterForAll(charText: String,
                 list_all_objects: MutableList<String>,
                 variable_list_all_objects: MutableList<String>,
                 adapterView: RecyclerView.Adapter<*>) {
    val mViewAdapter = adapterView
    var charText = charText
    charText = charText.toLowerCase(Locale.getDefault())
    variable_list_all_objects.clear()
    if (charText.length < 2) {
        variable_list_all_objects.clear()
    } else {
        if (charText.length >= 2){
            for (wp in 0 until list_all_objects.size) {
                if (list_all_objects[wp].toLowerCase(Locale.getDefault()).contains(charText)) {
                    variable_list_all_objects.add(list_all_objects[wp])
                }
            }
        }

    }
    mViewAdapter.notifyDataSetChanged()
}

// Настройка поиска
class listenerForAll (private val list_all_objects: MutableList<String>,
                      private val variable_list_all_objects: MutableList<String>,
                      adapterView: RecyclerView.Adapter<*>) : android.widget.SearchView.OnQueryTextListener {
    val mViewAdapter = adapterView
    override fun onQueryTextChange(p0: String?): Boolean {
        filterForAll(p0!!, list_all_objects, variable_list_all_objects, mViewAdapter)
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
        holder.image.loadImage(images[position])

    }

    private fun ImageView.loadImage(image: String) {
        Glide.with(this).load(image).into(this)
    }

    override fun getItemCount(): Int = images.size

}

// Квадратное view для картинки - исправляет ошибку некорректной отрисовки списка
class SquareImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
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
class photographsHandler(private val names: MutableList<String>,
                         private val id: MutableList<Int>/*,
                    adapterView: RecyclerView.Adapter<*>*/) {

    //val mAdapterView = adapterView

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
                    //mAdapterView.notifyDataSetChanged()

                } catch (e: JSONException) {
                    //
                }
            }

        } else {
            //
        }

    }
}

// Запрос фотографов (main)
class photographsCall(private val names: MutableList<String>,
                      private val id: MutableList<Int>,
                      context: Context) {

    val mContext = context
    private lateinit var mPostCall : PostUserCall

    interface PostUserCall {
        fun onPostUserCall(){}
    }

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
                    //mAdapterView.notifyDataSetChanged()


                } catch (e: JSONException) {
                    //
                }
            }
            mPostCall = mContext as PostUserCall
            mPostCall.onPostUserCall()
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

// Запрос альбомов (main)
class albumsCall(private val titles: MutableList<String>,
                 private val user: MutableList<Int>,
                 private val id: MutableList<Int>,
                 context: Context) {

    val mContext = context
    private lateinit var mPostCall : PostAlbumCall

    interface PostAlbumCall {
        fun onPostAlbumCall(){}
    }

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
                    val user_id = curObject.get("userId")
                    val album_id = curObject.get("id")

                    titles.add(album_title.toString())
                    user.add(user_id.toString().toInt())
                    id.add(album_id.toString().toInt())

                } catch (e: JSONException) {
                    //
                }
            }
            mPostCall = mContext as PostAlbumCall
            mPostCall.onPostAlbumCall()
        } else {
            //
        }
    }
}

// Запрос фотографий
class photosHandler(private val titles: MutableList<String>,
                    private val id: MutableList<Int>,
                    private val full_urls: MutableList<String>,
                    private val urls: MutableList<String>,
                    adapterView: RecyclerView.Adapter<*>) {

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

// Запрос фотографий (main)
class photosCall(private val titles: MutableList<String>,
                 private val album: MutableList<Int>,
                 private val id: MutableList<Int>,
                 private val urls: MutableList<String>,
                 context: Context) {

    val mContext = context
    private lateinit var mPostCall : PostPhotoCall

    interface PostPhotoCall {
        fun onPostPhotoCall(){}
    }

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
                    val album_id = curObject.get("albumId")
                    val photo_id = curObject.get("id")
                    val photo_url = curObject.get("thumbnailUrl")

                    titles.add(photo_title.toString())
                    album.add(album_id.toString().toInt())
                    id.add(photo_id.toString().toInt())
                    urls.add(photo_url.toString())

                } catch (e: JSONException) {
                    //
                }
            }
            mPostCall = mContext as PostPhotoCall
            mPostCall.onPostPhotoCall()
        } else {
            //
        }
    }
}

// SQLite база данных для кэширования запросов
class DBWorkHelper(context: Context)
    : SQLiteOpenHelper(context, "AlbumsDB", null, 1) {

    //val db_version : Int = 1
    //val db_name = "AlbumsDB"
    val table_users = "usersTable"
    val table_albums = "albumsTable"
    val table_photos = "photosTable"

    val key_t_u_id = "_id"
    val key_user_id = "user_id"
    val key_user_name = "user_name"

    val key_t_a_id = "_id"
    val key_user = "user_id"
    val key_album_id = "album_id"
    val key_album_title = "album_title"

    val key_t_p_id = "_id"
    val key_album = "album_id"
    val key_photo_id = "photo_id"
    val key_photo_title = "photo_title"
    val key_photo_url = "photo_url"


    override fun onCreate(database: SQLiteDatabase?) {
        database!!.execSQL("create table " + table_users + "("
                + key_t_u_id + " integer primary key,"
                + key_user_id + " integer,"
                + key_user_name + " text"
                + ")")

        database.execSQL("create table " + table_albums + "("
                + key_t_a_id + " integer primary key,"
                + key_user + " integer,"
                + key_album_id + " integer,"
                + key_album_title + " text"
                + ")")

        database.execSQL("create table " + table_photos + "("
                + key_t_p_id + " integer primary key,"
                + key_album + " integer,"
                + key_photo_id + " integer,"
                + key_photo_title + " text,"
                + key_photo_url + " text"
                + ")")
    }

    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        database!!.execSQL("drop table if exists " + table_users)
        database.execSQL("drop table if exists " + table_albums)
        database.execSQL("drop table if exists " + table_photos)

        onCreate(database)
    }
}

// Удаление кэшированных данных
fun deleteCache(context: Context) {
    val database : SQLiteDatabase
    database = DBWorkHelper(context).writableDatabase
    database.delete(DBWorkHelper(context).table_users, null, null)
    database.delete(DBWorkHelper(context).table_albums, null, null)
    database.delete(DBWorkHelper(context).table_photos, null, null)
    DBWorkHelper(context).close()
    Glide.get(context).clearMemory();
}

// Выгружает список фотографов из базы данных
fun loadUserList(context: Context,
                 list_t_u_id: MutableList<Int>,
                 list_user_id: MutableList<Int>,
                 list_user_name: MutableList<String>) {

    val database : SQLiteDatabase
    val cursor : Cursor
    database = DBWorkHelper(context).writableDatabase
    cursor = database.query(DBWorkHelper(context).table_users,
        null, null, null, null, null, null)

    if (cursor.moveToFirst()){
        val id = cursor.getColumnIndex(DBWorkHelper(context).key_t_u_id)
        val user_id = cursor.getColumnIndex(DBWorkHelper(context).key_user_id)
        val user_name = cursor.getColumnIndex(DBWorkHelper(context).key_user_name)
        do {
            list_t_u_id.add(cursor.getInt(id))
            list_user_id.add(cursor.getInt(user_id))
            list_user_name.add(cursor.getString(user_name))
        } while (cursor.moveToNext())
    }

    cursor.close()
    DBWorkHelper(context).close()
}

// Выгружает список альбомов из базы данных
fun loadAlbumList(context: Context,
                  user: Int,
                  list_t_a_id: MutableList<Int>,
                  list_user: MutableList<Int>,
                  list_album_id: MutableList<Int>,
                  list_album_title: MutableList<String>) {

    val database : SQLiteDatabase
    val cursor : Cursor
    database = DBWorkHelper(context).writableDatabase
    cursor = database.query(DBWorkHelper(context).table_albums,
        null, DBWorkHelper(context).key_user  + "= " + user, null, null, null, null)

    if (cursor.moveToFirst()){
        val id = cursor.getColumnIndex(DBWorkHelper(context).key_t_a_id)
        val user = cursor.getColumnIndex(DBWorkHelper(context).key_user)
        val album_id = cursor.getColumnIndex(DBWorkHelper(context).key_album_id)
        val album_title = cursor.getColumnIndex(DBWorkHelper(context).key_album_title)
        do {
            list_t_a_id.add(cursor.getInt(id))
            list_user.add(cursor.getInt(user))
            list_album_id.add(cursor.getInt(album_id))
            list_album_title.add(cursor.getString(album_title))
        } while (cursor.moveToNext())
    }

    cursor.close()
    DBWorkHelper(context).close()
}

// Выгружает список фотографий из базы данных
fun loadPhotoList(context: Context,
                  album: Int,
                  list_t_p_id: MutableList<Int>,
                  list_album: MutableList<Int>,
                  list_photo_id: MutableList<Int>,
                  list_photo_title: MutableList<String>,
                  list_photo_url: MutableList<String>) {

    val database : SQLiteDatabase
    val cursor : Cursor
    database = DBWorkHelper(context).writableDatabase
    cursor = database.query(DBWorkHelper(context).table_photos,
        null, DBWorkHelper(context).key_album  + "= " + album, null, null, null, null)

    if (cursor.moveToFirst()){
        val id = cursor.getColumnIndex(DBWorkHelper(context).key_t_p_id)
        val album = cursor.getColumnIndex(DBWorkHelper(context).key_album)
        val photo_id = cursor.getColumnIndex(DBWorkHelper(context).key_photo_id)
        val photo_title = cursor.getColumnIndex(DBWorkHelper(context).key_photo_title)
        val album_url = cursor.getColumnIndex(DBWorkHelper(context).key_photo_url)
        do {
            list_t_p_id.add(cursor.getInt(id))
            list_album.add(cursor.getInt(album))
            list_photo_id.add(cursor.getInt(photo_id))
            list_photo_title.add(cursor.getString(photo_title))
            list_photo_url.add(cursor.getString(album_url))
        } while (cursor.moveToNext())
    }

    cursor.close()
    DBWorkHelper(context).close()
}

// Выгружает список всех объектов из базы данных
fun loadAllList(context: Context,
                 list_all_objects: MutableList<String>) {

    val database : SQLiteDatabase
    database = DBWorkHelper(context).writableDatabase

    // фотографы
    val user_cursor : Cursor
    user_cursor = database.query(DBWorkHelper(context).table_users,
        null, null, null, null, null, null)
    if (user_cursor.moveToFirst()){
        val user_name = user_cursor.getColumnIndex(DBWorkHelper(context).key_user_name)
        do {
            list_all_objects.add(user_cursor.getString(user_name))
        } while (user_cursor.moveToNext())
    }
    user_cursor.close()

    // альбомы
    val album_cursor : Cursor
    album_cursor = database.query(DBWorkHelper(context).table_albums,
        null, null, null, null, null, null)
    if (album_cursor.moveToFirst()){
        val album_title = album_cursor.getColumnIndex(DBWorkHelper(context).key_album_title)
        do {
            list_all_objects.add(album_cursor.getString(album_title))
        } while (album_cursor.moveToNext())
    }
    album_cursor.close()

    // фото
    val photo_cursor : Cursor
    photo_cursor = database.query(DBWorkHelper(context).table_photos,
        null, null, null, null, null, null)
    if (photo_cursor.moveToFirst()){
        val photo_title = photo_cursor.getColumnIndex(DBWorkHelper(context).key_photo_title)
        do {
            list_all_objects.add(photo_cursor.getString(photo_title))
        } while (photo_cursor.moveToNext())
    }
    photo_cursor.close()

    DBWorkHelper(context).close()
}

// Сохранить фотографов
fun saveUsers(context: Context,
              list_user_id: MutableList<Int>,
              list_user_name: MutableList<String>) {
    val database : SQLiteDatabase
    database = DBWorkHelper(context).writableDatabase
    for (i in 0 until list_user_id.size) {
        val contentValues : ContentValues = ContentValues()
        contentValues.put(DBWorkHelper(context).key_user_id, list_user_id[i])
        contentValues.put(DBWorkHelper(context).key_user_name, list_user_name[i])

        database.insert(DBWorkHelper(context).table_users, null, contentValues)
    }
    database.close()
}

// Сохранить альбомы
fun saveAlbums(context: Context,
               list_user: MutableList<Int>,
               list_album_id: MutableList<Int>,
               list_album_title: MutableList<String>) {
    val database : SQLiteDatabase
    database = DBWorkHelper(context).writableDatabase
    for (i in 0 until list_album_id.size) {
        val contentValues : ContentValues = ContentValues()
        contentValues.put(DBWorkHelper(context).key_user, list_user[i])
        contentValues.put(DBWorkHelper(context).key_album_id, list_album_id[i])
        contentValues.put(DBWorkHelper(context).key_album_title, list_album_title[i])

        database.insert(DBWorkHelper(context).table_albums, null, contentValues)
    }
    database.close()
}

// Сохранить фото
fun savePhotos(context: Context,
               list_album: MutableList<Int>,
               list_photo_id: MutableList<Int>,
               list_photo_title: MutableList<String>,
               list_photo_url: MutableList<String>) {
    val database : SQLiteDatabase
    database = DBWorkHelper(context).writableDatabase
    for (i in 0 until list_photo_id.size) {
        val contentValues : ContentValues = ContentValues()
        contentValues.put(DBWorkHelper(context).key_album, list_album[i])
        contentValues.put(DBWorkHelper(context).key_photo_id, list_photo_id[i])
        contentValues.put(DBWorkHelper(context).key_photo_title, list_photo_title[i])
        contentValues.put(DBWorkHelper(context).key_photo_url, list_photo_url[i])

        database.insert(DBWorkHelper(context).table_photos, null, contentValues)
    }
    database.close()
}

// Смена имени фотографа
fun updateUser(context: Context,
               index: Int,
               list_t_u_id : MutableList<Int>,
               list_user_name: MutableList<String>) {
    val database : SQLiteDatabase
    database = DBWorkHelper(context).writableDatabase
    val contentValues : ContentValues = ContentValues()
    contentValues.put(DBWorkHelper(context).key_user_name, list_user_name[index])

    database.update(DBWorkHelper(context).table_users, contentValues,
        DBWorkHelper(context).key_t_u_id + "= " + list_t_u_id[index], null)
    database.close()
}

// Смена названия альбома
fun updateAlbum(context: Context,
               index: Int,
               list_t_a_id : MutableList<Int>,
               list_album_title: MutableList<String>) {
    val database : SQLiteDatabase
    database = DBWorkHelper(context).writableDatabase
    val contentValues : ContentValues = ContentValues()
    contentValues.put(DBWorkHelper(context).key_album_title, list_album_title[index])

    database.update(DBWorkHelper(context).table_albums, contentValues,
        DBWorkHelper(context).key_t_a_id + "= " + list_t_a_id[index], null)
    database.close()
}

// Убирает лишние пробелы
fun removeSpaces(text: String) : String {
    var result = ""
    var prevChar = ""
    for (char in text) {
        if(!(prevChar == " " && char == ' ')){
            result += char
        }
        prevChar = char.toString()
    }
    return result.trim()
}