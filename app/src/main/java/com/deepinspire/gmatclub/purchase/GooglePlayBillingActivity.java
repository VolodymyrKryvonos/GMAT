package com.deepinspire.gmatclub.purchase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.deepinspire.gmatclub.R;

import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * /**
 * Created by Andriy Lykhtey on 2019-11-01.
 */
public class GooglePlayBillingActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private BillingClient billingClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_play_billing);
        ButterKnife.bind(this);
        setUpBillingClient();
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {

    }

    @OnClick(R.id.tvPrivacyPolicy)
    public void openPrivacyPolicy() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://gmatclub.com/static/gmatclub-terms-and-conditions-and-privacy-policy.php"));
        startActivity(browserIntent);
    }

    @OnClick(R.id.btnStartSubscription)
    public void startSubscription() {

        if (billingClient.isReady()) {
            SkuDetailsParams params = SkuDetailsParams.newBuilder()
                    .setSkusList(Arrays.asList("id_quizzes"))
                    .setType(BillingClient.SkuType.INAPP)
                    .build();

            billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetailsList.get(0))
                                .build();

                        billingClient.launchBillingFlow(GooglePlayBillingActivity.this,flowParams);

                    } else
                        Toast.makeText(GooglePlayBillingActivity.this, "Cannot query product", Toast.LENGTH_SHORT).show();
                }
            });

        } else
            Toast.makeText(GooglePlayBillingActivity.this, "Billing client not ready", Toast.LENGTH_SHORT).show();
    }

    private void setUpBillingClient() {

        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(this)
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Toast.makeText(GooglePlayBillingActivity.this, "Success to connect Billing", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(GooglePlayBillingActivity.this, billingResult.getResponseCode(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(GooglePlayBillingActivity.this, "You are disconnect from Billing", Toast.LENGTH_SHORT).show();
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

}
