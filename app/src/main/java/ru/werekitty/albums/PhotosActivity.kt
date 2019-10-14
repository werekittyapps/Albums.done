package ru.werekitty.albums

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideType
import kotlinx.android.synthetic.main.activity_photos.*

class PhotosActivity : AppCompatActivity() {

    var photos_titles_list : MutableList<String> = mutableListOf()
    var photos_id_list : MutableList<Int> = mutableListOf()
    var photos_full_URL_list : MutableList<String> = mutableListOf()
    var photos_URL_list : MutableList<String> = mutableListOf()
    var list_t_p_id: MutableList<Int> = mutableListOf()
    var list_album: MutableList<Int> = mutableListOf()


    private lateinit var viewAdapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photos)

        viewAdapter = ImagesAdapter(photos_URL_list)
        recyclerImageHelper(this, PhotosListView, viewAdapter)

        val albumID = intent.getIntExtra("albumID", -1)

        loadPhotoList(this, albumID, list_t_p_id, list_album, photos_id_list, photos_titles_list, photos_full_URL_list)
        if (photos_titles_list.isEmpty()) {
            // если нет кэша, делаем запросы и сохраняем
            val url = "https://jsonplaceholder.typicode.com/photos?albumId=" + albumID
            photosHandler(photos_titles_list, photos_id_list, photos_full_URL_list, photos_URL_list, viewAdapter).photosApi().execute(url)
        } else {
            // если есть, выгружаем данные из кэша
            for (i in 0 until 10){
                photos_URL_list.add(photos_full_URL_list[i])
            }
            viewAdapter.notifyDataSetChanged()
        }
    }

    fun more(view: View){
        if(photos_full_URL_list.size - photos_URL_list.size >= 10){
            val index = photos_URL_list.size
            for (i in 0 until 10) {
                photos_URL_list.add(photos_full_URL_list[index+i])
            }
            viewAdapter.notifyDataSetChanged()
            if(photos_full_URL_list.size - photos_URL_list.size < 10){
                moreBtn.isEnabled = false
                moreBtn.alpha = 0.0F
            }
        }
    }
}
