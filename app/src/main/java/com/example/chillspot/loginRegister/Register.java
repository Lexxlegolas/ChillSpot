package com.example.chillspot.loginRegister;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chillspot.MainActivity;
import com.example.chillspot.R;
import com.example.chillspot.SetUp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity
{
    private EditText email_r,password_r,confirmPassword_r;
    private Button createAccBtn;
    private FirebaseAuth auth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();

        initializeFields();

        createAccBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               createAccount();
            }
        });
    }

    private void initializeFields()
    {
        loadingBar = new ProgressDialog(this);
        email_r = findViewById(R.id.email_register);
        password_r = findViewById(R.id.password_register);
        confirmPassword_r = findViewById(R.id.confirm_password_reg);

        createAccBtn = findViewById(R.id.register_button);
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

    private void createAccount()
    {
        String email = email_r.getText().toString();
        String password = password_r.getText().toString();
        String confPassword = confirmPassword_r.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confPassword))
        {
            Toast.makeText(this, "Please make sure all fields are Entered", Toast.LENGTH_LONG).show();
        }
        else if (!password.equals(confPassword))
        {
            Toast.makeText(this, "Make Sure Password Is Correct", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Creating Account");
            loadingBar.setMessage("Just a moment ...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                Intent i = new Intent(Register.this, SetUp.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                                loadingBar.dismiss();

                            }
                            else
                            {
                                String e = task.getException().toString();
                                Toast.makeText(Register.this, "Error: " +e, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }


    }

    private void sendUserToMain()
    {
        Intent i = new Intent(Register.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

}
