package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class homeactivity extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference mReference;
    ArrayList<String> chatlist = new ArrayList<String>();
    ArrayList<String> keylist = new ArrayList<String>();
    ArrayList<String> onlinestatus=new ArrayList<String>();
    static List<HashMap<String,String>> listitems= new ArrayList<>();
    SimpleAdapter adapter;
    ListView listView;
    HashMap<String,String> result;
    boolean userfound;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homeactivity);
        mAuth = FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        mReference=db.getReference();
        listView= (ListView) findViewById(R.id.chatlists);
        userfound=false;
        showchatlists();
        LocalDateTime now = LocalDateTime.now();
        String sc= now.getDayOfMonth()+"/"+ now.getMonthValue() +"/"+ now.getYear() +"  "+ now.getHour() +":"+ now.getMinute();
        mReference.child("users").child(MainActivity.uid).child("isOnline").child("false").onDisconnect().setValue(sc);
        mReference.child("users").child(MainActivity.uid).child("isOnline").child("true").onDisconnect().setValue(false);
        ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(networkInfo!=null && networkInfo.isConnected()))
        {
            Toast.makeText(this,"Network unAvailable", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu );
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.newchat)
        {
            ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo!=null && networkInfo.isConnected())
            {
                LayoutInflater layoutInflater = LayoutInflater.from(homeactivity.this);
                View promptview = layoutInflater.inflate(R.layout.prompt, null);
                AlertDialog.Builder alertdialog = new AlertDialog.Builder(this);
                alertdialog.setView(promptview);
                final EditText email = (EditText)promptview.findViewById(R.id.emailprompt);
                alertdialog.setCancelable(false).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String s= email.getText().toString();
                        if(s!="" && networkInfo!=null && networkInfo.isConnected())
                        {
                            mReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot abc: dataSnapshot.getChildren())
                                    {
                                        String childemail = abc.child("email").getValue().toString();
                                        if(childemail.equals(s))
                                        {
                                            userfound=true;
                                            Toast.makeText(getApplicationContext(), "User Found, Opening Chat", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), chatactivity.class);
                                            //Toast.makeText(getApplicationContext(),abc.child("name").getValue().toString() , Toast.LENGTH_SHORT).show();
                                            intent.putExtra("nameofreciever",abc.child("name").getValue().toString());
                                            intent.putExtra("uidofreciever",abc.getKey());
                                            startActivity(intent);
                                            break;
                                        }
                                    }
                                    if(userfound==false)
                                        Toast.makeText(getApplicationContext(), "User Not Found,Please Enter Valid email\n or \n Ask your friend to signup to WECHAT to chat with him/her", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"Network unAvailable", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertdialog.create();
                alertdialog.show();
            }
            else
            {
                Toast.makeText(this,"Network unAvailable", Toast.LENGTH_SHORT).show();
            }


        }
        else if(item.getItemId()== R.id.myprofile)
        {
            Intent intent= new Intent(this, profileactivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.logout)
        {

            LocalDateTime now = LocalDateTime.now();
            String sl= now.getDayOfMonth() +"/"+ now.getMonthValue() +"/"+ now.getYear() +"  "+ now.getHour() +":"+ now.getMinute();
            mReference.child("users").child(MainActivity.uid).child("isOnline").child("true").setValue(false);
            mReference.child("users").child(MainActivity.uid).child("isOnline").child("false").setValue(sl);
            mAuth.signOut();
            MainActivity.email.setText("");
            MainActivity.password.setText("");
            Intent intent= new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    public void showchatlists()
    {
        chatlist.clear();
        keylist.clear();
        onlinestatus.clear();
        listitems.clear();
        adapter= new SimpleAdapter(this,listitems,R.layout.friendslist,
            new String[]{"username","onlinestatus"}, new int[]{R.id.username, R.id.onlinestatus});
        listView.setAdapter(adapter);
        mReference.child("users").child(MainActivity.uid).child("withwhomchated").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //chatlist.add(dataSnapshot.getValue().toString());
                //String uid = dataSnapshot.getValue().toString();
                final String name= dataSnapshot.child("name").getValue().toString();
                String uid = dataSnapshot.child("uidofreciever").getValue().toString();
                //Toast.makeText(getApplicationContext(),uid,Toast.LENGTH_SHORT).show();
                chatlist.add(name);
                //Toast.makeText(getApplicationContext(),name,Toast.LENGTH_SHORT).show();
                keylist.add(uid);
                HashMap<String,String> x= new HashMap<>();
                listitems.add(x);
                mReference.child("users").child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("isOnline").child("true").getValue().toString().equals("true")) {
                            result= new HashMap<>();
                            result.put("username",dataSnapshot.child("name").getValue().toString());
                            result.put("onlinestatus", "Online");

                            int idx=-1;
                            for(int i=0;i<keylist.size();i++)
                            {
                                if(dataSnapshot.getKey().equals(keylist.get(i)))
                                {
                                    idx=i;
                                    listitems.set(i,result);
                                    break;
                                }
                            }
                            if(idx==-1)
                                listitems.add(result);
                            adapter.notifyDataSetChanged();
                            //Toast.makeText(getApplicationContext(),result.toString(),Toast.LENGTH_SHORT).show();
                        }
                        else {
                            result= new HashMap<>();
                            result.put("username",dataSnapshot.child("name").getValue().toString());
                            String lastseen= dataSnapshot.child("isOnline").child("false").getValue().toString();
                            result.put("onlinestatus", "lastseen at "+lastseen);

                            int idx=-1;
                            for(int i=0;i<keylist.size();i++)
                            {
                                if(dataSnapshot.getKey().equals(keylist.get(i)))
                                {
                                    idx=i;
                                    listitems.set(i,result);
                                    break;
                                }
                            }
                            if(idx==-1)
                                listitems.add(result);
                            adapter.notifyDataSetChanged();
                            //Toast.makeText(getApplicationContext(),result.toString(),Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent= new Intent(getApplicationContext(), chatactivity.class);
                //Toast.makeText(getApplicationContext(),"Name="+chatlist.get(position)+"\n"+ "uidofreciever="+keylist.get(position), Toast.LENGTH_LONG).show();
                intent.putExtra("nameofreciever", listitems.get(position).get("username"));
                intent.putExtra("uidofreciever",keylist.get(position));
                intent.putExtra("position",position);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onBackPressed() {
        mAuth.signOut();
        Intent intent= new Intent(this, MainActivity.class);
        startActivity(intent);
        LocalDateTime now = LocalDateTime.now();
        String sbp= now.getDayOfMonth() +"/"+ now.getMonthValue() +"/"+ now.getYear() +"  "+ now.getHour() +":"+ now.getMinute();
        mReference.child("users").child(MainActivity.uid).child("isOnline").child("true").setValue(false);
        mReference.child("users").child(MainActivity.uid).child("isOnline").child("false").setValue(sbp);
        super.onBackPressed();
    }
}
