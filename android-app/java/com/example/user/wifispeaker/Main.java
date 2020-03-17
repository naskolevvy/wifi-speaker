package com.example.user.wifispeaker;

import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main extends ListActivity {

    ListView listView;
    EditText editText;
    Button del, add;
    ImageView play,stop;
    boolean isConnected;
    int lastPosition = -1;

    ArrayList<String> listItems = new ArrayList<>();
    Map<String, String> playlist = new LinkedHashMap<>();

    ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        refresh();

        del = (Button) findViewById(R.id.button2);
        add = (Button) findViewById(R.id.button);
        play = (ImageView) findViewById(R.id.imageView1); // i tuka AAAAAAAAAAAA
        stop = (ImageView) findViewById(R.id.imageView2);//opaaaaa kvo stana eeee
        listView = (ListView) findViewById(android.R.id.list);
        editText = (EditText) findViewById(R.id.editText);


        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice,
                listItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                if(lastPosition == pos) {
                    listView.clearChoices();
                    adapter.notifyDataSetChanged();
                    lastPosition = -1;
                }else{
                    lastPosition = pos;
                }
            }

        });

        stop.setOnClickListener(new View.OnClickListener() { // i tva az go adnah AAAAAAAAAAAAA
            @Override
            public void onClick(View v) {

                if (connectionCheck()) {
                    //we are connected to a network
                    Stop();
                } else
                    Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
                }
        });

        play.setOnClickListener(new View.OnClickListener() { // i tva az go adnah AAAAAAAAAAAAA
            @Override
            public void onClick(View v) {

                if (connectionCheck()) {
                    //we are connected to a network
                    if (adapter.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Nothing to Play!", Toast.LENGTH_SHORT).show();
                    } else {
                        Play();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (connectionCheck()) {
                    if (adapter.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Nothing to Delete!", Toast.LENGTH_SHORT).show();
                    } else {
                        Delete();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void  Stop() { // i tvaaaaaaaaaaa AAAAAAAAAAAAAAa

        Toast.makeText(getApplicationContext(), "Stopped!", Toast.LENGTH_SHORT).show();
        new NetworkOperation().execute("STOP");
    }

    private void Play(){ //tuka az go adnah AAAAAAAAAAAAAAAAa
        int pos = listView.getCheckedItemPosition();
        if (pos > -1) {
            //add.setEnabled(false);
            //del.setEnabled(false);
            String id = "";
            String name = "";
            for(Map.Entry<String, String> entry : playlist.entrySet()) {
                if(entry.getValue().contentEquals(listItems.get(pos))) {
                    id = entry.getKey();
                    name = entry.getValue();
                }
            }

            new NetworkOperation().execute("PLAY",id);
            listView.clearChoices();
            adapter.notifyDataSetInvalidated();   //tva she se mahne kato mine prez network operationa mislq che
            Toast.makeText(getApplicationContext(), "Currently Playing: "+ name, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "Select a song!", Toast.LENGTH_SHORT).show();
        }

    }


    private void Delete() {

        int pos = listView.getCheckedItemPosition();
        if (pos > -1) {
            add.setEnabled(false);
            del.setEnabled(false);
            String id = "";
            for(Map.Entry<String, String> entry : playlist.entrySet()) {
                if(entry.getValue().contentEquals(listItems.get(pos))) {
                    id = entry.getKey();
                }
            }
            new NetworkOperation().execute("REMOVE",id);
            listView.clearChoices();
            editText.setText("");
            Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
        }

    }

    public void AddItems(View view) {
        String song_link = editText.getText().toString();

        if(!song_link.isEmpty()) {

            if (connectionCheck()) {

            /* if we have internet */
                //malko poveche validation na linka :D znam che ne te kefi ama karai
                if(song_link.length() <= 43 && song_link.contains("https://www.youtube.com/watch?v=") || song_link.length() <= 28 && song_link.contains("https://youtu.be/")) {
                    song_link = song_link.replace("https://www.youtube.com/watch?v=", "");
                    song_link = song_link.replace("https://youtu.be/", "");


                    if(!playlist.keySet().contains(song_link)) {
                        add.setEnabled(false);
                        del.setEnabled(false);
                        new NetworkOperation().execute("ADD",song_link);
                        listView.clearChoices();
                        Toast.makeText(getApplicationContext(), "Added successfully!", Toast.LENGTH_SHORT).show();

                    }else{
                        Toast.makeText(getApplicationContext(), "This link already exist!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Invalid link!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Nothing to add!", Toast.LENGTH_SHORT).show();
        }
        editText.setText("");
    }
    public void VolumeUP(View view) {

        if(connectionCheck()) {
            new NetworkOperation().execute("UP");
        }else{
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }

    }

    public void VolumeDown(View view) {

        if(connectionCheck()) {
            new NetworkOperation().execute("DOWN");
        }else{
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean connectionCheck() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    public void refresh() {
        Thread listenerThread = new Thread(new Runnable() {
            public void run() {
                Socket socket = null;
                try {
                    socket = new Socket("164.132.56.199", 1081);
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    isConnected = true;

                    String content;
                    while (true) {
                        content = null;
                        content = br.readLine();
                        if (content == null) {  // disconnected from the server
                            socket.close();
                            break;
                        }
                        if(content.contains("INVALID")) {
                            final String finalContent = content;
                            runOnUiThread(new Runnable() {
                                              @Override
                                              public void run() {
                                                  String id = finalContent.split(" ")[1];
                                                  Toast.makeText(getApplicationContext(), "The song "+playlist.get(id)+" is protected with copyright and cannot be played! Please use another link.", Toast.LENGTH_LONG).show();
                                              }
                                          });
                        }else{
                            JSONArray json = null;
                            try {
                                playlist.clear();
                                json = new JSONArray(content);
                                for(int i = 0; i < json.length(); i++) {
                                    String id = json.getJSONObject(i).get("id").toString();
                                    String title = json.getJSONObject(i).get("title").toString();
                                    System.out.printf("SONG %d: %s | %s\n", i+1, id, title);
                                    playlist.put(id, title);
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.clear();
                                        adapter.addAll(playlist.values());
                                        adapter.notifyDataSetChanged();
                                        del.setEnabled(true);
                                        add.setEnabled(true);
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally{
                    isConnected = false;
                }
            }
        });
        listenerThread.start();
    }

    private class NetworkOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                String song_link = "";
                Socket socket = new Socket("164.132.56.199", 1080);
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);

                if(!isConnected) refresh(); // if the socket is not connected try to establish a connection

                if(params[0].contains("ADD")) {
                    song_link = params[1];
                    pw.print("GET /add/?id=" + song_link + " HTTP/1.0\r\n\n");
                }else if(params[0].contains("REMOVE")){
                    song_link = params[1];
                    pw.print("GET /remove/?id=" + song_link + " HTTP/1.0\r\n\n");
                }else if(params[0].contains("PLAY")){
                    song_link = params[1];
                    pw.print("GET /cmd/?cmd="+params[0]+"&id=" + song_link + " HTTP/1.0\r\n\n");
                }else{
                    pw.print("GET /cmd/?cmd="+params[0]+" HTTP/1.0\r\n\n");
                }

                pw.flush();
                socket.close();
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return "done";
        }
    }
}