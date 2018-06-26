package br.edu.ufam.icomp.tesi.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by berg on 19/06/18.
 */

public class Lines implements Serializable {
    @SerializedName("boundingBox")
    private List<Integer> boundingBoxList;
    @SerializedName("text")
    private String text;
    @SerializedName("words")
    private List<Lines> linesWords;

    public List<Integer> getBoundingBoxList() {
        return boundingBoxList;
    }

    public void setBoundingBoxList(List<Integer> boundingBoxList) {
        this.boundingBoxList = boundingBoxList;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Lines> getLinesWords() {
        return linesWords;
    }

    public void setLinesWords(List<Lines> linesWords) {
        this.linesWords = linesWords;
    }
}
