package com.example.behavior_based_authentication;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface JsonApi {

    @POST("/post")
    Call<JsonDataSet> PostData(@Body JsonDataSet jsonDataSet);
}
