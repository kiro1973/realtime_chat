package com.example.splitapplication.ui.dashboard;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import com.example.splitapplication.MySocket;
import com.example.splitapplication.OnFragmentInteractionListener;
import com.example.splitapplication.R;
import com.example.splitapplication.SocketSingleton;
import com.example.splitapplication.databinding.FragmentDashboardBinding;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardFragment extends Fragment {
    public static final String currentUserPreference = "user_id_preference";
    String currentUserId;
    private Socket mSocket;
    private FragmentDashboardBinding binding;
    private OnFragmentInteractionListener mListener;




    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


//        try {
//            mSocket = IO.socket("http://10.0.2.2:3000");
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }

        //mSocket.connect();
        //mSocket = MySocket.getSocket();
//        BottomNavigationView navView = findViewById(R.id.nav_view);
//        BadgeDrawable badge = navView.getOrCreateBadge(R.id.navigation_home);
//        badge.setVisible(true);
//        badge.setText(" \uD83D\uDD14 ");
        mSocket = SocketSingleton.getInstance();
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Find your button by ID and set a click listener
        Button addButton = binding.addGeorge;
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the button click event
                onAddGeorgeButtonClick();
            }
        });

        return root;
    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onAddGeorgeButtonClick() {
        // Replace 'george_user_id' and 'George' with the actual user ID and name
        String userId = "63a55e6d3a23b7a4b28590db";
        String gName = "Saint George";

        // Emit a request to create a new chat with the userId and gName
        mSocket.emit("createChat", userId, gName);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Add a listener for the 'newChat' event from the server
        mSocket.on("newChat", onNewChat);
        mListener.onUIChange();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Remove the listener for the 'newChat' event when the fragment is not visible
        mSocket.off("newChat", onNewChat);
    }




//    public interface OnFragmentInteractionListener {
//        void onUIChange();
//    }



    private Emitter.Listener onNewChat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            // Handle the 'newChat' event from the server
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String chatId = data.getString("chatId");
                        String userId = data.getString("userId");

                        // Handle the new chat information on the UI as needed
                        // For example, display a toast or navigate to the chat screen
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}
