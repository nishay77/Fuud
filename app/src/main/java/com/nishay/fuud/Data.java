package com.nishay.fuud;

import android.util.Log;

import java.io.Serializable;

public class Data implements Serializable {

    private String name;
    private String link;
    private String imagePath;

    public Data(String imagePath, String name, String link) {
        this.imagePath = imagePath;
        this.name = format(name);
        this.link = link;
        Log.v("data", this.name + " " + this.link);
    }

    public String getName() {
        return name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getLink() {
        return link;
    }

    private String format(String old) {
        String s = old.toUpperCase();
        s = s.replace("--", "-AND-");
        s = s.replace('-',' ');
        s = s.split("\\.")[0];
        return s;
    }

}
