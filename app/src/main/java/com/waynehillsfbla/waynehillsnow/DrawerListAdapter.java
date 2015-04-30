package com.waynehillsfbla.waynehillsnow;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * *********************************************
 * This class is an adapter for the navigation  *
 * drawer in the main activity.                 *
 * *********************************************
 */
public class DrawerListAdapter extends RecyclerView.Adapter<DrawerListAdapter.DrawerViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private String[] drawerListTitles;
    private int[] drawerListIcons;

    private String displayName;
    private String profilePicture;

    private boolean hasHeader;

    //This constructor is used if there isn't a signed in user and will not have a header
    public DrawerListAdapter(String[] titles, int[] icons) {
        drawerListTitles = titles;
        drawerListIcons = icons;

        hasHeader = false;
    }

    //This constructor is used if there is a signed in user and will have a header
    public DrawerListAdapter(String[] titles, int[] icons, String name, String profilePictureURL) {
        drawerListTitles = titles;
        drawerListIcons = icons;
        displayName = name;
        profilePicture = profilePictureURL;

        hasHeader = true;
    }

    @Override
    public DrawerListAdapter.DrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflates view depending on whether it's a normal row or the header
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_drawer_row, parent, false);

            DrawerViewHolder viewHolderItem = new DrawerViewHolder(v, viewType);

            return viewHolderItem;
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header, parent, false);

            DrawerViewHolder viewHolderHeader = new DrawerViewHolder(v, viewType);

            return viewHolderHeader;
        }
    }

    @Override
    public void onBindViewHolder(DrawerListAdapter.DrawerViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            //Clicking on a row in the drawer starts the appropriate activity
            public void onClick(View v) {
                if ((position == 1 && hasHeader) || (position == 0 && !hasHeader))
                    v.getContext().startActivity(new Intent(v.getContext(), GooglePlusSignIn.class));
                if ((position == 2 && hasHeader) || (position == 1 && !hasHeader))
                    v.getContext().startActivity(new Intent(v.getContext(), MyEventsActivity.class));
                if ((position == 3 && hasHeader) || (position == 2 && !hasHeader))
                    v.getContext().startActivity(new Intent(v.getContext(), CalendarActivity.class));
                if ((position == 4 && hasHeader) || (position == 3 && !hasHeader))
                    v.getContext().startActivity(new Intent(v.getContext(), LiveAtHillsActivity.class));
                if ((position == 5 && hasHeader) || (position == 4 && !hasHeader))
                    v.getContext().startActivity(new Intent(v.getContext(), SearchActivity.class));
            }
        });
        //The position needs to be adjusted by one, depending on whether or not there is a header
        if (holder.holderId == 1) {
            if (!hasHeader) {
                holder.vRowText.setText(drawerListTitles[position]);
                holder.vRowIcon.setImageResource(drawerListIcons[position]);
            } else {
                holder.vRowText.setText(drawerListTitles[position - 1]);
                holder.vRowIcon.setImageResource(drawerListIcons[position - 1]);
            }
        } else {
            holder.vDisplayName.setText(displayName);
            Picasso.with(holder.vContext).load(profilePicture).into(holder.vProfilePicture);
        }
    }

    @Override
    public int getItemCount() {
        if (hasHeader)
            return drawerListTitles.length + 1;
        else
            return drawerListTitles.length;
    }

    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;
        else
            return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return hasHeader && position == 0;
    }

    public static class DrawerViewHolder extends RecyclerView.ViewHolder {
        protected ImageView vProfilePicture;
        protected TextView vDisplayName;
        protected ImageView vRowIcon;
        protected TextView vRowText;
        protected Context vContext;
        int holderId;

        public DrawerViewHolder(View v, int ViewType) {
            super(v);
            vContext = v.getContext();

            if (ViewType == TYPE_ITEM) {
                vRowText = (TextView) v.findViewById(R.id.rowText);
                vRowIcon = (ImageView) v.findViewById(R.id.rowIcon);
                holderId = 1;
            } else {
                vProfilePicture = (ImageView) v.findViewById(R.id.profilePicture);
                vDisplayName = (TextView) v.findViewById(R.id.displayName);
                holderId = 0;
            }
        }
    }
}
