package br.edu.ufam.icomp.tesi.service;

import br.edu.ufam.icomp.tesi.model.Recognized;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by berg on 19/06/18.
 */

public interface TesiService {

    @Headers({
            "Ocp-Apim-Subscription-Key: 0aece025f5ba49d18038013a2f9555ac",
            "Content-Type: application/octet-stream"
    })
    @POST("recognizeText?handwriting=true")
    Call<ResponseBody> sendImage(@Body RequestBody image);

    @Headers({
            "Ocp-Apim-Subscription-Key: 0aece025f5ba49d18038013a2f9555ac"
    })
    @GET()
    Call<Recognized> getRecognized(@Url String url);
}
