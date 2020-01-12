package com.example.airittestdemo.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.airittestdemo.LocalDBAdapter.SqliteDBAdapter;
import com.example.airittestdemo.Models.ContactListModel;
import com.example.airittestdemo.Models.OfflineContactListModel;
import com.example.airittestdemo.R;
import com.example.airittestdemo.Utilities.Connectivity;

import java.util.ArrayList;

public class CustomContactListAdapter extends RecyclerView.Adapter<CustomContactListAdapter.CustomViewHolder> {

    Context context;
    ArrayList<ContactListModel> contactListModelArrayList = new ArrayList<>();

    //OFFLINE Process
    OfflineContactListModel offlineContactListModel = new OfflineContactListModel();
    ArrayList<OfflineContactListModel> offlinePojoArrayList = new ArrayList<>();
    SqliteDBAdapter sqliteDBAdapter;

    public CustomContactListAdapter(Context context,ArrayList<ContactListModel> contactListModelArrayList,
                                    OfflineContactListModel offlineContactListModel,ArrayList<OfflineContactListModel> offlinePojoArrayList){
        this.context = context;
        this.contactListModelArrayList = contactListModelArrayList;
        this.offlineContactListModel = offlineContactListModel;
        this.offlinePojoArrayList = offlinePojoArrayList;
    }

    @NonNull
    @Override
    public CustomContactListAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customcontactlayout, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomContactListAdapter.CustomViewHolder holder, int position) {
        try{
            sqliteDBAdapter = new SqliteDBAdapter(context);
            if (Connectivity.isConnected(context)){

                Log.e("MjSongListAdapter","ArrayList Data "+contactListModelArrayList.get(position).toString());

                Glide
                        .with(context)
                        .load(contactListModelArrayList.get(position).getImage())
                        .into(holder.iv_personImage);

                holder.tv_contactPersonName.setText(contactListModelArrayList.get(position).getName());
                holder.tv_contactNumber.setText(contactListModelArrayList.get(position).getPhone());
            }else {
                offlineContactListModel = offlinePojoArrayList.get(position);

                Glide
                        .with(context)
                        .load(offlineContactListModel.getImage())
                        .into(holder.iv_personImage);

                holder.tv_contactPersonName.setText(offlineContactListModel.getName());
                holder.tv_contactNumber.setText(offlineContactListModel.getPhone());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (Connectivity.isConnected(context)){
            return contactListModelArrayList.size();
        }else {
            return offlinePojoArrayList.size();
        }
    }

    public void removeItem(int position) {
        if (Connectivity.isConnected(context)){
            contactListModelArrayList.remove(position);
            // notify the item removed by position
            // to perform recycler view delete animations
            // NOTE: don't call notifyDataSetChanged()
            notifyItemRemoved(position);
        }else {
            offlinePojoArrayList.remove(position);
            // notify the item removed by position
            // to perform recycler view delete animations
            // NOTE: don't call notifyDataSetChanged()
            notifyItemRemoved(position);
            sqliteDBAdapter.deleteItemPositionWise(position);
        }
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv_personImage;
        public TextView tv_contactPersonName;
        public TextView tv_contactNumber;
        public LinearLayout viewForeground;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_personImage = itemView.findViewById(R.id.iv_personImage);
            tv_contactPersonName = itemView.findViewById(R.id.tv_contactPersonName);
            tv_contactNumber = itemView.findViewById(R.id.tv_contactNumber);
            viewForeground = itemView.findViewById(R.id.viewForeground);
        }
    }
}
