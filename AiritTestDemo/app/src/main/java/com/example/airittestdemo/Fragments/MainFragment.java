package com.example.airittestdemo.Fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.airittestdemo.Adapters.CustomContactListAdapter;
import com.example.airittestdemo.Models.ContactListModel;
import com.example.airittestdemo.R;
import com.example.airittestdemo.RetroApiClient.RetrofitApiUtils;
import com.example.airittestdemo.SubUrlInterface.ApiInterface;
import com.example.airittestdemo.Utilities.CallingImportantMethod;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

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
        contactListModel = new ContactListModel();

        pd = new ProgressDialog(getContext());
        pd.setMessage("Loading..Please Wait.!");
        pd.setCancelable(false);
        pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        pd.show();
        requestServerToFetchContactList();
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
                        contactListModelArrayList = new ArrayList<>();
                        ArrayList<ContactListModel> resultArrayList = response.body();
                        for (int i=0; i<resultArrayList.size(); i++){
                            contactListModel.setName(resultArrayList.get(i).getName());
                            contactListModel.setImage(resultArrayList.get(i).getImage());
                            contactListModel.setPhone(resultArrayList.get(i).getPhone());

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
        customContactListAdapter = new CustomContactListAdapter(getContext(),contactListModelArrayList);
        rv_contactList.setAdapter(customContactListAdapter);
        customContactListAdapter.notifyDataSetChanged();
    }
}
