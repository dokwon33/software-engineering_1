package com.example.uosense

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.example.uosense.models.CategoryType
import com.example.uosense.models.DoorType
import com.example.uosense.models.FilterType
import com.example.uosense.models.ReportDetailType
import com.example.uosense.models.SubDescriptionType
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * MainActivity에서 쓰는 일부 함수 리팩토링하기 위해서
 * Coroutine 쓰는 것은 여기에서 정의 금지
 */
object AppUtils {
    

    lateinit var naverMap: NaverMap
    var userMarker: Marker? = null
    val restaurantMarkers = mutableListOf<Marker>()
    lateinit var locationSource: FusedLocationSource

    // 문 좌표 정의
    private val doorCoordinates = mapOf(
        "FRONT" to LatLng(37.5834643, 127.0536246),  // 정문
        "SIDE" to LatLng(37.5869791, 127.0564010),   // 쪽문
        "BACK" to LatLng(37.5869320, 127.0606581),   // 후문
        "SOUTH" to LatLng(37.5775540, 127.0578147)   // 남문
    )


    /**
     * 현재 위치 마커 업데이트
     */
    fun updateUserLocationMarker(latitude: Double, longitude: Double) {
        if (userMarker == null) {
            userMarker = Marker().apply {
                position = LatLng(latitude, longitude)
                icon = OverlayImage.fromResource(R.drawable.ddong_playstore)
                width = 120
                height = 120
                captionText = "현재 위치"
                map = naverMap
            }
            Log.d("USER_MARKER", "사용자 마커 생성됨: ($latitude, $longitude)")
        } else {
            userMarker?.position = LatLng(latitude, longitude)
            Log.d("USER_MARKER", "사용자 마커 업데이트됨: ($latitude, $longitude)")
        }
        moveCameraToLocation(latitude, longitude)
    }

    /**
     * 카메라를 특정 위치로 이동
     */
    fun moveCameraToLocation(latitude: Double, longitude: Double) {
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
        naverMap.moveCamera(cameraUpdate)
        Log.d("CAMERA_MOVE", "카메라 이동됨: ($latitude, $longitude)")
    }

    /**
     * 카메라를 모든 마커가 보이도록 이동
     */
    fun moveCameraToFitAllMarkers() {
        if (restaurantMarkers.isNotEmpty()) {
            val bounds = LatLngBounds.Builder().apply {
                restaurantMarkers.forEach { include(it.position) }
                userMarker?.let { include(it.position) }
            }.build()

            val cameraUpdate = CameraUpdate.fitBounds(bounds, 100)
            naverMap.moveCamera(cameraUpdate)
        }
    }

    /**
     * 가장 가까운 문을 계산하여 반환
     */
    fun getClosestDoorType(userLat: Double, userLon: Double): String? {
        val doorLocations = mapOf(
            "정문" to LatLng(37.5834643, 127.0536246),
            "쪽문" to LatLng(37.5869791, 127.0564010),
            "후문" to LatLng(37.5869320, 127.0606581),
            "남문" to LatLng(37.5775540, 127.0578147)
        )

        val closestDoor = doorLocations.minByOrNull { (_, location) ->
            distanceBetween(userLat, userLon, location.latitude, location.longitude)
        }

        return closestDoor?.key
    }



    /**
     * 두 좌표 간 거리 계산
     */
    fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val result = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return result[0]
    }

    /**
     * Toast 메시지 출력
     * 되도록이면 사용 X
     * 만약 사용시 Coroutine 내에서는 this@특정액티비티, 아닐때는 this만 사용
     * duration은 Toast.LENGTH_SHORT 또는 Toast.LENGTH_LONG만 사용
     */
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    // 도로명 주소 -> 위도, 경도 변환
    fun getLatLngFromAddress(address: String, callback: (Double?, Double?) -> Unit) {
        val client = OkHttpClient()
        val url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=${address}"
        println(url)
        // ID, KEY 절대 수정 X , SECRET 사용 가능
        val request = Request.Builder()
            .url(url)
            .addHeader("X-NCP-APIGW-API-KEY-ID", "s78aa7asq0")
            .addHeader("X-NCP-APIGW-API-KEY", "WGmu5zQXqTGWOy7Bj9PWwrD8HeQezlBvZ675Q24K")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody ?: "")
                val addresses = json.getJSONArray("addresses")

                if (addresses.length() > 0) {
                    val location = addresses.getJSONObject(0)
                    val latitude = location.getDouble("y")
                    val longitude = location.getDouble("x")

                    withContext(Dispatchers.Main) {
                        callback(latitude, longitude)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(null, null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(null, null)
                }
            }
        }
    }
    // doorType을 API에서 사용하는 값으로 매핑하는 함수
    fun mapDoorTypeForApi(doorType: String): String {
        return when (doorType) {
            "정문" -> "FRONT"
            "남문" -> "SOUTH"
            "쪽문" -> "SIDE"
            "후문" -> "BACK"
            else -> "DEFAULT"  // 기본값 처리
        }
    }

    // CategoryType 매핑 함수
    fun mapCategoryForApi(category: String): String {
        return when (category) {
            "한식" -> "KOREAN"
            "중식" -> "CHINESE"
            "일식" -> "JAPANESE"
            "양식" -> "WESTERN"
            "기타" -> "OTHER"
            else -> "OTHER"
        }
    }

    // SubDescriptionType 매핑 함수
    fun mapSubDescription(subDescription: String): String {
        return when (subDescription) {
            "술집" -> "BAR"
            "카페" -> "CAFE"
            "음식점" -> "RESTAURANT"
            else -> "RESTAURANT"
        }
    }

    // DayOfWeek 매핑 함수
    fun mapDayOfWeek(day: String): String {
        return when (day) {
            "월요일" -> "Monday"
            "화요일" -> "Tuesday"
            "수요일" -> "Wednesday"
            "목요일" -> "Thursday"
            "금요일" -> "Friday"
            "토요일" -> "Saturday"
            "일요일" -> "Sunday"
            else -> throw IllegalArgumentException("알 수 없는 요일: $day")
        }
    }


    // FilterType 매핑 함수
    fun mapFilterType(filter: String): String {
        return when (filter) {
            "즐겨찾기 많은 순" -> "BOOKMARK"
            "거리 가까운 순" -> "DISTANCE"
            "평점 순" -> "RATING"
            "리뷰 많은 순" -> "REVIEW"
            "가격 낮은 순" -> "PRICE"
            else -> "BOOKMARK"
        }
    }


}
