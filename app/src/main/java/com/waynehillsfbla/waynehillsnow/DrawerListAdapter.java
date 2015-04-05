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
 * Created by Kartik on 4/5/2015.
 */
public class DrawerListAdapter extends RecyclerView.Adapter<DrawerListAdapter.DrawerViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private String[] drawerListTitles;
    private int[] drawerListIcons;

    private String displayName;
    private String profilePicture;

    private boolean hasHeader;

    public DrawerListAdapter(String[] titles, int[] icons) {
        drawerListTitles = titles;
        drawerListIcons = icons;

        hasHeader = false;
    }

    public DrawerListAdapter(String[] titles, int[] icons, String name, String profilePictureURL) {
        drawerListTitles = titles;
        drawerListIcons = icons;
        displayName = name;
        profilePicture = profilePictureURL;

        hasHeader = true;
    }

    @Override
    public DrawerListAdapter.DrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
            public void onClick(View v) {
                if ((position == 1 && hasHeader) || (position == 0 && !hasHeader))
                    v.getContext().startActivity(new Intent(v.getContext(), GooglePlusSignIn.class));
                if ((position == 2 && hasHeader) || (position == 1 && !hasHeader))
                    v.getContext().startActivity(new Intent(v.getContext(), MyEvents.class));
                if ((position == 3 && hasHeader) || (position == 2 && !hasHeader))
                    v.getContext().startActivity(new Intent(v.getContext(), MainActivity.class));
                //TODO create a settings activity
            }
        });
        if (holder.holderId == 1) {
            holder.vRowText.setText(drawerListTitles[position - 1]);
            holder.vRowIcon.setImageResource(drawerListIcons[position - 1]);
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
        if (hasHeader)
            return position == 0;
        else
            return false;
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
