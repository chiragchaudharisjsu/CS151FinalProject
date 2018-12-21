package com.cyraptor.uketabs;

public class Tab {
    String tabImage;
    String tabName;
    String tabArtist;
    String tabFile;

    public Tab(String tabImage, String tabName, String tabArtist, String tabFile) {
        this.tabImage = tabImage;
        this.tabName = tabName;
        this.tabArtist = tabArtist;
        this.tabFile = tabFile;
    }

    public String getTabImage() {
        return tabImage;
    }

    public String getTabName() {
        return tabName;
    }

    public String getTabArtist() {
        return tabArtist;
    }

    public String getTabFile() {
        return tabFile;
    }
}