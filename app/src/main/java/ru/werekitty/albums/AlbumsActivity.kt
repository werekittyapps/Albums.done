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

class AlbumsActivity : AppCompatActivity(), ListAdapter.TapAction, ListAdapter.LongTapAction {

    var album_titles_list : MutableList<String> = mutableListOf()
    var album_titles_list_full : MutableList<String> = mutableListOf()
    var album_id_list : MutableList<Int> = mutableListOf()
    var album_id_list_full : MutableList<Int> = mutableListOf()
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var mListener: listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_albums)

        viewAdapter = ListAdapter(album_titles_list, album_id_list, this)
        recyclerHelper(this, AlbumsListView, viewAdapter, album_titles_list)

        val userID = intent.getIntExtra("userID", -1)

        val url = "https://jsonplaceholder.typicode.com/albums/?userId=" + userID
        albumsHandler(album_titles_list, album_titles_list_full, album_id_list, album_id_list_full, viewAdapter).albumsApi().execute(url)

        mListener = listener(album_titles_list, album_titles_list_full, album_id_list, album_id_list_full, viewAdapter)
        search.setOnQueryTextListener(mListener)

    }

    override fun onTap(text: String, id: Int) {
        val intent = Intent(this, PhotosActivity::class.java)
        intent.putExtra("albumID", id)
        startActivity(intent)
    }

    override fun onLongTap(text: String, id: Int) {
        //
    }

}


