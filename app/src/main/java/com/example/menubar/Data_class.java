package com.example.menubar;

public class Data_class {
    private String name;
    private String password;
    private String dataImage;

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getDataImage() {
        return dataImage;
    }

    public Data_class(String name, String password, String dataImage) {
        this.name = name;
        this.password = password;
        this.dataImage = dataImage;
    }
}
