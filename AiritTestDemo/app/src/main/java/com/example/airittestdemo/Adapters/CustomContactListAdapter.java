package com.example.airittestdemo.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.airittestdemo.Models.ContactListModel;
import com.example.airittestdemo.R;

import java.util.ArrayList;

public class CustomContactListAdapter extends RecyclerView.Adapter<CustomContactListAdapter.CustomViewHolder> {

    Context context;
    ArrayList<ContactListModel> contactListModelArrayList = new ArrayList<>();

    public CustomContactListAdapter(Context context,ArrayList<ContactListModel> contactListModelArrayList){
        this.context = context;
        this.contactListModelArrayList = contactListModelArrayList;
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
            Log.e("MjSongListAdapter","ArrayList Data "+contactListModelArrayList.get(position).toString());

            Glide
                    .with(context)
                    .load(contactListModelArrayList.get(position).getImage())
                    .into(holder.iv_personImage);

            holder.tv_contactPersonName.setText(contactListModelArrayList.get(position).getName());
            holder.tv_contactNumber.setText(contactListModelArrayList.get(position).getPhone());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return contactListModelArrayList.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_personImage;
        TextView tv_contactPersonName;
        TextView tv_contactNumber;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_personImage = itemView.findViewById(R.id.iv_personImage);
            tv_contactPersonName = itemView.findViewById(R.id.tv_contactPersonName);
            tv_contactNumber = itemView.findViewById(R.id.tv_contactNumber);
        }
    }
}
