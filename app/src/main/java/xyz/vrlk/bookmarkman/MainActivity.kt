package xyz.vrlk.bookmarkman

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

const val PICK_FILE_REQUEST: Int = 1


class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener { _ ->
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "text/html"
            startActivityForResult(intent, PICK_FILE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST) {
            if (resultCode == RESULT_OK) {
                val r = data?.data
                if (r != null) {
                    val reader = contentResolver.openInputStream(r).bufferedReader()
                    val parser = BookmarkParser(reader)
                    val result = parser.parse()
                    Log.d("", result.toString())
                }
            }
        }
    }

}

