package com.lmweav.schoolquest.utilities.billing;

import android.app.Activity;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.List;

/*
 * School Quest: BillingManager
 * This class handles in-app purchases.
 *
 * Methods in this class query the sku details and start a purchase flow to display the Google play
 * billing dialog for the first one-time product. Purchases are automatically consumed, so one-time
 * products can be bought multiple times.
 *
 * @author Luke Weaver
 * @version 1.0.5
 * @since 2019-04-21
 */
public class BillingManager implements PurchasesUpdatedListener {

    private static final String TAG = "BillingManager";

    private boolean isServiceConnected;
    private final BillingClient mBillingClient;
    private final Activity mActivity;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public BillingManager(Activity activity) {
        mActivity = activity;
        mBillingClient = BillingClient.newBuilder(mActivity).setListener(this).build();
        startServiceConnection(null);
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    private void startServiceConnection(final Runnable runnable) {
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponse) {
                if (billingResponse == BillingClient.BillingResponse.OK) {
                    isServiceConnected = true;
                    if (runnable != null) {
                        runnable.run();
                    }
                    queryPurchases();
                    Log.i(TAG, "onBillingSetupFinished() response: " + billingResponse);
                } else {
                    Log.w(TAG, "onBillingSetupFinished() error code: " + billingResponse);
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                isServiceConnected = false;
                Log.w(TAG, "onBillingServiceDisconnected()");
            }
        });
    }

    @Override
    public void onPurchasesUpdated(@BillingClient.BillingResponse int responseCode,
                                   List<Purchase> purchases) {
        if (purchases == null || responseCode != BillingClient.BillingResponse.OK) { return; }
        for (Purchase purchase: purchases) {
            mBillingClient.consumeAsync(purchase.getPurchaseToken(), new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(int responseCode, String purchaseToken) {
                    if (responseCode != BillingClient.BillingResponse.OK) {
                        startServiceConnection(null);
                    }
                }
            });
        }
        Log.d(TAG, "onPurchasesUpdated() response: " + responseCode);
    }

    public void querySkuDetailsAsync(@BillingClient.SkuType final String itemType,
                                     final List<String> skuList,
                                     final SkuDetailsResponseListener listener) {
        SkuDetailsParams skuDetailsParams = SkuDetailsParams.newBuilder().setSkusList(skuList).
                setType(itemType).build();
        mBillingClient.querySkuDetailsAsync(skuDetailsParams, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                listener.onSkuDetailsResponse(responseCode, skuDetailsList);
            }
        });
    }

    public void startPurchaseFlow(SkuDetails details) {
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().
                setSkuDetails(details).build();
        mBillingClient.launchBillingFlow(mActivity, billingFlowParams);
    }

    private void executeServiceRequest(Runnable runnable) {
        if (isServiceConnected) {
            runnable.run();
        } else {
            startServiceConnection(runnable);
        }
    }

    private void queryPurchases() {
        Runnable queryToExecute = new Runnable() {
            @Override
            public void run() {
                Purchase.PurchasesResult purchasesResult =
                        mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
                onQueryPurchasesFinished(purchasesResult);

            }
        };
        executeServiceRequest(queryToExecute);


    }

    private void onQueryPurchasesFinished(Purchase.PurchasesResult result) {
        if (mBillingClient == null || result.getResponseCode() != BillingClient.BillingResponse.OK) {
            Log.w(TAG, "Billing client was null or result code (" + result.getResponseCode()
                    + ") was bad – quitting");
            return;
        }
        for (Purchase purchase: result.getPurchasesList()) {
            mBillingClient.consumeAsync(purchase.getPurchaseToken(), new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(int responseCode, String purchaseToken) {
                    if (responseCode != BillingClient.BillingResponse.OK) {
                        startServiceConnection(null);
                    }
                }
            });
        }
    }


}
