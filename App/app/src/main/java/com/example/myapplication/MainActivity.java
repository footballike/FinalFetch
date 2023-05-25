package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;

import com.example.myapplication.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ArrayList<String> dataList;
    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;
    ArrayAdapter<String> listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeUserList();

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new getData().start();
            }
        });

    }

    private void initializeUserList() {
        dataList = new ArrayList<>();
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        binding.dataList.setAdapter(listAdapter);

    }
    class getData extends Thread{
        @Override
        public void run() {

            try {
                URL url = new URL("https://fetch-hiring.s3.amazonaws.com/hiring.json");
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                InputStream in = con.getInputStream();
                BufferedReader buf = new BufferedReader(new InputStreamReader(in));
                String line;
                ArrayList<Integer> id = new ArrayList<>(); //Array of id's in order
                ArrayList<Integer> listId = new ArrayList<>(); //Array of listId's in order
                dataList.clear(); //Clears list incase of multiple button presses

                while ((line = buf.readLine()) != null) { //While there is still a line to read

                    String tempName = "id: ";

                    if (line.equals("[") || line.equals("]")) { //If it's the first or last line
                        continue;
                    }

                    JSONObject temp = new JSONObject(line);

                    if (temp.getString("name").equals("null") //If name is empty or null
                         || temp.getString("name").equals("")) {
                        continue;
                    }

                    tempName += temp.getString("id"); //Creates the string to be added to the list
                    tempName += " listId: ";
                    tempName += temp.getString("listId");
                    tempName += " Name: ";
                    tempName += temp.getString("name");

                    if (dataList.size() == 0) { //Add first string if list is empty
                        dataList.add(tempName);
                        id.add((Integer.parseInt(temp.getString("id"))));
                        listId.add((Integer.parseInt(temp.getString("listId"))));
                        continue;
                    }

                    for (int i = 0; i < dataList.size();i++) {
                        if (Integer.parseInt(temp.getString("listId")) == listId.get(i)) { //Sorts same listId based on name/id

                            if (Integer.parseInt(temp.getString("id")) < id.get(i) ){
                                    listId.add(i, Integer.parseInt(temp.getString("listId")));
                                    id.add(i, Integer.parseInt(temp.getString("id")));
                                    dataList.add(i, tempName);
                                    break;
                                }

                            if (i == dataList.size() - 1) { //If gets to end of list
                                    listId.add(i + 1, Integer.parseInt(temp.getString("listId")));
                                    id.add(i + 1, Integer.parseInt(temp.getString("id")));
                                    dataList.add(i + 1, tempName);
                                    break;
                                }
                        }
                        else if (Integer.parseInt(temp.getString("listId")) < listId.get(i)) { //If listId gets to end of the same listId's
                            listId.add(i, Integer.parseInt(temp.getString("listId")));
                            id.add(i, Integer.parseInt(temp.getString("id")));
                            dataList.add(i, tempName);
                            break;
                        }

                        else {

                            if (i == dataList.size() - 1) { //If it gets to the end of the list
                                listId.add(i + 1, Integer.parseInt(temp.getString("listId")));
                                id.add(i + 1, Integer.parseInt(temp.getString("id")));
                                dataList.add(i + 1, tempName);
                                break;
                            }
                        }



                    }

                }


            } catch (IOException e) {
                e.printStackTrace();
                dataList.add("IOException");
            } catch (JSONException e) {
                e.printStackTrace();
                dataList.add("JSONException");
            }

            mainHandler.post(new Runnable() {
                @Override
                public void run() {

                    listAdapter.notifyDataSetChanged();
                }
            });

        }
    }
}