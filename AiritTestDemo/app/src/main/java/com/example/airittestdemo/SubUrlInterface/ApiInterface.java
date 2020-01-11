package com.example.airittestdemo.SubUrlInterface;

import com.example.airittestdemo.Models.ContactListModel;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.ArrayList;

public interface ApiInterface {

    // API's For Stock Take Madule GET Fetch the list locations
    @GET("contacts.json")
    Call<ArrayList<ContactListModel>> getContactListData();

}
