package com.example.rateio.data.remote.openlibrary

import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


data class OLWorksSearchResponse(
    @SerializedName("numFound") val numFound: Int,
    @SerializedName("docs") val docs: List<OLWork>,
)

data class OLWork(
    @SerializedName("key") val key: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("author_key") val authorKey: List<String>?,
    @SerializedName("author_name") val authorName: List<String>?,

    @SerializedName("cover_edition_key") val coverEditionKey: String?,
    @SerializedName("cover_i") val coverI: Int?,

    @SerializedName("first_publish_year") val firstPublishYear: Int?,
)

data class OLWorkDetail(
    @SerializedName("key") val key: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: OLTypeValue?,
    @SerializedName("authors") val authors: List<OLWorkAuthor>?,
    @SerializedName("covers") val covers: List<Int>?,

    @SerializedName("subjects") val subjects: List<String>?,
    @SerializedName("subject_places") val subjectPlaces: List<String>?,
    @SerializedName("subject_people") val subjectPeople: List<String>?,
)

data class OLWorkAuthor(
    @SerializedName("author") val author: OLKeyString?,
    @SerializedName("type") val type: OLKeyString?,
)
data class OLKeyString(
    @SerializedName("key") val key: String?,
)
data class OLTypeValue(
    @SerializedName("type") val type: String?,
    @SerializedName("value") val value: String?,
)

data class OLWorksEditionsResponse(
    @SerializedName("size") val size: Int?,
    @SerializedName("entries") val entries: List<OLWorkEdition>?,
)

data class OLWorkEdition(
    @SerializedName("key") val key: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("covers") val covers: List<Int>?,

    @SerializedName("description") val description: OLTypeValue?,
    @SerializedName("languages") val languages: List<OLKeyString>?,

    @SerializedName("publish_date") val publishDate: String?,
    @SerializedName("publish_places") val publishPlaces: List<String>?,
    @SerializedName("publishers") val publishers: List<String>?,

    @SerializedName("number_of_pages") val numberOfPages: Int?,
    @SerializedName("table_of_contents") val tableOfContents: List<OLTableContent>?,
)
data class OLTableContent(
    @SerializedName("level") val level: Int?,
    @SerializedName("label") val label: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("pagenum") val pageNum: String?,
)


data class OLAuthorDetail(
    @SerializedName("key") val key: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("bio") val bio: OLTypeValue?,
    @SerializedName("birth_date") val birthDate: String?,
    @SerializedName("photos") val photos: List<Int>?,
)



@Serializable
data class OLWorkMetadata(
    val numberOfChaptersByPartsAPI: List<Int>? = null,
    val numberOfChaptersByParts: List<Int>? = null,
    val numberOfPages: Int? = null,
    val completedPages: Int = 0,
)


fun OLWork.toRateItem(categoryId: Long = 0) = RateItem(
    id = 0,
    categoryId = categoryId,
    title = title ?: "Unknown",
    subtitle = if (!authorName.isNullOrEmpty()) authorName.first() else firstPublishYear?.toString() ?: "N/A",
    coverImageUrl = coverI?.let { OpenLibraryClient.COVERS_BASE_URL + "/ID/${it}-L.jpg" },
    coverImageLowUrl = coverI?.let { OpenLibraryClient.COVERS_BASE_URL + "/ID/${it}-M.jpg" },
    externalId = key,
    externalSource = CategoryType.OPEN_LIBRARY_BOOKS,
)

fun OLWorkDetail.toRateItem(categoryId: Long = 0, subtitle: String? = null) = RateItem(
    id = 0,
    categoryId = categoryId,
    title = title ?: "Unknown",
    subtitle = subtitle,
    coverImageUrl = covers?.takeIf { it.isNotEmpty() }?.let {
        OpenLibraryClient.COVERS_BASE_URL + "/ID/${it.first()}-L.jpg"
    },
    coverImageLowUrl = covers?.takeIf { it.isNotEmpty() }?.let {
        OpenLibraryClient.COVERS_BASE_URL + "/ID/${it.first()}-M.jpg"
    },
    externalId = key,
    externalSource = CategoryType.OPEN_LIBRARY_BOOKS,
)


fun OLTableContent.toRateItem(categoryId: Long = 0, bookName: String = "N/A", index: Int? = null) = RateItem(
    id = 0,
    categoryId = categoryId,
    title = when {
        !title.isNullOrBlank() -> title
        !label.isNullOrBlank() -> label
        index != null -> "Chapter $index"
        else -> "N/A"
    },
    subtitle = when {
        !title.isNullOrBlank() && !label.isNullOrBlank() -> "$label  |  $bookName"
        else -> bookName
    },
    coverImageUrl = null,
    coverImageLowUrl = null,
    externalId = null,
    externalSource = CategoryType.OPEN_LIBRARY_CHAPTER,
)