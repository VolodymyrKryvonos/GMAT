package com.deepinspire.gmatclub.utils;

import android.util.Log;
import android.webkit.CookieManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Provides a synchronization point between the webview cookie store and okhttp3.OkHttpClient cookie store
 */
public final class WebviewCookieHandler implements CookieJar {
    private CookieManager webviewCookieManager = CookieManager.getInstance();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        try {

            String urlString = url.toString();
            Log.e("saveFromResponse", url.toString());
            for (Cookie cookie : cookies) {
                Log.e("saveFromResponse", cookie.toString());
            }
            for (Cookie cookie : cookies) {
                webviewCookieManager.setCookie(urlString, cookie.toString().trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        try {
            String urlString = url.toString();
            String cookiesString = webviewCookieManager.getCookie(urlString);

            Log.e("loadForRequest", url.toString());
            if (cookiesString != null && !cookiesString.isEmpty()) {
                String[] cookieHeaders = cookiesString.split(";");
                List<Cookie> cookies = new ArrayList<>(cookieHeaders.length);

                for (String header : cookieHeaders) {
                    cookies.add(Cookie.parse(url, header));
                }
                for (Cookie cookie : cookies) {
                    Log.e("loadForRequest", cookie.toString());
                }
                return cookies;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
