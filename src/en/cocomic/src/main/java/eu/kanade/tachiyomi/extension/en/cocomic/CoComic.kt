package eu.kanade.tachiyomi.extension.en.cocomic

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Locale

class CoComic : Madara(
    "CoComic",
    "https://cocomic.co",
    "en",
    SimpleDateFormat("MMMM d, yyyy", Locale.US),
) {
    override val id: Long = 4673821938475629301L
    override val mangaSubString = "manga"
    override val useNewChapterEndpoint = true
    override val genreUrlContains = "manga-genre"
    override val filterNonMangaItems = false

    override fun popularMangaRequest(page: Int): Request =
        GET("$baseUrl/manga/?m_orderby=trending&page=$page", headers)

    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl/manga/?m_orderby=latest&page=$page", headers)

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query.isNotBlank()) {
            return GET("$baseUrl/?s=$query&post_type=wp-manga&page=$page", headers)
        }
        var url = "$baseUrl/manga/"
        val params = mutableListOf("page=$page")
        filters.forEach { filter ->
            when (filter) {
                is GenreFilter -> if (filter.state != 0) url = "$baseUrl/manga-genre/${filter.toUriPart()}/"
                is OrderByFilter -> params.add("m_orderby=${filter.toUriPart()}")
                else -> {}
            }
        }
        return GET("$url?${params.joinToString("&")}", headers)
    }

    override fun getFilterList() = FilterList(
        Filter.Header("Filters ignored with text search"),
        Filter.Separator(),
        GenreFilter(),
        OrderByFilter(),
    )

    private class GenreFilter : UriPartFilter("Genre", arrayOf(
        Pair("<All>", ""), Pair("Yaoi / BL", "yaoibl"), Pair("Manhwa", "manhwa"),
        Pair("Manga", "manga"), Pair("Manhua", "manhua"), Pair("Drama", "drama"),
        Pair("Romance", "romance"), Pair("Smut", "smut"), Pair("Fantasy", "fantasy"),
        Pair("Full Color", "full-color"), Pair("Slice of Life", "slice-of-life"),
        Pair("Action", "action"), Pair("Comedy", "comedy"), Pair("Mature", "mature"),
        Pair("Webtoon", "webtoon"), Pair("Sci-fi", "sci-fi"),
        Pair("Supernatural", "supernatural"), Pair("Sports", "sports"),
        Pair("Medical", "medical"), Pair("Web Comic", "web-comic"),
    ))

    private class OrderByFilter : UriPartFilter("Order By", arrayOf(
        Pair("Latest", "latest"), Pair("Trending", "trending"),
        Pair("Most Viewed", "views"), Pair("New Manga", "new-manga"),
        Pair("Rating", "rating"), Pair("A-Z", "alphabet"),
    ))

    open class UriPartFilter(displayName: String, private val vals: Array<Pair<String, String>>) :
        Filter.Select<String>(displayName, vals.map { it.first }.toTypedArray()) {
        fun toUriPart() = vals[state].second
    }
}
