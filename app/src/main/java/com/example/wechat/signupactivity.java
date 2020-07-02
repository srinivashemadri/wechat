package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signupactivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference mReference;
    EditText email;
    EditText password;
    EditText password2;
    EditText name;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signupactivity);
        mAuth = FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        mReference=db.getReference();
        name= (EditText) findViewById(R.id.editText);
        email= (EditText) findViewById(R.id.email);
        password= (EditText) findViewById(R.id.password);
        password2= (EditText) findViewById(R.id.reenterpassword);
    }
    public void signup(View view)
    {
        ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected())
        {

            final String nm = name.getText().toString();
            final String em= email.getText().toString();
            String pw= password.getText().toString();
            String pw2= password2.getText().toString();
            if(pw.equals(pw2) && nm.length()!=0 && em.length()!=0 && pw.length()>=6) {
                mAuth.createUserWithEmailAndPassword(em, pw)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getApplicationContext(),"Sign-up success \n"+ "A Verificatio Email has been sent to"+em ,Toast.LENGTH_LONG).show();
                                        }
                                    });

                                    mReference.child("users").child(task.getResult().getUser().getUid()).child("email").setValue(email.getText().toString());
                                    mReference.child("users").child(task.getResult().getUser().getUid()).child("name").setValue(name.getText().toString());
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(nm).build();
                                    user.updateProfile(profileUpdates);
                                    movetologinpage();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Signup Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            else if(em.length() ==0 || nm.length()==0)
                Toast.makeText(this,"Email / Name can't be empty",Toast.LENGTH_SHORT).show();
            else if(pw.equals(pw2) && em.length()!=0 && pw.length()<6)
                Toast.makeText(this, "Password Length should be atleast 6 characters long", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "Passwords in both the fields must match", Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(this,"Network unAvailable", Toast.LENGTH_SHORT).show();

    }
    public void movetologinpage()
    {
        Intent intent= new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
