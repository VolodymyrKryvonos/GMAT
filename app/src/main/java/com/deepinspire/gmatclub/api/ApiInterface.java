package com.deepinspire.gmatclub.api;

import com.deepinspire.gmatclub.storage.User;
import com.deepinspire.gmatclub.storage.UserData;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.QueryMap;

public interface ApiInterface {
    @Multipart
    //@FormUrlEncoded
    @POST("forum/ucp.php?mode=login")
    //Call<String> signIn(@Body UserData data);
    //Call<String> signIn(@FieldMap Map<String, String> params);
    Call<String> signIn(@Header("Authorization") String header, @PartMap Map<String, RequestBody> params);

    @Multipart
    //@FormUrlEncoded
    @POST("forum/ucp.php?mode=sendpassword")
    //Call<String> signIn(@Body UserData data);
    //Call<String> signIn(@FieldMap Map<String, String> params);
    Call<String> forgotPassword(@Header("Authorization") String header, @PartMap Map<String, RequestBody> params);

    @Multipart
    @POST("forum/oauthorize_app.php")
    Call<ResponseBody> signInSocial(@HeaderMap Map<String, String> headers, @QueryMap Map<String, String> options, @PartMap Map<String, RequestBody> params);

    @GET("oauth2/v4/tokeninfo")
    Call<ResponseBody> getToken(@QueryMap Map<String, String> options);

    @Multipart
    @POST("forum/notifications/register.php")
    Call<ResponseBody> register(@Header("Authorization") String header, @QueryMap Map<String, String> options, @PartMap Map<String, RequestBody> params);

    @Headers({"Accept: application/json", "Cache-Control: no-cache"})
    @GET("forum/notify.php")
    Call<ResponseBody> updateNotify(@Header("Authorization") String header, @QueryMap Map<String, String> options);

    @Headers({"Accept: application/json", "Cache-Control: no-cache"})
    @GET("forum/notify.php")
    Call<ResponseBody> getNotifications(@Header("Authorization") String header, @QueryMap Map<String, String> options);
}