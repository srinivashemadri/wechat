package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class profileactivity extends AppCompatActivity {

    TextView name;
    TextView email;
    TextView welcome;
    FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference mReference;
       protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profileactivity);
        name= (TextView) findViewById(R.id.editText4);
        email= (TextView) findViewById(R.id.email);
        mAuth = FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        mReference=db.getReference();
        welcome=(TextView) findViewById(R.id.welcome);
        String greeting= "Hello! "+mAuth.getCurrentUser().getDisplayName();
        welcome.setText(greeting);
        name.setText(mAuth.getCurrentUser().getDisplayName());
        email.setText(mAuth.getCurrentUser().getEmail());
    }
    public void savechanges(View view)
    {
        ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected()) {
            String cuser=FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            String editedname=name.getText().toString();

            if(!cuser.equals(editedname) && editedname.length()!=0) {
                String previousname = mAuth.getCurrentUser().getDisplayName();
                FirebaseUser user = mAuth.getCurrentUser();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(editedname).build();
                mReference.child("users").child(MainActivity.uid).child("name").setValue(editedname);
                user.updateProfile(profileUpdates);
                Toast.makeText(getApplicationContext(), "Name Changed Successfully", Toast.LENGTH_SHORT).show();
                welcome = (TextView) findViewById(R.id.welcome);
                String greeting = "Hello! " + editedname;
                welcome.setText(greeting);
            }
            else if(editedname.length()==0)
                Toast.makeText(getApplicationContext(),"Display Name can't be empty",Toast.LENGTH_SHORT).show();
            else
            {
                Toast.makeText(getApplicationContext(),"No Changes made",Toast.LENGTH_SHORT).show();
            }
        }
        else
            Toast.makeText(this, "Network Unavailable",Toast.LENGTH_SHORT).show();


    }
    public void deletemyaccount(View view)
    {
        ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected())
        {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_delete)
                    .setTitle("Are you sure you want to delete your account")
                    .setMessage("Deleting your account will permenantly deletes your chats, data and you can't restore them back")
                    .setPositiveButton("Delete My Account", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseUser user= mAuth.getCurrentUser();
                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        mReference.child("users").child(MainActivity.uid).removeValue();
                                        Toast.makeText(getApplicationContext(),"Your account has been successfully deleted",Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                        Toast.makeText(getApplicationContext(),"Some Error occured! Please try again after some time",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }
        else
            Toast.makeText(this,"Network Unavailable", Toast.LENGTH_SHORT).show();

    }
}
