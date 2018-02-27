package xyz.vrlk.bookmarkman

import android.app.Activity
import android.os.Bundle
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_bookmark_view.*

class BookmarkViewActivity : Activity() {

    private val adapterStack: MutableList<BookmarkItemAdapter> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_view)

        val bookmarkData = intent.getSerializableExtra(MainActivity.BOOKMARK_DATA) as Items
        val adapter = BookmarkItemAdapter(this, bookmarkData.items)
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener(
                { parent, _, position, _ ->
                    val parentAdapter = parent.adapter as BookmarkItemAdapter
                    val d = parentAdapter.items[position]
                    if (d is Subfolder) {
                        adapterStack.add(listView.adapter as BookmarkItemAdapter)
                        val newAdapter = BookmarkItemAdapter(this, d.items)
                        listView.adapter = newAdapter
                    }
                })
    }

    override fun onBackPressed() {
        if (adapterStack.isNotEmpty()) {
            val previousAdapter = adapterStack.last()
            adapterStack.removeAt(adapterStack.size - 1)
            listView.adapter = previousAdapter
        } else {
            super.onBackPressed()
        }
    }
}
