# Màn hình Home và Trending

## Tổng quan
Đã tạo thành công màn hình home với tab bottom và màn hình trending theo yêu cầu.

## Cấu trúc code

### 1. Màn hình Home (`HomeScreen.kt`)
- **Vị trí**: `app/src/main/java/com/amb/emoij/ui/screens/home/HomeScreen.kt`
- **Chức năng**: 
  - Hiển thị title "Status Bar"
  - Tab bottom với 3 tab: Trending, Gesture, Customize
  - Chuyển đổi giữa các màn hình con

### 2. Màn hình Trending (`TrendingScreen.kt`)
- **Vị trí**: `app/src/main/java/com/amb/emoij/ui/screens/trending/TrendingScreen.kt`
- **Chức năng**:
  - Hiển thị danh sách trending items
  - Card layout với ảnh, title, description, category
  - Like/unlike functionality
  - Stats (views, likes)

### 3. Tab Bottom Component (`BottomTabBar.kt`)
- **Vị trí**: `app/src/main/java/com/amb/emoij/ui/components/BottomTabBar.kt`
- **Chức năng**:
  - Icon enable/disable cho từng tab
  - Animation khi chọn tab
  - Sử dụng Jetpack Compose

### 4. Data Models
- **TrendingItem.kt**: Model cho trending item
- **TrendingRepository.kt**: Repository pattern
- **TrendingViewModel.kt**: ViewModel với mock data

### 5. Icons được sử dụng
- `ic_home_tab_trending_enable.xml` / `ic_home_tab_trending_disable.xml`
- `ic_home_tab_gesture_enable.xml` / `ic_home_tab_gesture_disable.xml`
- `ic_home_tab_custumize_enable.xml` / `ic_home_tab_custumize_disable.xml`

## Mock Data
- **File JSON**: `app/src/main/assets/trending_mock.json`
- **Dữ liệu**: 6 trending items với đầy đủ thông tin
- **Pagination**: Hỗ trợ phân trang

## Dependencies đã thêm
- **Coil**: Để load ảnh từ URL
- **Koin**: Dependency injection
- **Jetpack Compose**: UI framework

## Cách chạy
1. Build project: `./gradlew build`
2. Chạy app: `./gradlew installDebug`
3. Màn hình Home sẽ là launcher activity

## Tính năng chính
✅ Tab bottom với icon enable/disable  
✅ Màn hình trending với card layout  
✅ Like/unlike functionality  
✅ Mock data từ JSON file  
✅ Sử dụng Jetpack Compose  
✅ Clean architecture với Repository pattern  
✅ Dependency injection với Koin  
✅ Responsive UI design  

## Cấu trúc thư mục
```
app/src/main/java/com/amb/emoij/
├── data/
│   ├── model/TrendingItem.kt
│   └── repository/TrendingRepository.kt
├── ui/
│   ├── activities/home/HomeActivity.kt
│   ├── components/BottomTabBar.kt
│   ├── screens/
│   │   ├── home/HomeScreen.kt
│   │   └── trending/TrendingScreen.kt
│   └── viewmodel/TrendingViewModel.kt
└── di/AppModule.kt
```
