// app/src/main/java/com/carrom/bot/billing/BillingManager.kt

package com.carrom.bot.billing

import android.content.Context
import com.android.billingclient.api.*
import timber.log.Timber

class BillingManager(private val context: Context) {
    
    private lateinit var billingClient: BillingClient
    private var purchaseListener: ((Purchase) -> Unit)? = null
    
    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchases?.forEach { purchase ->
                        purchaseListener?.invoke(purchase)
                    }
                }
            }
            .enablePendingPurchases()
            .build()
        
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Billing setup complete")
                }
            }
            
            override fun onBillingServiceDisconnected() {
                Timber.d("Billing service disconnected")
            }
        })
    }
    
    fun launchPurchaseFlow(skuId: String) {
        val skuDetails = SkuDetailsParams.newBuilder()
            .setSkusList(listOf(skuId))
            .setType(BillingClient.SkuType.INAPP)
            .build()
        
        billingClient.querySkuDetailsAsync(skuDetails) { result, skuDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                skuDetailsList?.forEach { skuDetails ->
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build()
                    
                    billingClient.launchBillingFlow(context as android.app.Activity, flowParams)
                }
            }
        }
    }
    
    fun getPurchases(): List<Purchase> {
        val result = billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP) { _, purchases ->
            Timber.d("Purchases: ${purchases.size}")
        }
        return emptyList()
    }
}
