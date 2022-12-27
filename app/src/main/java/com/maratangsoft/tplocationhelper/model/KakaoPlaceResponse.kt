package com.maratangsoft.tplocationhelper.model

data class KakaoPlaceResponse(
    val meta: PlaceMeta,
    val documents: MutableList<Place>
)

data class PlaceMeta(
    val total_count: Int,
    val pageable_count: Int,
    val is_end: Boolean
)

data class Place(
    val id: String,
    val place_name: String,
    val category_name: String,
    val phone: String,
    val address_name: String,
    val road_address_name: String,
    val x: String, //경도, longitude
    val y: String, //위도, latitude
    val place_url: String,
    var distance: String //요청파라미터로 x,y를 줬을 때만 나옴
)