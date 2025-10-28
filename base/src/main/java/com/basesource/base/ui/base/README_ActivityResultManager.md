# ActivityResultManager

Một manager để quản lý các ActivityResultLauncher một cách gọn gàng, cho phép sử dụng callback ngay tại chỗ cần launch activity.

## Tính năng

- ✅ Quản lý tất cả ActivityResultLauncher trong một class
- ✅ Callback inline ngay tại chỗ cần launch
- ✅ Hỗ trợ nhiều loại launcher: Activity, Permission, Image picker, Camera
- ✅ Tự động cleanup để tránh memory leak
- ✅ Extension functions để sử dụng gọn gàng hơn

## Cách sử dụng

### 1. Trong BaseActivity (Khuyến nghị)

```kotlin
class MyActivity : BaseActivity() {
    
    fun launchSomeActivity() {
        val intent = Intent(this, SomeActivity::class.java)
        launchActivity(intent) { result ->
            if (result.resultCode == RESULT_OK) {
                // Xử lý kết quả thành công
                val data = result.data
                // ...
            }
        }
    }
    
    fun requestPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            if (cameraGranted) {
                openCamera()
            }
        }
    }
    
    fun pickImage() {
        pickImage("image/*") { uri ->
            uri?.let {
                // Xử lý ảnh đã chọn
                loadImage(it)
            }
        }
    }
}
```

### 2. Trong ComponentActivity thông thường

```kotlin
class MyActivity : ComponentActivity() {
    
    private val activityResultManager by lazy { createActivityResultManager() }
    
    fun launchSomeActivity() {
        val intent = Intent(this, SomeActivity::class.java)
        activityResultManager.launchActivity(intent) { result ->
            // Xử lý kết quả
        }
    }
}
```

## Các loại launcher được hỗ trợ

### 1. Launch Activity
```kotlin
launchActivity(intent) { result ->
    // Xử lý kết quả
}
```

### 2. Request Multiple Permissions
```kotlin
requestPermissions(arrayOf(permission1, permission2)) { permissions ->
    // Xử lý kết quả permissions
}
```

### 3. Request Single Permission
```kotlin
requestPermission(Manifest.permission.CAMERA) { isGranted ->
    // Xử lý kết quả permission
}
```

### 4. Pick Image from Gallery
```kotlin
pickImage("image/*") { uri ->
    // Xử lý ảnh đã chọn
}
```

### 5. Take Picture with Camera
```kotlin
val photoUri = Uri.fromFile(photoFile)
takePicture(photoUri) { success ->
    // Xử lý kết quả chụp ảnh
}
```

## So sánh trước và sau

### Trước (Cách cũ)
```kotlin
class SettingsActivity : BaseActivity() {
    
    private val languageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            recreate()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ...
        onLanguageClick = {
            val intent = LanguageSelectionActivity.createIntent(this)
            languageLauncher.launch(intent)
        }
    }
}
```

### Sau (Cách mới)
```kotlin
class SettingsActivity : BaseActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ...
        onLanguageClick = {
            val intent = LanguageSelectionActivity.createIntent(this)
            launchActivity(intent) { result ->
                if (result.resultCode == RESULT_OK) {
                    recreate()
                }
            }
        }
    }
}
```

## Lợi ích

1. **Gọn gàng hơn**: Không cần khai báo launcher ở đầu class
2. **Callback inline**: Xử lý kết quả ngay tại chỗ cần launch
3. **Tái sử dụng**: Một manager cho tất cả các loại launcher
4. **Memory safe**: Tự động cleanup callbacks
5. **Dễ bảo trì**: Tập trung logic quản lý launcher

## Lưu ý

- Manager sẽ tự động cleanup callbacks trong `onDestroy()`
- Sử dụng lazy initialization để đảm bảo activity đã sẵn sàng
- Extension functions chỉ hoạt động với BaseActivity
