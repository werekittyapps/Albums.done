package ru.werekitty.albums

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_albums.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class AlbumsActivity : AppCompatActivity(), ListAdapter.TapAction, ListAdapter.LongTapAction,
    ChangeNameDialog.Listener{

    var album_titles_list : MutableList<String> = mutableListOf()
    var album_titles_list_full : MutableList<String> = mutableListOf()
    var album_id_list : MutableList<Int> = mutableListOf()
    var album_id_list_full : MutableList<Int> = mutableListOf()
    var list_t_a_id: MutableList<Int> = mutableListOf()
    var list_user: MutableList<Int> = mutableListOf()

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var mListener: listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_albums)

        viewAdapter = ListAdapter(album_titles_list, album_id_list, this)
        recyclerHelper(this, AlbumsListView, viewAdapter, album_titles_list)

        val userID = intent.getIntExtra("userID", -1)

        loadAlbumList(this, userID, list_t_a_id, list_user, album_id_list, album_titles_list)
        list_t_a_id.clear()
        list_user.clear()
        loadAlbumList(this, userID, list_t_a_id, list_user, album_id_list_full, album_titles_list_full)

        if (album_titles_list.isEmpty()) {
            // если нет кэша, делаем запросы и сохраняем
            val url = "https://jsonplaceholder.typicode.com/albums/?userId=" + userID
            albumsHandler(album_titles_list, album_titles_list_full, album_id_list, album_id_list_full, viewAdapter).albumsApi().execute(url)
        } else {
            // если есть, выгружаем данные из кэша
            viewAdapter.notifyDataSetChanged()
        }

        mListener = listener(album_titles_list, album_titles_list_full, album_id_list, album_id_list_full, viewAdapter)
        search.setOnQueryTextListener(mListener)

    }

    override fun onTap(text: String, id: Int) {
        val intent = Intent(this, PhotosActivity::class.java)
        intent.putExtra("albumID", id)
        startActivity(intent)
    }

    override fun onLongTap(text: String, id: Int) {
        ChangeNameDialog(id).show(supportFragmentManager, "ChangeNameDialog")
    }

    override fun DialogHelper(text: String, id: Int) {
        if(list_t_a_id.isEmpty()) {
            // значит кэша нет и мы апдейтим только лист
            album_titles_list[id-1] = text
            album_titles_list_full[id-1] = text
            viewAdapter.notifyDataSetChanged()
        } else {
            // значит кэш есть и мы апдейтим и лист, и базу данных (как раз по list_t_u_id)
            album_titles_list[id] = text
            album_titles_list_full[id] = text
            updateAlbum(this, id, list_t_a_id, album_titles_list)
            viewAdapter.notifyDataSetChanged()
        }
    }

}


