package com.example.splitapplication;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BadgeUtils {
    public static void addBadge(BottomNavigationView bottomNavigationView, int menuItemId, View badgeView) {
        // Find the menu item view
        View menuItemView = bottomNavigationView.findViewById(menuItemId);

        if (menuItemView != null) {
            // Calculate badge position
            int badgeMargin = bottomNavigationView.getResources().getDimensionPixelSize(R.dimen.badge_margin);
            int badgeSize = bottomNavigationView.getResources().getDimensionPixelSize(R.dimen.badge_size);
            int x = menuItemView.getRight() - badgeSize / 2 - badgeMargin;
            int y = menuItemView.getTop() + badgeMargin;

            // Set badge layout params
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = x;
            params.topMargin = y;
            badgeView.setLayoutParams(params);

            // Add badge view to bottom navigation view
            bottomNavigationView.addView(badgeView);
        }
    }
}

