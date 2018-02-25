package xyz.vrlk.bookmarkman

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.bookmark_item_view.view.*

class BookmarkItemAdapter(private val context: Context, val items: List<Item>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = View.inflate(context, R.layout.bookmark_item_view, null)
        val data = items[position]
        if (data is Shortcut) {
            view.textView.text = data.title
            if (!data.icon.isEmpty()) {
                val regex = Regex("""data:(.*);base64,(.*)""")
                val match = regex.matchEntire(data.icon)
                val mime = match?.groupValues?.get(1) ?: ""
                val raw = match?.groupValues?.get(2) ?: ""
                if (mime == "image/png" && raw.isNotEmpty()) {
                    val bytes = Base64.decode(raw, Base64.DEFAULT)
                    val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    view.imageView.setImageBitmap(image)
                }
            }
        } else if (data is Subfolder) {
            view.textView.text = data.title
        }
        return view
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }
}
