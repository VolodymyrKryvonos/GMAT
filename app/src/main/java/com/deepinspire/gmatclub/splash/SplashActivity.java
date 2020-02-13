package com.deepinspire.gmatclub.splash;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.deepinspire.gmatclub.R;
import com.deepinspire.gmatclub.api.Api;
import com.deepinspire.gmatclub.auth.AuthActivity;
import com.deepinspire.gmatclub.storage.Injection;
import com.deepinspire.gmatclub.storage.Repository;
import com.deepinspire.gmatclub.web.WebActivity;

import static com.deepinspire.gmatclub.notifications.Notifications.INPUT_URL;

public class SplashActivity extends AppCompatActivity {

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent != null) {
            onNewIntent(intent);
        }
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Uri uri = getURI();

        if(uri != null) {
            Intent intent = new Intent(SplashActivity.this, WebActivity.class);

            intent.setData(uri);
            intent.putExtra(INPUT_URL,url);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        } else {
            Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
            intent.putExtra(INPUT_URL,url);
            startActivity(intent);
        }

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            url = bundle.getString(INPUT_URL);
        }
    }

    private Uri getURI() {
        Intent intent = getIntent();

        if(intent != null) {
            Uri uri = intent.getData();

            if(uri != null) {
                return uri;
            }
        }

        return null;
    }
}
