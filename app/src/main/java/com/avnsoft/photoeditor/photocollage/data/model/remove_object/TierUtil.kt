package com.avnsoft.photoeditor.photocollage.data.model.remove_object

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale

object TierUtil {
    // Danh sách mã quốc gia cho từng nhóm tier
    private val TEAR_1_COUNTRY_CODES = mutableListOf<String?>(
        "US", "CA", "GB", "AU", "NZ", "DE", "FR", "JP", "KR", "SE", "DK", "NO", "FI"
    )

    private val TEAR_2_COUNTRY_CODES = mutableListOf<String?>(
        "BR", "MX", "RU", "CN", "IN", "ZA", "TR", "PL", "CZ", "AR", "TH", "MY", "ID", "PH"
    )

    private val TEAR_3_COUNTRY_CODES = mutableListOf<String?>(
        "PK", "VN", "NG", "EG", "BD", "KE", "LK", "ET", "TZ", "UG", "GH", "UA", "VE"
    )

    /**
     * Xác định tier của người dùng dựa trên mã quốc gia.
     *
     * @param context Context hiện tại
     * @return String biểu thị tier (1, 2 hoặc 3)
     */
    fun getTearUser(context: Context): String {
        // Nếu ứng dụng đã mua, trả về "0" (bỏ phần này nếu không cần)
        // if (SharePref.isAppPurchased()) {
        //     return "0";
        // }

        // Lấy mã quốc gia từ TelephonyManager

        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

        val networkCountryIso =
            if (telephonyManager != null) telephonyManager.getNetworkCountryIso() else ""

        if (networkCountryIso != null) {
            val countryCode = networkCountryIso.uppercase(Locale.getDefault())

            if (TEAR_1_COUNTRY_CODES.contains(countryCode)) {
                return "1"
            } else if (TEAR_2_COUNTRY_CODES.contains(countryCode)) {
                return "2"
            }
        }

        // Mặc định trả về tier 3
        return "3"
    }
}