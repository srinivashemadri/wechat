package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference mReference;
    static EditText email;
    static EditText password;
    static String uid;
    static String emailid;
    static String currentusername;
    SharedPreferences sharedPreferences;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        mReference=db.getReference();
        email= (EditText) findViewById(R.id.email);
        password= (EditText) findViewById(R.id.password);
        TextView clickhere= (TextView) findViewById(R.id.forgotpassword);
        String clickme="Forgot Password? Click Here";
        SpannableString ss = new SpannableString(clickme);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                forgotpassword();
            }
        };
        ss.setSpan(clickableSpan,17,27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        clickhere.setText(ss);
        clickhere.setMovementMethod(LinkMovementMethod.getInstance());
        sharedPreferences= this.getSharedPreferences("com.example.wechat", Context.MODE_PRIVATE);
        String sharedprefemail = sharedPreferences.getString("username","");
        String sharedprefpw = sharedPreferences.getString("password","");
        email.setText(sharedprefemail);
        password.setText(sharedprefpw);

    }
    public void Login(View view)
    {

        ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected()) {


            Log.i("Login", "Entered");
            String em = email.getText().toString();
            String pw = password.getText().toString();
            if ((em.length() == 0) || (pw.length() == 0)) {
                Toast.makeText(this, "Email/ password can't be empty", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Log.i("Email =", em);
                Log.i("Password =", pw);
                CheckBox checkBox= (CheckBox) findViewById(R.id.checkBox);
                if(checkBox.isChecked())
                {
                    sharedPreferences.edit().putString("username",em).apply();
                    sharedPreferences.edit().putString("password",pw).apply();
                }
                mAuth.signInWithEmailAndPassword(em, pw)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    String mail = user.getEmail();

                                    boolean isemailverified = user.isEmailVerified();
                                    //Toast.makeText(getApplicationContext(), Boolean.toString(isemailverified), Toast.LENGTH_LONG).show();
                                    if (isemailverified == true) {
                                        currentusername = user.getDisplayName();
                                        //Toast.makeText(getApplicationContext(),name+"\n"+mail,Toast.LENGTH_LONG).show();
                                        Log.i("Login", "Success");
                                        uid = task.getResult().getUser().getUid();
                                        emailid = email.getText().toString();
                                        Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();
                                        mReference.child("users").child(uid).child("isOnline").child("true").setValue(true);
                                        movetohomepage();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Your Email address is not verified, Verify your email to login", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Log.i("Login", "Failed");
                                    Toast.makeText(getApplicationContext(), "Username/ Password is incorrect", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
        else
        {
            Toast.makeText(this, "Network unAvailable", Toast.LENGTH_SHORT).show();
        }
    }
    public void signup(View view)
    {
        Intent intent= new Intent(this, signupactivity.class);
        startActivity(intent);
    }
    public void movetohomepage()
    {
        Intent intent = new Intent(this, homeactivity.class);
        startActivity(intent);
    }
    public  void forgotpassword()
    {
        Intent intent= new Intent(this, forgotpasswordactivity.class);
        startActivity(intent);
        String emailAddress = "exclusiveforcodechef@gmail.com";


    }

}
