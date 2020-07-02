package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class chatactivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference mReference;
    EditText message;
    ListView listView;
    String uid ;
    String uidofreciever;
    String nameofreciever;
    String emailofsender;
    ArrayList<String> messages = new ArrayList<String>();
    ArrayList<String> head = new ArrayList<String>();
    ArrayList<String> time = new ArrayList<String>();
    boolean withwhomchated;
    boolean withwhomchated2;
    private final String CHANNEL_ID = "personal_notifications";
    private final int NOTIFICATION_ID=001;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatactivity);
        mAuth = FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        mReference=db.getReference();
        message = (EditText) findViewById(R.id.Typemessage);
        listView = (ListView) findViewById(R.id.listview);
        Intent intent= getIntent();
        nameofreciever= intent.getStringExtra("nameofreciever");
        int pos= intent.getIntExtra("position",0);
        String lastseen = "";
        if(homeactivity.listitems.size()>0)
            lastseen= homeactivity.listitems.get(pos).get("onlinestatus");
        TextView username3= (TextView) findViewById(R.id.username);
        username3.setText(nameofreciever + "\n "+lastseen );
        checkforupdates();
        ConnectivityManager connectivityManager;
        connectivityManager= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(networkInfo!=null && networkInfo.isConnected()))
            Toast.makeText(this, "Network Unavailable", Toast.LENGTH_SHORT).show();

    }
    public void checkforupdates()
    {

        messages.clear();
        head.clear();
        time.clear();
        final List<HashMap<String,String>> listitems= new ArrayList<>();
        final SimpleAdapter adapter= new SimpleAdapter(this,listitems, R.layout.listitem,
                new String[]{"sendermsg","sendertime","memsg","metime"}, new int[]{R.id.sendermsg,R.id.sendertime,R.id.memsg,R.id.metime} );
        listView.setAdapter(adapter);
        uid =MainActivity.uid;
        Intent intent= getIntent();
        String emailid = MainActivity.emailid;
        uidofreciever = intent.getStringExtra("uidofreciever");
        String result= uid+uidofreciever;
        char resultarray[] = result.toCharArray();
        Arrays.sort(resultarray);
        result= new String(resultarray);
        mReference.child("chats").child(result).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //messages.add(dataSnapshot.getValue().toString());
                String uid23= dataSnapshot.child("uid").getValue().toString();
                String msg= dataSnapshot.child("Message").getValue().toString();
                String tim= dataSnapshot.child("Time").getValue().toString();
                messages.add(msg);
                head.add(uid23);
                time.add(tim);
                int i=head.size()-1;
                HashMap<String, String> result= new HashMap<>();
                String h= head.get(i);
                String m= messages.get(i);
                String t= time.get(i);
                //Toast.makeText(getApplicationContext(),"h="+h,Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(),"nameofreciever="+nameofreciever,Toast.LENGTH_SHORT).show();
                if(h.equals(uidofreciever))
                {

                    /*createNotificationChannel();
                    NotificationCompat.Builder builder= new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID);
                    builder.setSmallIcon(R.drawable.ic_sms_black_24dp);
                    builder.setContentTitle(nameofreciever+ "Has sent you a message");
                    builder.setContentText(m);
                    builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    NotificationManagerCompat notificationManagerCompat=  NotificationManagerCompat.from(getApplicationContext());
                    notificationManagerCompat.notify(NOTIFICATION_ID,builder.build());*/
                    result.put("sendermsg", m);
                    result.put("sendertime",t);

                }
                else
                {
                    result.put("memsg",m);
                    result.put("metime",t);
                }
                    listitems.add(result);
                adapter.notifyDataSetChanged();
            }
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendmessage(View view) {

        final String mess = message.getText().toString();
        if(mess.length() == 0)
        {
            Toast.makeText(this,"can't Send empty message",Toast.LENGTH_SHORT).show();
            return;
        }
        message.setText("");
        uid =MainActivity.uid;
        Intent intent= getIntent();
        String emailid = MainActivity.emailid;
        uidofreciever = intent.getStringExtra("uidofreciever");
        String result= uid+uidofreciever;
        char resultarray[] = result.toCharArray();
        Arrays.sort(resultarray);
        result= new String(resultarray);
         withwhomchated= false;
         withwhomchated2=false;
         Map messageinfo= new HashMap();
        LocalDateTime now = LocalDateTime.now();
        int date= now.getDayOfMonth();
        int month=now.getMonthValue();
        int year=now.getYear();
        int hour= now.getHour();
        int minute= now.getMinute();
        String s= Integer.toString(date)+"/"+Integer.toString(month)+"/"+Integer.toString(year)+"  "+Integer.toString(hour)+":"+Integer.toString(minute);

         messageinfo.put("uid", MainActivity.uid);
         messageinfo.put("Message", mess);
         messageinfo.put("Time", s);
        mReference.child("chats").child(result).push().setValue(messageinfo);
        //FirebaseDatabase.getInstance().getReference().child("chats").child(result).push().setValue(MainActivity.currentusername+":\n"+mess);
        mReference.child("users").child(uid).child("withwhomchated").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Toascodet.makeText(getApplicationContext(), "In chatactivity.java \n"+dataSnapshot.child("uidofreciever").getValue().toString() + "\n" + "uid of reciever = "+ uidofreciever , Toast.LENGTH_SHORT).show();
                if(dataSnapshot.child("uidofreciever").getValue().toString().equals(uidofreciever))
                    withwhomchated=true;
            }
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        mReference.child("users").child(uid).child("withwhomchated").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(withwhomchated == false)
                {
                    Intent intent2= getIntent();
                    String name = intent2.getStringExtra("nameofreciever");
                    HashMap<String,String> data = new HashMap<String,String>();
                    data.put("name",name);
                    data.put("uidofreciever", uidofreciever);
                    mReference.child("users").child(uid).child("withwhomchated").push().setValue(data);
                    data.clear();
                    data.put("name", MainActivity.currentusername);
                    data.put("uidofreciever", MainActivity.uid);
                   mReference.child("users").child(uidofreciever).child("withwhomchated").push().setValue(data);
                    data.clear();
                }
            }
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
    public void goback(View view)
    {
        Intent go1= new Intent(this,homeactivity.class);
        startActivity(go1);
    }
    private void createNotificationChannel()
    {
        /*if(Build.VERSION.SDK_INT>=26)
        {
            CharSequence name="Personal Notifications";
            String description ="Include all the personal notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel= new NotificationChannel(CHANNEL_ID, name, importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }*/
    }
}
