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
    //@FormUrlEncoded
    //@Headers({"Referer: /forum/ucp.php?mode=login", "Cache-Control: no-cache"})
    @POST("forum/oauthorize_app.php")
        //Call<String> signIn(@Body UserData data);
        //Call<String> signIn(@FieldMap Map<String, String> params);
    Call<ResponseBody> signInSocial(@Header("Authorization") String header, @QueryMap Map<String, String> options, @PartMap Map<String, RequestBody> params);
    /*Call<ResponseBody> signInSocial(
            @Header("Authorization") String header,
            @Field(value = "token_type", encoded = true) String token_type,
            @Field(value = "access_token", encoded = true) String access_token,
            @Field(value = "expires_in", encoded = true) String expires_in);*/


    @Multipart
    @POST("forum/notifications/register.php")
    Call<ResponseBody> register(@Header("Authorization") String header, @QueryMap Map<String, String> options, @PartMap Map<String, RequestBody> params);

    @Headers({"Accept: application/json", "Cache-Control: no-cache"})
    @GET("forum/notify.php")
    Call<ResponseBody> updateNotify(@Header("Authorization") String header, @QueryMap Map<String, String> options);
}