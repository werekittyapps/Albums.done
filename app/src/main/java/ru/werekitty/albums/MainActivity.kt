package ru.werekitty.albums

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ListAdapter.TapAction, ListAdapter.LongTapAction,
    photographsCall.PostUserCall, albumsCall.PostAlbumCall, photosCall.PostPhotoCall,
    ChangeNameDialog.Listener{

    var list_all_objects : MutableList<String> = mutableListOf()
    var variable_list_all_objects : MutableList<String> = mutableListOf()

    var user_names_list : MutableList<String> = mutableListOf()
    var user_id_list : MutableList<Int> = mutableListOf()
    var list_t_u_id : MutableList<Int> = mutableListOf()

    var list_user: MutableList<Int> = mutableListOf()
    var list_album_id: MutableList<Int> = mutableListOf()
    var list_album_title: MutableList<String> = mutableListOf()

    var list_album: MutableList<Int> = mutableListOf()
    var list_photo_id: MutableList<Int> = mutableListOf()
    var list_photo_title: MutableList<String> = mutableListOf()
    var list_photo_url: MutableList<String> = mutableListOf()

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewAdapterForAll: RecyclerView.Adapter<*>
    private lateinit var mListener: listenerForAll

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewAdapter = ListAdapter(user_names_list, user_id_list, this)
        recyclerHelper(this, PhotographsListView, viewAdapter, user_names_list)

        viewAdapterForAll = SecondListAdapter(variable_list_all_objects)
        recyclerHelper(this, FullListView, viewAdapterForAll, variable_list_all_objects)

        loadAllList(this, list_all_objects)
        if (list_all_objects.isEmpty()) {
            // если нет кэша, делаем запросы и сохраняем
            val url_users = "https://jsonplaceholder.typicode.com/users"
            photographsCall(user_names_list, user_id_list, this).photographsApi().execute(url_users)
        } else {
            // если есть, выгружаем данные из кэша
            loadUserList(this, list_t_u_id, user_id_list, user_names_list)
            viewAdapter.notifyDataSetChanged()
        }
        mListener = listenerForAll(list_all_objects, variable_list_all_objects, viewAdapterForAll)
        mainSearch.setOnQueryTextListener(mListener)
    }

    override fun onTap(text: String, id: Int) {
        val intent = Intent(this, AlbumsActivity::class.java)
        intent.putExtra("userID", id)
        startActivity(intent)
    }

    override fun onLongTap(text: String, id: Int) {
        ChangeNameDialog(id).show(supportFragmentManager, "ChangeNameDialog")
    }

    override fun DialogHelper(text: String, id: Int) {
        if(list_t_u_id.isEmpty()) {
            // значит кэша нет и мы апдейтим только лист
            user_names_list[id-1] = text
            viewAdapter.notifyDataSetChanged()
        } else {
            // значит кэш есть и мы апдейтим и лист, и базу данных (как раз по list_t_u_id)
            user_names_list[id-1] = text
            updateUser(this, id-1, list_t_u_id, user_names_list)
            viewAdapter.notifyDataSetChanged()
        }
    }

    override fun onPostUserCall() {
        val url_albums = "https://jsonplaceholder.typicode.com/albums"
        albumsCall(list_album_title, list_user, list_album_id, this).albumsApi().execute(url_albums)
    }

    override fun onPostAlbumCall() {
        val url_photos = "https://jsonplaceholder.typicode.com/photos"
        photosCall(list_photo_title, list_album, list_photo_id, list_photo_url, this).photosApi().execute(url_photos)
    }

    override fun onPostPhotoCall() {
        saveUsers(this, user_id_list, user_names_list)
        saveAlbums(this, list_user, list_album_id, list_album_title)
        savePhotos(this, list_album, list_photo_id, list_photo_title, list_photo_url)
        loadAllList(this, list_all_objects)
        viewAdapter.notifyDataSetChanged()
        println("thats it")
    }

    fun deleteDatabase(view: View) {
        deleteCache(this)
        Toast.makeText(this, "кэш очищен", Toast.LENGTH_SHORT).show()
    }

}
