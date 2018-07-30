package xyz.vrlk.bookmarkman

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.FileUriExposedException
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_bookmark_view.*
import java.io.InputStream

class BookmarkViewActivity : Activity() {

    private val adapterStack: MutableList<Triple<BookmarkItemAdapter, Int, Int>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_view)

        val uriString = intent.getStringExtra(MainActivity.PREFERENCE_PREVIOUS_URI)
        val uri = Uri.parse(uriString)

        fun openStream(uri: Uri): InputStream? {
            return try {
                contentResolver.openInputStream(uri)
            } catch (e: SecurityException) {
                null
            }
        }

        val stream = openStream(uri)
        if (stream == null) {
            Toast.makeText(this, "Cannot open stream.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val reader = stream.bufferedReader()
        val parser = BookmarkParser(reader)
        var result: MutableList<Item> = mutableListOf()
        try {
            result = parser.parse().items
        } catch (_: BookmarkParseException) {
            Toast.makeText(this, "Parse failed", Toast.LENGTH_SHORT).show()
        }

        // if the bookmark only contains 1 subfolder at top level
        // then expand it
        if (result.size == 1 && result[0] is Subfolder) {
            result = (result[0] as Subfolder).items
        }

        val adapter = BookmarkItemAdapter(this, result)
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
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
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(d.url))
                    startActivity(browserIntent)
                } catch (e: FileUriExposedException) {
                    Toast.makeText(this, "Cannot open \"${d.url}\"", Toast.LENGTH_SHORT).show()
                }
            }
        }
        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, _, position, _ ->
            val parentAdapter = parent.adapter as BookmarkItemAdapter
            val d = parentAdapter.items[position]
            if (d is Shortcut) {
                val clipData = ClipData.newUri(contentResolver, d.title, Uri.parse(d.url))
                val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.primaryClip = clipData
                Toast.makeText(this, "URL has copied to clipboard", Toast.LENGTH_SHORT).show()
                true
            } else {
                false
            }
        }
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
