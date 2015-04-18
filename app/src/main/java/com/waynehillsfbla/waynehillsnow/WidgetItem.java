package com.waynehillsfbla.waynehillsnow;

/**
 * Created by Kartik on 4/8/2015.
 */
//TODO Add event id
public class WidgetItem {
    public String text;
    public String imageURL;
    public int eventId;

    public WidgetItem(String text, String imageURL, int eventId) {
        this.text = text;
        this.imageURL = imageURL;
        this.eventId = eventId;
    }
}
