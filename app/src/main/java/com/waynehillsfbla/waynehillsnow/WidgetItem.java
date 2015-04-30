package com.waynehillsfbla.waynehillsnow;

/**
 * This class represents a Stack View Widget.
 */
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
