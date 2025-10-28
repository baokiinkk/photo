# Mock API Implementation

## Tổng quan
Đã cập nhật hệ thống mock API để hoạt động như API thật, sử dụng Retrofit và MockInterceptor.

## Cấu trúc Mock API

### 1. MockInterceptor (`base/src/main/java/com/basesource/base/network/mock/MockInterceptor.kt`)
- **Chức năng**: Intercept HTTP requests và trả về mock data từ JSON files
- **Cách hoạt động**:
  - Kiểm tra URL endpoint
  - Load JSON data từ assets folder
  - Trả về Response với mock data
  - Nếu không match endpoint nào, forward request đến server thật

### 2. API Endpoints
```kotlin
// Mock endpoints
const val MOCK_HOME_API = "mock/home"
const val MOCK_TRENDING_API = "mock/trending"

// JSON files
const val MOCK_FILE_HOME_JSON = "home_mock.json"
const val MOCK_FILE_TRENDING_JSON = "trending_mock.json"
```

### 3. TrendingApiService (`base/src/main/java/com/basesource/base/network/TrendingApiService.kt`)
```kotlin
interface TrendingApiService {
    @GET("mock/trending")
    suspend fun getTrendingItems(): Response<ResponseBody>
}
```

### 4. Data Models (`base/src/main/java/com/basesource/base/data/model/TrendingItem.kt`)
- Di chuyển từ app module xuống base module
- Sử dụng Kotlinx Serialization
- Có thể sử dụng chung cho nhiều module

## Cách hoạt động

### 1. Request Flow
```
TrendingViewModel -> TrendingRepository -> TrendingApiService -> MockInterceptor -> JSON File
```

### 2. Response Flow
```
JSON File -> MockInterceptor -> TrendingApiService -> TrendingRepository -> TrendingViewModel -> UI
```

### 3. MockInterceptor Logic
```kotlin
override fun intercept(chain: Interceptor.Chain): Response {
    val uri = chain.request().url.toUri().toString()
    val responseString = when {
        uri.endsWith(MOCK_HOME_API) -> getJsonStringFromFile(context.assets, MOCK_FILE_HOME_JSON)
        uri.endsWith(MOCK_TRENDING_API) -> getJsonStringFromFile(context.assets, MOCK_FILE_TRENDING_JSON)
        else -> ""
    }
    
    return if (responseString.isNotEmpty()) {
        // Trả về mock response
        Response.Builder()
            .code(HTTP_OK)
            .protocol(Protocol.HTTP_2)
            .message("OK")
            .request(chain.request())
            .body(responseString.toByteArray().toResponseBody("application/json".toMediaTypeOrNull()))
            .addHeader("content-type", "application/json")
            .build()
    } else {
        // Forward đến server thật
        chain.proceed(chain.request())
    }
}
```

## Dependencies

### 1. NetworkModule (`base/src/main/java/com/basesource/base/di/NetworkModule.kt`)
```kotlin
val networkModule = module {
    // ... other dependencies
    single<TrendingApiService> {
        get<Retrofit>().create(TrendingApiService::class.java)
    }
}
```

### 2. AppModule (`app/src/main/java/com/amb/emoij/di/AppModule.kt`)
```kotlin
@Single
fun provideTrendingViewModel(trendingRepository: TrendingRepository): TrendingViewModel {
    return TrendingViewModel(trendingRepository)
}
```

## Mock Data Structure

### trending_mock.json
```json
{
  "status": "success",
  "data": {
    "trending_items": [
      {
        "id": "1",
        "title": "Trending Item 1",
        "description": "This is a trending item description",
        "image_url": "https://picsum.photos/300/200?random=1",
        "likes": 1250,
        "views": 15000,
        "category": "Entertainment",
        "is_liked": false,
        "created_at": "2024-01-15T10:30:00Z"
      }
      // ... more items
    ],
    "pagination": {
      "current_page": 1,
      "total_pages": 3,
      "per_page": 6,
      "total_items": 18
    }
  }
}
```

## Lợi ích

### 1. **Giống API thật**
- Sử dụng Retrofit interface
- Có error handling
- Có loading states
- Có response parsing

### 2. **Dễ dàng chuyển đổi**
- Chỉ cần thay đổi base URL
- Không cần thay đổi code logic
- Mock data có thể thay thế bằng API thật

### 3. **Tái sử dụng**
- Data models ở base module
- API service có thể dùng cho nhiều module
- Repository pattern chuẩn

### 4. **Testing**
- Dễ dàng test với mock data
- Có thể test error cases
- Có thể test loading states

## Cách thêm API mới

### 1. Thêm endpoint vào MockInterceptor
```kotlin
const val MOCK_NEW_API = "mock/new"
const val MOCK_FILE_NEW_JSON = "new_mock.json"
```

### 2. Tạo API Service
```kotlin
interface NewApiService {
    @GET("mock/new")
    suspend fun getNewData(): Response<ResponseBody>
}
```

### 3. Thêm vào NetworkModule
```kotlin
single<NewApiService> {
    get<Retrofit>().create(NewApiService::class.java)
}
```

### 4. Tạo JSON file
- Đặt file JSON vào `base/src/main/assets/`
- Đảm bảo format JSON đúng

## Kết luận
Hệ thống mock API đã được cập nhật để hoạt động như API thật, giúp:
- Phát triển offline
- Test dễ dàng
- Chuyển đổi sang API thật đơn giản
- Code clean và maintainable
