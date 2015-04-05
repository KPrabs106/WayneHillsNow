package com.waynehillsfbla.waynehillsnow;

/**
 * Created by Entity on 4/1/2015.
 */
public class GooglePlusUser {
    private static String name;
    private static String googleId;
    private static String profilePictureURL;

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        GooglePlusUser.name = name;
    }

    public static String getGoogleId() {
        return googleId;
    }

    public static void setGoogleId(String googleId) {
        GooglePlusUser.googleId = googleId;
    }

    public static String getProfilePictureURL() {
        return profilePictureURL;
    }

    public static void setProfilePictureURL(String profilePictureURL) {
        GooglePlusUser.profilePictureURL = profilePictureURL;
    }

    public static boolean isSet() {
        return name == null && googleId == null && profilePictureURL == null;
    }
}
