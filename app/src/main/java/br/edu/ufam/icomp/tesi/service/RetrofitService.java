package br.edu.ufam.icomp.tesi.service;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by berg on 19/06/18.
 */

public class RetrofitService {

    private final Retrofit mRetrofit;

    private static RetrofitService mInstance;

    public static RetrofitService getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RetrofitService();
        }

        return mInstance;
    }

    private RetrofitService() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();

        mRetrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://brazilsouth.api.cognitive.microsoft.com/vision/v1.0/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public TesiService tesiService() {
        return mRetrofit.create(TesiService.class);
    }
}
