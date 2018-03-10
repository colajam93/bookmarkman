package xyz.vrlk.bookmarkman

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {
    companion object {
        private const val PICK_FILE_REQUEST: Int = 1
        const val BOOKMARK_DATA = "TAG_BOOKMARK_DATA"
        const val PREFERENCE_PREVIOUS_URI = "PREFERENCE_PREVIOUS_URI"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener { _ ->
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "text/html"
            startActivityForResult(intent, PICK_FILE_REQUEST)
        }

        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val previousUri = sharedPreferences.getString(PREFERENCE_PREVIOUS_URI, null)
        if (previousUri != null) {
            startBookmarkViewActivity(Uri.parse(previousUri))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri == null) {
                Toast.makeText(this, "The uri was null", Toast.LENGTH_SHORT).show()
                return
            }

            val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(PREFERENCE_PREVIOUS_URI, uri.toString())
            editor.apply()

            startBookmarkViewActivity(uri)
        }

    }

    private fun startBookmarkViewActivity(uri: Uri) {
        val reader = contentResolver.openInputStream(uri).bufferedReader()
        val parser = BookmarkParser(reader)
        try {
            val result = parser.parse()
            val intent = Intent(this, BookmarkViewActivity::class.java)
            intent.putExtra(BOOKMARK_DATA, result)
            startActivity(intent)
        } catch (_: BookmarkParseException) {
            Toast.makeText(this, "Parse failed", Toast.LENGTH_SHORT).show()
        }

    }

}

