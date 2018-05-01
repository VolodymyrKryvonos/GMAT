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

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        Repository repository = Injection.getRepository(getApplicationContext());

        if(repository.logged()) {
            Intent intent = new Intent(SplashActivity.this, WebActivity.class);

            intent.setData(Uri.parse(Api.FORUM_URL));

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        } else {
            startActivity(new Intent(this, AuthActivity.class));
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
}
