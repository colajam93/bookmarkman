package xyz.vrlk.bookmarkman

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.bookmark_item_view.view.*

private class ViewHolder(var imageView: ImageView, var textView: TextView)

class BookmarkItemAdapter(private val context: Context, val items: List<Item>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var v = convertView
        var holder: ViewHolder? = null

        v?.let {
            holder = it.tag as ViewHolder?
        } ?: run {
            val view = View.inflate(context, R.layout.bookmark_item_view, null)
            holder = ViewHolder(view.imageView, view.textView)
            view.tag = holder
            v = view
        }

        holder?.let {
            val data = items[position]
            setupView(it, data)
        }

        return v as View
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

    private fun setupView(view: ViewHolder, data: Item) {
        if (data is Shortcut) {
            view.textView.text = data.title
            if (data.icon.isNotEmpty()) {
                val regex = Regex("""data:(.*);base64,(.*)""")
                val match = regex.matchEntire(data.icon)
                val mime = match?.groupValues?.get(1) ?: ""
                val raw = match?.groupValues?.get(2) ?: ""
                if (mime == "image/png" && raw.isNotEmpty()) {
                    val bytes = Base64.decode(raw, Base64.DEFAULT)
                    val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (image == null) {
                        Toast.makeText(context, "Bitmap decode failed.", Toast.LENGTH_SHORT).show()
                    }
                    view.imageView.setImageBitmap(image)
                    view.imageView.colorFilter = null
                }
            } else {
                view.imageView.setImageResource(R.drawable.ic_insert_drive_file_black_24dp)
                view.imageView.setColorFilter(context.getColor(R.color.colorGray))
            }
        } else if (data is Subfolder) {
            view.textView.text = data.title
            view.imageView.setImageResource(R.drawable.ic_folder_black_24dp)
            view.imageView.setColorFilter(context.getColor(R.color.colorGray))
        }
    }
}
