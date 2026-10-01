package com.example.core.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.example.BuildConfig
import com.example.core.ads.AdConstants
import com.example.core.time.NetworkTimeManager

/**
 * Trình quản lý thanh toán Google Play Billing (com.android.billingclient:billing)
 * Cho các gói VIP 2 và VIP 3.
 * 
 * Tích hợp:
 * - Đồng hồ internet (NetworkTimeManager) để phát hiện chính xác khung giờ vàng Flash Sale.
 * - Áp dụng giảm giá tự động vào các ngày Chủ Nhật, Thứ 2, Thứ 4, Thứ 6 (Sun, Mon, Wed, Fri):
 *   + VIP 2: Giảm từ $1.25 xuống $0.699
 *   + VIP 3: Giảm từ $4.00 xuống $2.599
 * - Hỗ trợ luồng mô phỏng hoàn tất mượt mà khi ID Google Play đang để trống.
 */
class BillingManager(
    private val context: Context,
    private val onPurchaseSuccess: (tier: Int) -> Unit
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
    }

    private val networkTimeManager = NetworkTimeManager.getInstance(context)
    private var billingClient: BillingClient? = null
    private var isConnected = false
    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    init {
        setupBillingClient()
    }

    /**
     * Khởi tạo BillingClient với cấu hình lắng nghe giao dịch
     */
    private fun setupBillingClient() {
        try {
            val pendingParams = PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()

            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(pendingParams)
                .build()

            startBillingConnection()
        } catch (e: Exception) {
            Log.w(TAG, "Lỗi khởi tạo BillingClient: ${e.message}")
        }
    }

    /**
     * Kết nối với dịch vụ Google Play Billing
     */
    fun startBillingConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isConnected = true
                    Log.d(TAG, "BillingClient kết nối thành công với Google Play Store")
                    queryAvailableProducts()
                    queryPurchases()
                } else {
                    isConnected = false
                    Log.w(TAG, "Billing setup thất bại: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnected = false
                Log.d(TAG, "Billing service bị ngắt kết nối")
            }
        })
    }

    /**
     * Khôi phục / truy vấn các gói đã mua trước đó của người chơi (Restore Purchases)
     */
    fun queryPurchases() {
        val client = billingClient ?: return
        if (!isConnected) return
        try {
            val params = com.android.billingclient.api.QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            client.queryPurchasesAsync(params) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Tìm thấy ${purchasesList.size} giao dịch đã mua")
                    for (purchase in purchasesList) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            handlePurchase(purchase)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Lỗi truy vấn giao dịch: ${e.message}")
        }
    }

    /**
     * Kiểm tra khung giờ giảm giá Flash Sale theo đồng hồ chuẩn Internet:
     * Chủ Nhật, Thứ 2, Thứ 4, Thứ 6 (Sun, Mon, Wed, Fri) từ 9:00 đến 12:00
     */
    fun isScheduledDiscountActive(): Boolean {
        return networkTimeManager.isFlashSaleActive()
    }

    /**
     * Lấy giá hiển thị cho gói VIP (USD) theo thời gian thực (đã tính giảm giá)
     */
    fun getVipPriceUsd(tier: Int): Double {
        val isDiscount = isScheduledDiscountActive()
        return when (tier) {
            2 -> if (isDiscount) AdConstants.VIP2_DISCOUNT_PRICE_USD else AdConstants.VIP2_REGULAR_PRICE_USD
            3 -> if (isDiscount) AdConstants.VIP3_DISCOUNT_PRICE_USD else AdConstants.VIP3_REGULAR_PRICE_USD
            else -> 0.0
        }
    }

    /**
     * Lấy chuỗi giá hiển thị đã định dạng (ví dụ: "$0.699" hoặc "$1.25")
     */
    fun getFormattedPrice(tier: Int): String {
        val targetProductId = getProductIdForTier(tier)
        val details = productDetailsMap[targetProductId]
        val formattedFromStore = details?.oneTimePurchaseOfferDetails?.formattedPrice
        if (!formattedFromStore.isNullOrEmpty()) {
            return formattedFromStore
        }

        val price = getVipPriceUsd(tier)
        return String.format(java.util.Locale.US, "$%.3f", price).trimEnd('0').let {
            if (it.endsWith(".")) it + "00" else it
        }.let {
            if (tier == 2 && isScheduledDiscountActive()) "$0.699"
            else if (tier == 2) "$1.25"
            else if (tier == 3 && isScheduledDiscountActive()) "$2.599"
            else if (tier == 3) "$4.00"
            else it
        }
    }

    /**
     * Lấy Product ID tương ứng cho cấp độ VIP và thời điểm hiện tại (giảm giá hay ngày thường)
     */
    fun getProductIdForTier(tier: Int): String {
        val isDiscount = isScheduledDiscountActive()
        return when (tier) {
            2 -> {
                if (isDiscount && AdConstants.PRODUCT_ID_VIP_2_DISCOUNT.isNotBlank()) {
                    AdConstants.PRODUCT_ID_VIP_2_DISCOUNT.trim()
                } else {
                    AdConstants.PRODUCT_ID_VIP_2.trim()
                }
            }
            3 -> {
                if (isDiscount && AdConstants.PRODUCT_ID_VIP_3_DISCOUNT.isNotBlank()) {
                    AdConstants.PRODUCT_ID_VIP_3_DISCOUNT.trim()
                } else {
                    AdConstants.PRODUCT_ID_VIP_3.trim()
                }
            }
            else -> ""
        }
    }

    /**
     * Truy vấn thông tin sản phẩm VIP từ Google Play Console
     */
    private fun queryAvailableProducts() {
        val productIds = listOfNotNull(
            AdConstants.PRODUCT_ID_VIP_2.trim().ifEmpty { null },
            AdConstants.PRODUCT_ID_VIP_2_DISCOUNT.trim().ifEmpty { null },
            AdConstants.PRODUCT_ID_VIP_3.trim().ifEmpty { null },
            AdConstants.PRODUCT_ID_VIP_3_DISCOUNT.trim().ifEmpty { null }
        ).distinct()

        if (productIds.isEmpty()) {
            Log.d(TAG, "Product ID đang để trống theo yêu cầu. Sẵn sàng luồng mô phỏng.")
            return
        }

        val productList = productIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val queryParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(queryParams) { billingResult, productDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val list = productDetailsResult.productDetailsList
                if (list != null) {
                    for (details in list) {
                        productDetailsMap[details.productId] = details
                    }
                    Log.d(TAG, "Đã truy vấn ${list.size} sản phẩm IAP từ Google Play")
                }
            } else {
                Log.w(TAG, "Lỗi truy vấn sản phẩm: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Khởi chạy luồng thanh toán Google Play Billing cho gói VIP
     * @param activity Activity gọi thanh toán
     * @param tier Cấp độ VIP (2 hoặc 3)
     * @param onFallbackSuccess Callback fallback khi Product ID chưa cấu hình hoặc trong môi trường dev
     */
    fun purchaseVip(
        activity: Activity,
        tier: Int,
        onFallbackSuccess: () -> Unit
    ) {
        val productId = getProductIdForTier(tier)

        // Cung cấp luồng mô phỏng CHỈ trong môi trường DEBUG khi chưa cấu hình Product ID trên Play Console
        if (productId.isEmpty() || !isConnected) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "[DEBUG] Product ID cho VIP $tier đang để trống. Kích hoạt mô phỏng trong môi trường thử nghiệm.")
                onFallbackSuccess()
            } else {
                Log.w(TAG, "Dịch vụ thanh toán Google Play chưa sẵn sàng hoặc Product ID chưa được cấu hình.")
            }
            return
        }

        val productDetails = productDetailsMap[productId]
        if (productDetails != null) {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val responseCode = billingClient?.launchBillingFlow(activity, billingFlowParams)?.responseCode
            if (responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "Không thể mở luồng thanh toán Google Play: mã lỗi $responseCode")
                if (BuildConfig.DEBUG) {
                    onFallbackSuccess()
                }
            }
        } else {
            Log.w(TAG, "Chưa tìm thấy SKU $productId trên Google Play.")
            if (BuildConfig.DEBUG) {
                onFallbackSuccess()
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "Người dùng đã hủy thanh toán")
        } else {
            Log.w(TAG, "Lỗi thanh toán: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Xác nhận giao dịch Google Play thành công")
                        grantVipPerks(purchase)
                    }
                }
            } else {
                grantVipPerks(purchase)
            }
        }
    }

    private fun grantVipPerks(purchase: Purchase) {
        val products = purchase.products
        when {
            products.contains(AdConstants.PRODUCT_ID_VIP_3) ||
                    products.contains(AdConstants.PRODUCT_ID_VIP_3_DISCOUNT) -> onPurchaseSuccess(3)
            products.contains(AdConstants.PRODUCT_ID_VIP_2) ||
                    products.contains(AdConstants.PRODUCT_ID_VIP_2_DISCOUNT) -> onPurchaseSuccess(2)
            else -> {
                Log.w(TAG, "Giao dịch chứa sản phẩm không xác định: $products. Không cấp quyền lợi VIP tự động.")
            }
        }
    }

    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }
}
