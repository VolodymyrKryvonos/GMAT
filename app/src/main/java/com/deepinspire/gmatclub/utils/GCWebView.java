package com.deepinspire.gmatclub.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.deepinspire.gmatclub.api.Api;

import java.util.Map;

/**
 * Created by dmytro mytsko on 23.03.18.
 */
public class GCWebView extends WebView {

    private boolean isLoading = false;

    public GCWebView(Context context) {
        super(context);
    }

    public GCWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GCWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GCWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        url = addMobileStyleParameter(url);

        isLoading = true;

        super.loadUrl(url, additionalHttpHeaders);
    }

    @Override
    public void loadUrl(String url) {
        url = addMobileStyleParameter(url);
        super.loadUrl(url);
    }

    private String addMobileStyleParameter(String url) {
        String mobileStyleParameter = "style=12";

        if (url != null && url.contains(Api.DOMAIN)) {
            if (url.contains(mobileStyleParameter)) {
                return url;
            } else {
                if (url.contains("?")) {
                    if (url.contains("#")) {
                        String urlParts[] = url.split("#", 2);
                        url = urlParts[0] + "&" + mobileStyleParameter + "#" + urlParts[1];
                    } else {
                        url = url + "&" + mobileStyleParameter;
                    }
                } else {
                    if (url.contains("#")) {
                        String urlParts[] = url.split("#", 2);
                        url = urlParts[0] + "?" + mobileStyleParameter + "#" + urlParts[1];
                    } else {
                        url = url + "?" + mobileStyleParameter;
                    }
                }
            }
        }

        return url;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void isLoading(boolean loading) {
        isLoading = loading;
    }
}