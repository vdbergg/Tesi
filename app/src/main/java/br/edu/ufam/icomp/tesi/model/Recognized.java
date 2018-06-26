package br.edu.ufam.icomp.tesi.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by berg on 19/06/18.
 */

public class Recognized implements Serializable {
    @SerializedName("status")
    private String status;
    @SerializedName("recognitionResult")
    private RecognationResult recognationResult;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public RecognationResult getRecognationResult() {
        return recognationResult;
    }

    public void setRecognationResult(RecognationResult recognationResult) {
        this.recognationResult = recognationResult;
    }
}
