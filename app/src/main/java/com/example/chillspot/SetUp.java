package com.example.chillspot;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chillspot.loginRegister.Register;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUp extends AppCompatActivity
{
    private Button saveBtn;
    private EditText fullName, userName, countryName;
    private CircleImageView profileImage;
    private String checker = "";
    private FirebaseAuth auth;
    private String currentUserId;
    private Uri imageUri;
    private String myUrl = "";
    private DatabaseReference rootRef;
    private StorageReference usersProfileImage;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        usersProfileImage = FirebaseStorage.getInstance().getReference().child("Profile Images");

        saveBtn = findViewById(R.id.save_setup);
        fullName = findViewById(R.id.name_setup);
        userName = findViewById(R.id.user_name_setup);
        countryName = findViewById(R.id.country_setup);
        profileImage = findViewById(R.id.profile_image_setup);

        saveBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checker.equals("clicked"))
                {
                    userInfoSaved();
                }
                else
                {
                    updateOnlyUserInfo();
                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                checker = "clicked";
                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(SetUp.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            profileImage.setImageURI(imageUri);
        }
        else
        {
            Toast.makeText(SetUp.this,"Error: Try Again",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SetUp.this,SetUp.class));
            finish();
        }
    }

    private void userInfoSaved()
    {
        String setName = fullName.getText().toString();
        String setUserName = userName.getText().toString();
        String setCountry = countryName.getText().toString();

        if (TextUtils.isEmpty(setName) || TextUtils.isEmpty(setUserName) || TextUtils.isEmpty(setCountry))
        {
            Toast.makeText(this, "Please make sure all fields are Entered", Toast.LENGTH_LONG).show();
        }
        else if (checker.equals("clicked"))
        {
            uploadImage(setName,setUserName,setCountry);
        }
    }

    private void uploadImage(final String setName,final String setUserName,final String setCountry)
    {
        if (imageUri != null)
        {
            final StorageReference fileref = usersProfileImage
                    .child(currentUserId + ".jpg");

            uploadTask = fileref.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation()
            {
                @Override
                public Object then(@NonNull Task task) throws Exception
                {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return fileref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        Uri downloadUrl = task.getResult();
                        myUrl= downloadUrl.toString();

                        HashMap<String, Object> setUpMap = new HashMap<>();
                        setUpMap.put("uid",currentUserId);
                        setUpMap.put("fullName",setName);
                        setUpMap.put("userName",setUserName);
                        setUpMap.put("country",setCountry);
                        setUpMap.put("image",myUrl);
                        setUpMap.put("status","Hey there, I am using ChillSpot");
                        setUpMap.put("gender","not selected");
                        setUpMap.put("dob","none");
                        setUpMap.put("relationship","none");

                        rootRef.child("Users").child(currentUserId).updateChildren(setUpMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            sendUserToMain();
                                        }
                                        else
                                        {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SetUp.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }
                    else
                    {
                        String e = task.getException().getMessage();
                        Toast.makeText(SetUp.this,"Error: " + e,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else
        {
            Toast.makeText(SetUp.this,"Image is not selected",Toast.LENGTH_SHORT).show();
        }
    }

    private void updateOnlyUserInfo()
    {
        String setName = fullName.getText().toString();
        String setUserName = userName.getText().toString();
        String setCountry = countryName.getText().toString();

        if (TextUtils.isEmpty(setName) || TextUtils.isEmpty(setUserName) || TextUtils.isEmpty(setCountry))
        {
            Toast.makeText(this, "Please make sure all fields are Entered", Toast.LENGTH_LONG).show();
        }
        else
        {
            HashMap<String, Object> setUpMap = new HashMap<>();
            setUpMap.put("uid",currentUserId);
            setUpMap.put("fullName",setName);
            setUpMap.put("userName",setUserName);
            setUpMap.put("country",setCountry);
            setUpMap.put("status","Hey there, I am using ChillSpot");
            setUpMap.put("gender","not selected");
            setUpMap.put("dob","none");
            setUpMap.put("relationship","none");

            rootRef.child("Users").child(currentUserId).updateChildren(setUpMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                sendUserToMain();
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(SetUp.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void sendUserToMain()
    {
        Intent i = new Intent(SetUp.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
