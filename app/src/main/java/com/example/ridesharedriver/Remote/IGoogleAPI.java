package com.example.ridesharedriver.Remote;

import retrofit2.Call;
import retrofit2.http.Url;

public interface IGoogleAPI {
    Call<String> getPath(@Url String url);
}
