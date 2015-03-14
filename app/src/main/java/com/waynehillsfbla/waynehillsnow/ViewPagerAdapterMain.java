package com.waynehillsfbla.waynehillsnow;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Kartik on 3/10/2015.
 */
public class ViewPagerAdapterMain extends FragmentStatePagerAdapter {
    CharSequence Titles[];
    int numTabs;

    public ViewPagerAdapterMain(FragmentManager fm, CharSequence Titles[], int numTabs) {
        super(fm);
        this.Titles = Titles;
        this.numTabs = numTabs;
    }

    public Fragment getItem(int position) {
        if (position == 0) {
            CalendarTab calendarTab = new CalendarTab();
            return calendarTab;
        } else {
            EventListTab eventListTab = new EventListTab();
            return eventListTab;
        }
    }

    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    public int getCount() {
        return numTabs;
    }
}
