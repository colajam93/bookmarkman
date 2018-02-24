package xyz.vrlk.bookmarkman

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.bookmark_item_view.view.textView

class BookmarkItemAdapter(private val context: Context, val items: List<Item>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = View.inflate(context, R.layout.bookmark_item_view, null)
        val data = items[position]
        if (data is Shortcut) {
            view.textView.text = data.title
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
