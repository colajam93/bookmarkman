package xyz.vrlk.bookmarkman

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_bookmark_view.*

class BookmarkViewActivity : Activity() {

    private val adapterStack: MutableList<Triple<BookmarkItemAdapter, Int, Int>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_view)

        val uriString = intent.getStringExtra(MainActivity.PREFERENCE_PREVIOUS_URI)
        val uri = Uri.parse(uriString)
        val reader = contentResolver.openInputStream(uri).bufferedReader()
        val parser = BookmarkParser(reader)
        var result = Items()
        try {
            result = parser.parse()
        } catch (_: BookmarkParseException) {
            Toast.makeText(this, "Parse failed", Toast.LENGTH_SHORT).show()
        }


        val adapter = BookmarkItemAdapter(this, result.items)
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener(
                { parent, _, position, _ ->
                    val parentAdapter = parent.adapter as BookmarkItemAdapter
                    val d = parentAdapter.items[position]
                    if (d is Subfolder) {
                        // save current ListView index and scroll offset
                        val index = listView.firstVisiblePosition
                        val v = listView.getChildAt(0) as View
                        val top = v.top - listView.paddingTop
                        adapterStack.add(Triple(listView.adapter as BookmarkItemAdapter, index, top))
                        val newAdapter = BookmarkItemAdapter(this, d.items)
                        listView.adapter = newAdapter
                    } else if (d is Shortcut) {
                        // Open shortcut
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(d.url))
                        startActivity(browserIntent)
                    }
                })
    }

    override fun onBackPressed() {
        if (adapterStack.isNotEmpty()) {
            // Restore previous adapter
            val previousAdapter = adapterStack.last()
            adapterStack.removeAt(adapterStack.size - 1)
            listView.adapter = previousAdapter.first
            listView.setSelectionFromTop(previousAdapter.second, previousAdapter.third)
        } else {
            super.onBackPressed()
        }
    }
}
