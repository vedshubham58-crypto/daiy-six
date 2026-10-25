package com.vedshubham.dailysix.billing

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
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.vedshubham.dailysix.data.GameStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The one-time $1.99 "Pro" upgrade through Google Play Billing.
 * The product ID must match the in-app product created in Play Console.
 */
object Billing {
    const val PRO_PRODUCT_ID = "pro_upgrade"
    private const val TAG = "Billing"

    private lateinit var client: BillingClient
    private lateinit var store: GameStore
    private var proDetails: ProductDetails? = null

    private val _price = MutableStateFlow<String?>(null)
    /** Localized price like "$1.99" or "₹159", once Play has answered. */
    val price: StateFlow<String?> = _price.asStateFlow()

    fun init(context: Context) {
        store = GameStore.get(context)
        client = BillingClient.newBuilder(context.applicationContext)
            .setListener { result, purchases -> onPurchasesUpdated(result, purchases) }
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()
        connect()
    }

    private fun connect() {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    loadProduct()
                    restorePurchases()
                } else {
                    Log.w(TAG, "Setup failed: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Disconnected")
            }
        })
    }

    private fun loadProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRO_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()
        client.queryProductDetailsAsync(params) { result, details ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "Product query failed: ${result.debugMessage}")
                return@queryProductDetailsAsync
            }
            proDetails = details.productDetailsList.firstOrNull()
            _price.value = proDetails?.oneTimePurchaseOfferDetails?.formattedPrice
        }
    }

    /** Gives Pro back after a reinstall or on a new phone with the same Google account. */
    private fun restorePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryPurchasesAsync
            val owned = purchases.any { isPro(it) }
            store.setPro(owned)
            purchases.filter { isPro(it) }.forEach(::acknowledge)
        }
    }

    /** Opens Google Play's payment sheet. Returns false if the store isn't reachable yet. */
    fun buyPro(activity: Activity): Boolean {
        val details = proDetails
        if (details == null) {
            if (client.isReady) loadProduct() else connect()
            return false
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build()),
            )
            .build()
        client.launchBillingFlow(activity, params)
        return true
    }

    private fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            restorePurchases()
            return
        }
        if (result.responseCode != BillingClient.BillingResponseCode.OK || purchases == null) return
        for (purchase in purchases) {
            if (isPro(purchase)) {
                store.setPro(true)
                acknowledge(purchase)
            }
        }
    }

    private fun isPro(purchase: Purchase) =
        PRO_PRODUCT_ID in purchase.products && purchase.purchaseState == Purchase.PurchaseState.PURCHASED

    // Play refunds purchases that are not acknowledged within 3 days.
    private fun acknowledge(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        client.acknowledgePurchase(params) { r ->
            if (r.responseCode != BillingClient.BillingResponseCode.OK) Log.w(TAG, "Ack failed: ${r.debugMessage}")
        }
    }
}
