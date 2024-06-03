//working version of code na2es-ha bas eno ye update le time ago kol minute
package com.example.splitapplication.ui.home.modules;


import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.splitapplication.R;
import com.example.splitapplication.SocketSingleton;

import java.util.List;

import io.socket.client.Socket;


public class clanAdapter extends RecyclerView.Adapter<clanAdapter.ImageViewHolder> {
	private Context mContext;
	private List<clanGetSet> mclans;
	private OnItemClickListener mListener;
	private ProgressDialog pDialog;
	private String TAG = " clanAdapter: ";

	public Socket mSocket;

public String uid;
	public clanAdapter(Context context, List<clanGetSet> clanGetSets) {
		mContext = context;
		mclans = clanGetSets;
	}

	@Override
	public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(mContext).inflate(R.layout.clan_item, parent, false);
		return new ImageViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {
		final clanGetSet clanGetSetCurrent = mclans.get(position);


		SharedPreferences userSharedSettings = mContext.getSharedPreferences("Setting", MODE_PRIVATE);

		holder.userProfileName.setText(clanGetSetCurrent.getn());
		//holder.userProfileName.setCompoundDrawablesWithIntrinsicBounds(0, 0, clanGetSetCurrent.getIsThereUnreadMessages() ? R.drawable.baseline_notifications_none_24 : 0, 0);
		Log.d("clanGetSetCurrent.getIsThereUnreadMessages()",String.valueOf(clanGetSetCurrent.getIsThereUnreadMessages()));
		if (clanGetSetCurrent.getIsThereUnreadMessages())
		{
			holder.userProfileName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_notifications_none_24, 0);
		}
		else{
			holder.userProfileName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

		}
		if (clanGetSetCurrent.getIsChatFavorite()){
			holder.userFavBtn.setBackgroundResource(R.drawable.favorite_filled_red);
		}
		else{
			holder.userFavBtn.setBackgroundResource(R.drawable.favorite_not_filled);

		}



		mSocket = SocketSingleton.getInstance();
		//clanGetSet clanGetSetCurrentInHolder = mclans.get(position);
		holder.userFavBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				 uid=userSharedSettings.getString("uid","63a55e6d3a23b7a4b28590db");

				if (clanGetSetCurrent.getIsChatFavorite()){
					//holder.userFavBtn.setBackgroundResource(R.drawable.favorite_not_filled);

					mSocket.emit("removeChatFromFavorites",uid,clanGetSetCurrent.getChatId() );

				}
				else{
					//holder.userFavBtn.setBackgroundResource(R.drawable.favorite_filled_red);
					mSocket.emit("addChatToFavorites",uid,clanGetSetCurrent.getChatId() );
				}

				clanGetSetCurrent.setIsChatFavorite(!clanGetSetCurrent.getIsChatFavorite());

				// Update the button background
				if (clanGetSetCurrent.getIsChatFavorite()) {
					holder.userFavBtn.setBackgroundResource(R.drawable.favorite_filled_red);
				} else {
					holder.userFavBtn.setBackgroundResource(R.drawable.favorite_not_filled);
				}

				mSocket.emit("getChats", "63a55e6d3a23b7a4b28590db");

			}
		});

		Log.d("clanGetSetCurrent.getLastMessageTimeStamp()",clanGetSetCurrent.getLastMessageTimeStamp());
		Log.d("clanGetSetCurrent.getLastMessage()",clanGetSetCurrent.getLastMessage());
		holder.userProfileLastChatTime.setText(clanGetSetCurrent.getLastMessageTimeAgo());    //taghyir
		holder.userProfileLastChat.setText(clanGetSetCurrent.getLastMessage());  //taghyir
		if(mContext!=null) {
			Glide
					.with(mContext)
					.load(clanGetSetCurrent.getm())
					.thumbnail(0.01f)
					.apply(RequestOptions.circleCropTransform())
					.into(holder.userProfileImage);
		}
		holder.itemView.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick (View view){
				next(clanGetSetCurrent.getn(),clanGetSetCurrent.getm(),clanGetSetCurrent.getChatId(),position);
			}
		});

	}
	@Override
	public int getItemCount() {
		return mclans.size();
	}


	public class ImageViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener,
			View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener  {
		public TextView userProfileName,userProfileLastChat,userProfileLastChatTime;
		public ImageView userProfileImage;
		public Button userFavBtn;
//		private Socket mSocket;
		public ImageViewHolder(View itemView) {
			super(itemView);
			SharedPreferences userSharedSettings = mContext.getSharedPreferences("Setting", MODE_PRIVATE);

			userProfileImage = itemView.findViewById(R.id.userProfileImage);
			userProfileName = itemView.findViewById(R.id.userProfileName);
			userProfileLastChat = itemView.findViewById(R.id.userProfileLastChat);
			userProfileLastChatTime = itemView.findViewById(R.id.userProfileLastChatTime);
			userFavBtn=itemView.findViewById(R.id.fav_btn);
//			mSocket = SocketSingleton.getInstance();

//			int position =getAdapterPosition();
//			clanGetSet clanGetSetCurrentInHolder = mclans.get(position);
//

//
//			userFavBtn.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//
//					String uid=userSharedSettings.getString("uid","63a55e6d3a23b7a4b28590db");
//
//					if (clanGetSetCurrentInHolder.getIsChatFavorite()){
//						userFavBtn.setBackgroundResource(R.drawable.favorite_filled_red);
//
//						mSocket.emit("addChatToFavorites",uid,clanGetSetCurrentInHolder.getChatId() );
//
//					}
//					else{
//						userFavBtn.setBackgroundResource(R.drawable.favorite_not_filled);
//						mSocket.emit("addChatToFavorites",uid,clanGetSetCurrentInHolder.getChatId() );
//
//					}
//
//
//				}
//			});
			itemView.setOnClickListener(this);
			itemView.setOnCreateContextMenuListener(this);
		}

		@Override
		public void onClick(View v) {
			if (mListener != null) {
				int position = getAdapterPosition();
				if (position != RecyclerView.NO_POSITION) {
					mListener.onItemClick(position);
				}
			}
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
			menu.setHeaderTitle("Select Action");
			MenuItem Report = menu.add(Menu.NONE, 1, 1, "Report");
			MenuItem delete = menu.add(Menu.NONE, 2, 2, "Remove");

			Report.setOnMenuItemClickListener(this);
			delete.setOnMenuItemClickListener(this);
		}

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			if (mListener != null) {
				int position = getAdapterPosition();
				if (position != RecyclerView.NO_POSITION) {

					switch (item.getItemId()) {
						case 1:
							mListener.onGetReport(position);
							return true;
						case 2:
							mListener.onGetDeleteClick(position);
							return true;
					}
				}
			}
			return false;
		}

	}






	public interface OnItemClickListener {
		void onItemClick(int position);

		void onGetReport(int position);

		void onGetDeleteClick(int position);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mListener = listener;
	}



	public void next(String final_me,String new_Image_uri,String chatId,int position){
		final clanGetSet clanGetSetCurrent = mclans.get(position);
//		mSocket.emit("markChatAsRead",uid,clanGetSetCurrent.getChatId());
		Intent intent =new Intent(mContext, chatroom.class);
		intent.putExtra("user_nick", final_me);
		//intent.putExtra("user_name", clanGetSetCurrent.getKey());
		intent.putExtra("user_name", final_me);
		intent.putExtra("chatId",chatId );
		intent.putExtra("imaged", new_Image_uri);
		mContext.startActivity(intent);
	}
}
