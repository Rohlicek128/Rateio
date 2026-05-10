package com.example.rateio.data.remote.steam

import com.example.rateio.data.remote.TmdbImage
import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.components.CarouselImage
import com.google.gson.annotations.SerializedName


data class SteamGameSummary(
    @SerializedName("appid") val appid: String,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("logo") val logo: String?,
)

data class SteamGameDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: SteamGameDetail,
)

data class SteamGameDetail(
    @SerializedName("steam_appid") val steamAppid: Int,
    @SerializedName("type") val type: String?,

    @SerializedName("name") val name: String,
    @SerializedName("release_date") val releaseDate: SteamReleaseDate,
    @SerializedName("short_description") val shortDescription: String?,
    @SerializedName("genres") val genres: List<SteamGenres>,

    @SerializedName("developers") val developers: List<String>,
    @SerializedName("publishers") val publishers: List<String>,

    @SerializedName("header_image") val headerImage: String?,
    @SerializedName("screenshots") val screenshots: List<SteamGameScreenshot>,

    @SerializedName("price_overview") val priceOverview: SteamGamePrice?,
)


data class SteamGamePrice(
    @SerializedName("initial") val initial: Int,
    @SerializedName("final") val final: Int,
    @SerializedName("discount_percent") val discountPercent: Int,
    @SerializedName("currency") val currency: String?,
    @SerializedName("initial_formatted") val initialFormatted: String?,
    @SerializedName("final_formatted") val finalFormatted: String?,
)

data class SteamGameScreenshot(
    @SerializedName("id") val id: Int,
    @SerializedName("path_thumbnail") val pathThumbnail: String?,
    @SerializedName("path_full") val pathFull: String?,
)

data class SteamGameReviews(
    @SerializedName("success") val success: Int,
    @SerializedName("query_summary") val querySummary: SteamGameReviewsSummary?,
)

data class SteamGameReviewsSummary(
    @SerializedName("total_positive") val totalPositive: Int,
    @SerializedName("total_negative") val totalNegative: Int,
    @SerializedName("total_reviews") val totalReviews: Int,
    @SerializedName("review_score_desc") val reviewScoreDesc: String,
) {
    val normalizedRating: Float? get() = if (totalReviews >= 100) totalPositive.toFloat() / totalReviews.toFloat() else null
}


data class SteamGenres(
    @SerializedName("id") val id: String,
    @SerializedName("description") val description: String,
)

data class SteamReleaseDate(
    @SerializedName("coming_soon") val comingSoon: Boolean,
    @SerializedName("date") val date: String,
)


fun SteamGameSummary.toRateItem(categoryId: Long = 0) = RateItem(
    id = 0,
    categoryId = categoryId,
    title = name,
    subtitle = null,
    coverImageUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/${appid}/library_600x900_2x.jpg",
    coverImageLowUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/${appid}/library_600x900.jpg", //"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/${appid}/capsule_616x353.jpg",
    externalId = appid,
    externalSource = CategoryType.STEAM_GAMES,
)

fun SteamGameScreenshot.toCarouselImage() = CarouselImage(
    filePath = pathFull ?: "",
    aspectRatio = 16f / 9f,
)