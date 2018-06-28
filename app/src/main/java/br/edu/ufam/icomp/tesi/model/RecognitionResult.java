package br.edu.ufam.icomp.tesi.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by berg on 19/06/18.
 */

public class RecognitionResult implements Serializable {
    @SerializedName("lines")
    private List<Lines> lines;

    public List<Lines> getLines() {
        return lines;
    }

    public void setLines(List<Lines> lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "RecognitionResult{" +
                "lines=" + lines +
                '}';
    }
}
