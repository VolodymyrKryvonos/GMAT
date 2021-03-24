package com.deepinspire.gmatclub.purchase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetailsParams;
import com.deepinspire.gmatclub.R;
import com.deepinspire.gmatclub.api.Api;

import java.util.Collections;
import java.util.List;


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

        View tvPrivacyPolicy = findViewById(R.id.tvPrivacyPolicy);
        View btnStartSubscription = findViewById(R.id.btnStartSubscription);
        tvPrivacyPolicy.setOnClickListener(v -> openPrivacyPolicy());
        btnStartSubscription.setOnClickListener(v -> startSubscription());
        setUpBillingClient();
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {

    }


    public void openPrivacyPolicy() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(Api.POLICY_URL));
        startActivity(browserIntent);
    }


    public void startSubscription() {

        if (billingClient.isReady()) {
            SkuDetailsParams params = SkuDetailsParams.newBuilder()
                    .setSkusList(Collections.singletonList("id_quizzes"))
                    .setType(BillingClient.SkuType.INAPP)
                    .build();

            billingClient.querySkuDetailsAsync(params, (billingResult, skuDetailsList) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetailsList.get(0))
                            .build();

                    billingClient.launchBillingFlow(GooglePlayBillingActivity.this, flowParams);

                } else
                    Toast.makeText(GooglePlayBillingActivity.this, "Cannot query product", Toast.LENGTH_SHORT).show();
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
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
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
