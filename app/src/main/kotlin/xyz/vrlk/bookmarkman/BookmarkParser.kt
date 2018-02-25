package xyz.vrlk.bookmarkman

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.Serializable

interface Item

class Items : Serializable {
    val items: MutableList<Item> = mutableListOf()
}

class Shortcut : Item, Serializable {
    var url: String = ""
    var title: String = ""
    var addDate: String = ""
    var lastVisit: String = ""
    var lastModified: String = ""
    var icon: String = ""
}

class Subfolder : Item, Serializable {
    var title: String = ""
    var addDate: String = ""
    val items: MutableList<Item> = mutableListOf()
}

class BookmarkParseException(message: String? = "") : Exception(message)

class BookmarkParser(reader: BufferedReader) {
    private val xpp: XmlPullParser = {
        val factory = XmlPullParserFactory.newInstance()
        factory.setFeature(Xml.FEATURE_RELAXED, true)
        val xpp = factory.newPullParser()
        xpp.setInput(reader)
        xpp
    }()

    companion object {
        private const val UNINITIALIZED = -9999
    }

    private var current: Int = UNINITIALIZED

    private fun next() {
        if (current == UNINITIALIZED) {
            current = xpp.eventType
        } else {
            while (true) {
                val token = xpp.next()
                // skip empty text entry
                if (token == XmlPullParser.TEXT && xpp.text.trim().isEmpty()) {
                    continue
                } else {
                    current = token
                    break
                }
            }
        }
    }

    private fun expectTrue(expr: () -> Boolean) {
        if (!expr()) {
            throw BookmarkParseException(expr.toString())
        }
    }

    fun parse(): Items? {
        if (!parseHeader()) {
            return null
        }

        return parseItems()
    }

    private fun parseHeader(): Boolean {
        next()
        // start document
        expectTrue({ current == XmlPullParser.START_DOCUMENT })

        // The doctype declaration and comment is simply ignored

        // meta and title
        next()
        if (current == XmlPullParser.START_TAG && xpp.name.toLowerCase() == "meta") {
            // We skip the meta tag for encoding
            // This is not included in the specification but
            // an bookmark file exported from the Google Chrome includes this tag.
            next()
        }
        expectTrue({ current == XmlPullParser.START_TAG && xpp.name.toLowerCase() == "title" })
        next()
        expectTrue({ current == XmlPullParser.TEXT && xpp.text == "Bookmarks" })
        next()
        expectTrue({ current == XmlPullParser.END_TAG && xpp.name.toLowerCase() == "title" })

        // H1
        next()
        expectTrue({ current == XmlPullParser.START_TAG && xpp.name.toLowerCase() == "h1" })
        next()
        expectTrue({ current == XmlPullParser.TEXT && xpp.text == "Bookmarks" })
        next()
        expectTrue({ current == XmlPullParser.END_TAG && xpp.name.toLowerCase() == "h1" })

        return true
    }

    private fun parseItems(): Items? {
        val items = Items()
        next()
        expectTrue({ current == XmlPullParser.START_TAG && xpp.name.toLowerCase() == "dl" })

        while (true) {
            next()
            if (current == XmlPullParser.END_TAG && xpp.name.toLowerCase() == "dl") {
                break
            }
            val item = parseItem()
            if (item == null) {
                break
            } else {
                items.items.add(item)
            }
        }
        next()
        if (current == XmlPullParser.START_TAG && xpp.name.toLowerCase() == "p") {
            // Skip the p tag in the beginning of file
            // (I think) This is not included in the specification but
            // an bookmark file exported from the Google Chrome includes this tag.
            next()
        }
        expectTrue({ current == XmlPullParser.END_DOCUMENT })
        return items
    }

    private fun parseItem(): Item? {
        if (current == XmlPullParser.START_TAG && xpp.name.toLowerCase() == "p") {
            // Skip the p tag in the beginning of file
            // (I think) This is not included in the specification but
            // an bookmark file exported from the Google Chrome includes this tag.
            next()
        }
        expectTrue({ current == XmlPullParser.START_TAG && xpp.name.toLowerCase() == "dt" })
        next()
        if (current == XmlPullParser.START_TAG) {
            if (xpp.name.toLowerCase() == "h3") {
                return parseSubfolder()
            } else if (xpp.name.toLowerCase() == "a") {
                return parseShortcut()
            }
        }
        return null
    }

    private fun parseSubfolder(): Subfolder? {
        val subfolder = Subfolder()
        expectTrue({ current == XmlPullParser.START_TAG && xpp.name.toLowerCase() == "h3" })
        if (xpp.attributeCount > 0) {
            for (i in 0 until xpp.attributeCount) {
                val name = xpp.getAttributeName(i).toLowerCase()
                if (name == "add_date") {
                    subfolder.addDate = xpp.getAttributeValue(i)
                    break
                }
            }
        }

        next()
        expectTrue({ current == XmlPullParser.TEXT })
        subfolder.title = xpp.text

        next()
        expectTrue({ current == XmlPullParser.END_TAG && xpp.name.toLowerCase() == "h3" })

        next()
        expectTrue({ current == XmlPullParser.START_TAG && xpp.name.toLowerCase() == "dl" })

        next()
        expectTrue({ current == XmlPullParser.START_TAG && xpp.name.toLowerCase() == "p" })

        while (true) {
            next()
            if (current == XmlPullParser.END_TAG && xpp.name.toLowerCase() == "dl") {
                break
            }
            val item = parseItem()
            if (item == null) {
                break
            } else {
                subfolder.items.add(item)
            }
        }

        next()
        expectTrue({ current == XmlPullParser.START_TAG && xpp.name.toLowerCase() == "p" })
        return subfolder
    }

    private fun parseShortcut(): Shortcut? {
        val shortcut = Shortcut()
        expectTrue({ current == XmlPullParser.START_TAG && xpp.name.toLowerCase() == "a" })
        if (xpp.attributeCount > 0) {
            for (i in 0 until xpp.attributeCount) {
                val name = xpp.getAttributeName(i).toLowerCase()
                when (name) {
                    "href" -> shortcut.url = xpp.getAttributeValue(i)
                    "add_date" -> shortcut.addDate = xpp.getAttributeValue(i)
                    "last_visit" -> shortcut.lastVisit = xpp.getAttributeValue(i)
                    "last_modified" -> shortcut.lastModified = xpp.getAttributeValue(i)
                    "icon" -> shortcut.icon = xpp.getAttributeValue(i)
                }
            }
        }

        next()
        expectTrue({ current == XmlPullParser.TEXT })
        shortcut.title = xpp.text

        next()
        expectTrue({ current == XmlPullParser.END_TAG && xpp.name.toLowerCase() == "a" })
        return shortcut
    }
}
