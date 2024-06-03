package com.example.splitapplication;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.splitapplication.ui.dashboard.DashboardFragment;
import com.example.splitapplication.ui.notifications.NotificationsFragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.splitapplication.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener{
    public static final String currentUserPreference = "user_id_preference";//this is userid saved in mongo It should be returned form server upon login
    private ActivityMainBinding binding;

 private Socket mSocket;

    BadgeDrawable badge;




    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter timeFilter =new IntentFilter(Intent.ACTION_TIME_TICK);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mSocket = SocketSingleton.getInstance();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

//        LayoutInflater inflater = LayoutInflater.from(this);
//        View badgeView = inflater.inflate(R.layout.badge_layout, navView, false);
//
//// Get the navigation home item ID
//        int homeItemId = navView.getMenu().findItem(R.id.navigation_dashboard).getItemId();
//
//// Add badge to the navigation home item
//        BadgeUtils.addBadge(navView, homeItemId, badgeView);

         //////////method 2/////////
        BottomNavigationView navView = findViewById(R.id.nav_view);
        badge = navView.getOrCreateBadge(R.id.navigation_home);
//        badge.setVisible(true);
//        badge.setText(" \uD83D\uDD14 ");

        badge.setBackgroundColor(getResources().getColor(R.color.white) );
           /////////method 3////////////////
//        BottomNavigationView navigationView = findViewById(R.id.nav_view);
//        Menu menu = navigationView.getMenu();
//        MenuItem menuItem = menu.findItem(R.id.navigation_home);
//        menu.findItem()
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View badgeView = inflater.inflate(R.layout.badge_layout, null);

       // MenuItemCompat.setActionView(menuItem, badgeView);

        SharedPreferences settings = getSharedPreferences(currentUserPreference, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("uid", "63a55e6d3a23b7a4b28590db");//for now it's static you have to retrieve it after login and authentication
        editor.putString("myimage", "https://handmadeiconsgreece.myshopify.com/cdn/shop/products/saint-yostos-el-antony-icon.jpg?v=1671954906");//for now it's static image you have to retrieve it after login and authentication

        editor.commit();
        mSocket.on("chats", onChatsReceived);
        mSocket.emit("getChats",settings.getString("uid","123"));

    }



    private Emitter.Listener onChatsReceived = args -> {
        Log.d("args[0] in main activity",args[0].toString());
        JSONArray chatListArray = (JSONArray) args[0];

        // Check if any chat has unread messages
        boolean[] hasUnreadMessages = {false};
        for (int i = 0; i < chatListArray.length(); i++) {
            try {
                JSONObject chatJson = chatListArray.getJSONObject(i);
                if (chatJson.getBoolean("isThereUnreadMessages")) {
                    hasUnreadMessages[0] = true;
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Update UI based on the presence of unread messages
        runOnUiThread(() -> {
            if (hasUnreadMessages[0]) {
                badge.setVisible(true);
                badge.setText(" \uD83D\uDD14 ");
                // Update UI to indicate unread messages
                // For example, show a badge or change the color of a UI element
            } else {
                badge.setVisible(false);
                badge.setText(" \uD83D\uDD14 ");
                // Update UI to indicate no unread messages
                // For example, hide the badge or revert the color change
            }
        });
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure to unregister the listener to prevent memory leaks
        mSocket.off("chats", onChatsReceived);
    }



    @Override
    public void onUIChange() {

    }
}