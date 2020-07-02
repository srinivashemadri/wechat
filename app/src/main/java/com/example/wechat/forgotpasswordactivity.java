package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class forgotpasswordactivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference mReference;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpasswordactivity);
        mAuth = FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        mReference=db.getReference();
    }
    public void submit(final View view)
    {
        EditText email = (EditText) findViewById(R.id.email);
        String emailAddress = email.getText().toString();
        ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected()) {
            if(emailAddress!="") {
                mAuth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Password reset link has been sent to your email", Toast.LENGTH_LONG).show();
                                    moveback(view);
                                } else {
                                    Toast.makeText(getApplicationContext(), "You have not registered to WECHAT, First Register to enjoy WECHAT!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
            else
                Toast.makeText(this,"email can't be empty", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Network Unavailable", Toast.LENGTH_SHORT).show();
        }


    }
    public void moveback(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
