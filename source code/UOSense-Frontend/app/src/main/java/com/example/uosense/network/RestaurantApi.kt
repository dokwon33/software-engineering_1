import com.example.uosense.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface RestaurantApi {

    // 회원 관리
    @POST("/api/v1/user/signup")
    suspend fun signupUser(
        @Body newUserRequest: NewUserRequest
    ): Response<Boolean>
    // 닉네임 검증
    @POST("/api/v1/user/check-nickname")
    suspend fun checkNickname(
        @Query("nickname") nickname: String
    ): Response<Boolean>

    @PUT("/api/v1/user/signout")
    suspend fun logoutUser(
        @Header("Cookie") refreshToken: String
    ): Response<Unit>

    @GET("/api/v1/user/reissue")
    suspend fun reissueToken(
        @Header("Cookie") refreshToken: String
    ): Response<Unit>

    // 웹메일 인증 관리
    @GET("/api/v1/webmail/check-format")
    suspend fun checkEmail(
        @Query("mailAddress") email: String
    ): Response<Boolean>

    @POST("/api/v1/webmail/verify")
    suspend fun sendAuthCode(
        @Body webmailRequest: WebmailRequest
    ): Response<Boolean>

    @POST("/api/v1/webmail/authenticate-code")
    suspend fun validateCode(
        @Body authCodeRequest: AuthCodeRequest
    ): Response<Boolean>

    // 식당 생성
    @POST("/api/v1/restaurant/create")
    suspend fun createRestaurant(
        @Header("access") token: String,
        @Body restaurantRequest: RestaurantRequest
    ): Response<RestaurantInfo>
    //이미지 생성
    @Multipart
    @POST("/api/v1/restaurant/create/images")
    suspend fun uploadRestaurantImages(
        @Header("access") token: String,
        @Query("restaurantId") restaurantId: Int,
        @Part images: List<MultipartBody.Part>
    ): Response<RestaurantImagesResponse>

    //영업일 생성
    @POST("/api/v1/restaurant/create/businessday")
    suspend fun createBusinessDay(
        @Header("access") token: String,
        @Body businessDayList: BusinessDayList
    ): Response<Unit>
    //메뉴 생성
    @Multipart
    @POST("/api/v1/restaurant/create/menu")
    suspend fun uploadMenu(
        @Header("access") token: String,
        @Query("restaurantId") restaurantId: Int,
        @Query("name") name: String,
        @Query("price") price: Int,
        @Query("description") description: String,
        @Part image: MultipartBody.Part?
    ): Response<Unit>
    // 식당 수정
    @PUT("/api/v1/restaurant/update")
    suspend fun editRestaurant(
        @Header("access") token: String,
        @Body updatedRequest: RestaurantRequest
    ): Response<Unit>
    // 메뉴 수정
    @PUT("/api/v1/restaurant/update/menu")
    suspend fun updateMenu(
        @Header("access") token: String,
        @Body menuRequest: MenuRequest
    ): Response<Unit>
    // 위치 수정
    @PUT("/api/v1/restaurant/update")
    suspend fun updateRestaurantLocation(
        @Header("access") token: String,
        @Body updatedRequest: RestaurantRequest
    ): Response<Unit>
    // 영업일 수정
    @PUT("/api/v1/restaurant/update/businessday")
    suspend fun editBusinessDay(
        @Header("access") token: String,
        @Body businessDayList: BusinessDayList
    ): Response<Unit>

    @DELETE("/api/v1/restaurant/delete")
    suspend fun deleteRestaurant(
        @Header("access") token: String,
        @Query("restaurantId") restaurantId: Int
    ): Response<Unit>

    @DELETE("/api/v1/restaurant/delete/menu")
    suspend fun deleteMenu(
        @Header("access") token: String,
        @Query("menuId") menuId: Int
    ): Response<Unit>

    @DELETE("/api/v1/restaurant/delete/businessday")
    suspend fun deleteBusinessDay(
        @Header("access") token: String,
        @Query("businessDayId") businessDayId: Int
    ): Response<Unit>

    // 검색
    @GET("/api/v1/search")
    suspend fun searchRestaurants(
        @Query("keyword") keyword: String,
        @Query("doorType") doorType: String? = null
    ): Response<List<RestaurantListResponse>>

    @GET("/api/v1/search/sort")
    suspend fun sortRestaurants(
        @Query("keyword") keyword: String,
        @Query("filter") filter: String
    ): Response<List<RestaurantListResponse>>


    @DELETE("/api/v1/review/delete")
    suspend fun deleteReview(
        @Header("access") token: String,
        @Query("reviewId") reviewId: Int
    ): Response<Unit>


    @GET("api/v1/restaurant/get/menu")
    suspend fun getMenu(
        @Query("restaurantId") restaurantId: Int
    ): Response<List<MenuResponse>>

    @GET("/api/v1/restaurant/get/businessday")
    suspend fun getBusinessDayList(
        @Query("restaurantId") restaurantId: Int
    ): Response<BusinessDayList>

    // 특정 식당 정보 조회
    @GET("/api/v1/restaurant/get")
    suspend fun getRestaurantById(
        @Query("restaurantId") restaurantId: Int
    ): Response<RestaurantInfo>

    // 식당 리스트 조회
    @GET("/api/v1/restaurant/get/list")
    suspend fun getRestaurantList(
        @Query("doorType") doorType: String? = null,
        @Query("filter") filter: String = "DEFAULT"
    ): Response<List<RestaurantListResponse>>


    @GET("/api/v1/restaurant/get/images")
    suspend fun getRestaurantImages(
        @Query("restaurantId") restaurantId: Int
    ): Response<RestaurantImagesResponse>


    // 특정 사용자 리뷰 조회
    @GET("/api/v1/review/get/user")
    suspend fun getUserReviews(
        @Query("userId") userId: Int
    ): List<ReviewResponse>

    // 즐겨찾기 관리
    @POST("/api/v1/bookmark/create")
    suspend fun addBookmark(
        @Header("access") accessToken: String,
        @Query("restaurantId") restaurantId: Int
    ): Response<Unit>

    @DELETE("/api/v1/bookmark/delete")
    suspend fun deleteBookmark(
        @Header("access") accessToken: String,
        @Query("restaurantId") restaurantId: Int
    ): Response<Unit>

    @GET("/api/v1/bookmark/get/user")
    suspend fun getUserBookmarks(
        @Query("userId") userId: Int
    ): Response<List<BookMarkResponse>>

    // 즐겨찾기 조회 (사용자 기준)
    @GET("/api/v1/bookmark/get/mine")
    suspend fun getMyBookmarks(
        @Header("access") accessToken: String // access 헤더 추가
    ): List<BookMarkResponse>

    // 리뷰 등록
    @POST("/api/v1/review/create")
    suspend fun createReview(
        @Body reviewRequest: ReviewRequest,
        @Header("access") accessToken: String
    ): Response<Int>
    //  리뷰 이미지 등록
    @Multipart
    @POST("/api/v1/review/create/images")
    suspend fun uploadReviewImages(
        @Query("reviewId") reviewId: Int, // 리뷰 ID
        @Part images: List<MultipartBody.Part> // 업로드할 이미지 파일 배열
    ): Response<Unit>

    // 리뷰 삭제
    @DELETE("/api/v1/review/delete")
    suspend fun deleteReview(
        @Query("reviewId") reviewId: Int,
    ): Response<Unit>

    // 리뷰 목록 조회 (식당 기준)
    @GET("/api/v1/review/get/list")
    suspend fun getRestaurantReviews(
        @Query("restaurantId") restaurantId: Int
    ): Response<List<ReviewItem>>

    // 자신 리뷰 조회
    @GET("/api/v1/review/get/mine")
    suspend fun getMyReviews(
        @Header("access") accessToken: String
    ): List<ReviewItem>

    // 특정 리뷰 조회
    @GET("/api/v1/review/get")
    suspend fun getReviewById(
        @Query("reviewId") reviewId: Int
    ): ReviewResponse

    // 리뷰 좋아요 추가
    @PATCH("/api/v1/review/like")
    suspend fun likeReview(
        @Query("reviewId") reviewId: Int,
        @Header("access") accessToken: String // access 헤더 추가
    ): Response<Unit>

    // 리뷰 신고
    @POST("/api/v1/report/create/review")
    suspend fun reportReview(
        @Body reportRequest: ReportRequest,
        @Header("access") accessToken: String // access 헤더 추가
    ): Response<Unit>

    // 모든 신고 내역 조회
    @GET("/api/v1/report/get/list")
    suspend fun getReports(
        @Header("access") accessToken: String
    ): List<ReportResponse>

    // 유저 프로필 조회
    @PUT("/api/v1/user/get")
    suspend fun getUserProfile(
        @Header("access") accessToken: String // access 헤더 추가
    ): UserProfileResponse

    // 유저 마이페이지 정보 수정
    @Multipart
    @PUT("/api/v1/user/update")
    suspend fun updateUserProfile(
        @Header("access") accessToken: String,
        @Query("nickname") nickname: String?,
        @Part image: MultipartBody.Part?
    ): Response<Unit>

    // 식당 상품 메뉴 정보 제안
    @POST("/api/v1/purpose/create/menu")
    suspend fun createMenu(
        @Header("access") accessToken: String,
        @Query("restaurantId") restaurantId: Int,
        @Query("name") name: String,
        @Query("price") price: Int,
        @Body image : MenuImageRequest?
    ): Response<Unit>

    // 식당 영업 시간 정보 제안
    @POST("/api/v1/purpose/create/businessday")
    suspend fun createBusinessDay(
        @Header("access") accessToken: String,
        @Body businessDayRequest: BusinessDayRequest
    ): Response<Unit>

    // 식당 기본 수정 제안 통합
    @POST("/api/v1/purpose/create/restaurant")
    suspend fun createBasicInfo(
        @Header("access") accessToken: String,
        @Body restaurantBasicInfo: RestaurantBasicInfo
    ): Response<Unit>

    // 로그인 API 호출 정의 (RestaurantApi.kt에 추가 필요)
    @POST("/api/v1/user/signin")
    suspend fun loginUser(
        @Body loginRequest: LoginRequest
    ): Response<Unit>
}
