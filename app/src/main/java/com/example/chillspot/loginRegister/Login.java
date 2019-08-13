package com.example.chillspot.loginRegister;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chillspot.MainActivity;
import com.example.chillspot.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity
{
    private Button loginBtn;
    private EditText email_l,password_l;
    private TextView register_l;
    private ImageView google;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth=FirebaseAuth.getInstance();

        initializeFields();

        register_l.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendUserToRegister();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                allowUserLogin();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null)
        {
            sendUserToMain();
        }
    }

    private void initializeFields()
    {
        loginBtn = findViewById(R.id.login_button);

        email_l = findViewById(R.id.email_login);
        password_l = findViewById(R.id.password_login);

        register_l = findViewById(R.id.register_text_view);

        google = findViewById(R.id.google_login);
    }

    private void allowUserLogin()
    {
        String email = email_l.getText().toString();
        String password = password_l.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please make sure all fields are Entered", Toast.LENGTH_LONG).show();
        }
        else
        {
            auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                sendUserToMain();
                            }
                            else
                            {
                                String e = task.getException().getMessage();
                                Toast.makeText(Login.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void sendUserToMain()
    {
        Intent i = new Intent(Login.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void sendUserToRegister()
    {
        Intent i = new Intent(Login.this, Register.class);
        startActivity(i);
    }

}
