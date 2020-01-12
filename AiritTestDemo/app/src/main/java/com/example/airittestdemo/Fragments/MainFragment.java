package com.example.airittestdemo.Fragments;


import android.app.ProgressDialog;
import android.content.ClipData;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.airittestdemo.Adapters.CustomContactListAdapter;
import com.example.airittestdemo.LocalDBAdapter.SqliteDBAdapter;
import com.example.airittestdemo.Models.ContactListModel;
import com.example.airittestdemo.Models.OfflineContactListModel;
import com.example.airittestdemo.R;
import com.example.airittestdemo.RecyclerViewDecorator.RecyclerItemTouchHelper;
import com.example.airittestdemo.RetroApiClient.RetrofitApiUtils;
import com.example.airittestdemo.SubUrlInterface.ApiInterface;
import com.example.airittestdemo.Utilities.CallingImportantMethod;
import com.example.airittestdemo.Utilities.Connectivity;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    RecyclerView rv_contactList;
    private ApiInterface iapiInterface;
    private RetrofitApiUtils retrofitApiUtils;
    ContactListModel contactListModel;
    CustomContactListAdapter customContactListAdapter;
    /**
     * Progress Dialog for delay Handle
     */
    ProgressDialog pd;
    ArrayList<ContactListModel> contactListModelArrayList;

    /**
    * Offline Process
    **/
    JSONArray jsonArrayInv;
    SqliteDBAdapter sqliteDBAdapter;
    OfflineContactListModel offlineContactListModel;
    ArrayList<OfflineContactListModel> offlinePojoArrayList;

    View parentLayout;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        initViews(rootView);

        return rootView;
    }

    private void initViews(View rootView){
        rv_contactList = rootView.findViewById(R.id.rv_contactList);
        parentLayout = rootView.findViewById(android.R.id.content);

        contactListModelArrayList = new ArrayList<>();

        //Offline Process
        jsonArrayInv = new JSONArray();
        sqliteDBAdapter = new SqliteDBAdapter(getActivity());
        offlinePojoArrayList = new ArrayList<>();

        if (!Connectivity.isConnected(getContext())){
            CallingImportantMethod.showToastError(getContext());
            getDataFromSqlite();
        }else {
            pd = new ProgressDialog(getContext());
            pd.setMessage("Loading..Please Wait.!");
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
            requestServerToFetchContactList();
        }

        // adding item touch helper
        // only ItemTouchHelper.LEFT added to detect Right to Left swipe
        // if you want both Right -> Left and Left -> Right
        // add pass ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT as param
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rv_contactList);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback1 = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Row is swiped from recycler view
                // remove it from adapter
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        // attaching the touch helper to recycler view
        new ItemTouchHelper(itemTouchHelperCallback1).attachToRecyclerView(rv_contactList);

    }

    /**
    * Calling Below method fetch data from server and populate it on Recyclerview
    **/
    private void requestServerToFetchContactList(){
        iapiInterface = retrofitApiUtils.getAPIService();
        iapiInterface.getContactListData().enqueue(new Callback<ArrayList<ContactListModel>>() {
            @Override
            public void onResponse(Call<ArrayList<ContactListModel>> call, Response<ArrayList<ContactListModel>> response) {
                try{
                    if (response.isSuccessful()){
                        ArrayList<ContactListModel> resultArrayList = response.body();
                        Log.e("Fragment","populating arraylist "+resultArrayList.toString());
                        for (int i=0; i<resultArrayList.size(); i++){
                            contactListModel = new ContactListModel();

                            contactListModel.setName(resultArrayList.get(i).getName());
                            contactListModel.setImage(resultArrayList.get(i).getImage());
                            contactListModel.setPhone(resultArrayList.get(i).getPhone());

                            insertDataIntoSqlite(resultArrayList.get(i).getName(), resultArrayList.get(i).getImage(),
                                    resultArrayList.get(i).getPhone());

                            contactListModelArrayList.add(contactListModel);
                        }
                        setDataOnRecyclerView();
                        pd.dismiss();
                    }else {
                        CallingImportantMethod.showToast(getContext(),"No Response Server");
                        pd.dismiss();
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                    pd.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<ContactListModel>> call, Throwable t) {
                CallingImportantMethod.showToast(getContext(),t.getMessage());
                pd.dismiss();
            }
        });
    }

    /**
    * Calling Method to Set Data on RecyclerView
    **/
    private void setDataOnRecyclerView(){
        rv_contactList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        rv_contactList.setLayoutManager(layoutManager);
        customContactListAdapter = new CustomContactListAdapter(getContext(),contactListModelArrayList,offlineContactListModel,offlinePojoArrayList);
        rv_contactList.setAdapter(customContactListAdapter);
        customContactListAdapter.notifyDataSetChanged();
    }

    /**
    * Below Method Inserting Data into local Database i.e. in Sqlite Database
    **/
    private void insertDataIntoSqlite(String personName, String contactImage, String contactNumber) {

        SqliteDBAdapter dbAdapter = new SqliteDBAdapter(getActivity());
        long result = dbAdapter.insertData(personName,contactImage,contactNumber);
        if (result > 0) {

            CallingImportantMethod.showToast(getActivity(), "Successfully Saved");

        }
    }

    /**
    * Calling method to fetch data from local database i.e. from SqliteDatabase
    **/
    private void getDataFromSqlite() {

        new AsyncTask<Object, Object, JSONArray>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pd = new ProgressDialog(getActivity(), R.style.AppTheme);
                pd.setCancelable(false);
                pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
                pd.show();
            }

            @Override
            protected JSONArray doInBackground(Object... params) {
                jsonArrayInv = sqliteDBAdapter.getDataList();       // Getting Data From Sqlite Databse
                Log.d("json",jsonArrayInv+"");

                return jsonArrayInv;
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            protected void onPostExecute(JSONArray jsonArray) {
                super.onPostExecute(jsonArray);

                Log.e("jsonArrayOne", jsonArray.toString());
                if (pd.isShowing()) {
                    pd.dismiss();

                    try {
                        for (int i=0;i<jsonArray.length();i++){
                            JSONObject sqliteDataObject = jsonArray.getJSONObject(i);
                            Log.e("sqliteDataObject", sqliteDataObject.toString());
                            offlineContactListModel = new OfflineContactListModel();

                            offlineContactListModel.setName(sqliteDataObject.getString("PersonName"));
                            offlineContactListModel.setImage(sqliteDataObject.getString("ContactImage"));
                            offlineContactListModel.setPhone(sqliteDataObject.getString("ContactNumber"));

                            offlinePojoArrayList.add(offlineContactListModel);
                        }

                        rv_contactList.setHasFixedSize(true);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                        rv_contactList.setLayoutManager(layoutManager);
                        customContactListAdapter = new CustomContactListAdapter(getContext(),contactListModelArrayList,offlineContactListModel,offlinePojoArrayList);
                        rv_contactList.setAdapter(customContactListAdapter);
                        customContactListAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }.execute();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (Connectivity.isConnected(getContext())){
            /*if (viewHolder instanceof CartListAdapter.MyViewHolder) {
                // get the removed item name to display it in snack bar
                String name = cartList.get(viewHolder.getAdapterPosition()).getName();

                // backup of removed item for undo purpose
                final ClipData.Item deletedItem = cartList.get(viewHolder.getAdapterPosition());
                final int deletedIndex = viewHolder.getAdapterPosition();

                // remove the item from recycler view
                mAdapter.removeItem(viewHolder.getAdapterPosition());

                // showing snack bar with Undo option
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, name + " removed from cart!", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // undo is selected, restore the deleted item
                        mAdapter.restoreItem(deletedItem, deletedIndex);
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }*/
        }else {
            if (viewHolder instanceof CustomContactListAdapter.CustomViewHolder) {
                // get the removed item name to display it in snack bar
                String name = offlinePojoArrayList.get(viewHolder.getAdapterPosition()).getName();

                // backup of removed item for undo purpose
                /*final Item deletedItem = offlinePojoArrayList.get(viewHolder.getAdapterPosition());
                final int deletedIndex = viewHolder.getAdapterPosition();*/

                // remove the item from recycler view
                customContactListAdapter.removeItem(viewHolder.getAdapterPosition());
                customContactListAdapter.notifyDataSetChanged();

                // showing snack bar with Undo option
                /*Snackbar snackbar = Snackbar
                        .make(parentLayout, name + " removed from cart!", Snackbar.LENGTH_LONG);*/
                /*snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // undo is selected, restore the deleted item
                        mAdapter.restoreItem(deletedItem, deletedIndex);
                    }
                });*/
                /*snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();*/
            }
        }
    }
}
