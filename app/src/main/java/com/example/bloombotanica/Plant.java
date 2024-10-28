package com.example.bloombotanica;

public class Plant {
    private String name;
    private int statusIcon; // Could also be a URI or resource ID if it's an image

    public Plant(String name, int statusIcon) {
        this.name = name;
        this.statusIcon = statusIcon;
    }

    public String getName() {
        return name;
    }

    public int getStatusIcon() {
        return statusIcon;
    }
}
