package ru.werekitty.albums

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ListAdapter.TapAction, ListAdapter.LongTapAction {

    var user_names_list : MutableList<String> = mutableListOf()
    var user_id_list : MutableList<Int> = mutableListOf()
    private lateinit var viewAdapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewAdapter = ListAdapter(user_names_list, user_id_list, this)
        recyclerHelper(this, PhotographsListView, viewAdapter, user_names_list)

        val url = "https://jsonplaceholder.typicode.com/users"
        photographsHandler(user_names_list, user_id_list, viewAdapter).photographsApi().execute(url)
    }

    override fun onTap(text: String, id: Int) {
        val intent = Intent(this, AlbumsActivity::class.java)
        intent.putExtra("userID", id)
        startActivity(intent)
    }

    override fun onLongTap(text: String, id: Int) {
        //
    }

}
