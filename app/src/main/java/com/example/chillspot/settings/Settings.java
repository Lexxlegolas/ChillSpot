package com.example.chillspot.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chillspot.R;
import com.example.chillspot.SetUp;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity
{
    private Toolbar toolbar;
    private CircleImageView imageSettings;
    private EditText status_S,userName_S, fullName_s, country_s, gender_s, relationship_s, dob_s;
    private Button update_s;

    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private String currentUserId;
    private String checker = "";
    private Uri imageUri;
    private String myUrl = "";
    private StorageReference usersProfileImage;
    private StorageTask uploadTask;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        usersProfileImage = FirebaseStorage.getInstance().getReference().child("Profile Images");

        initializeFields();

        userRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String image = dataSnapshot.child("image").getValue().toString();
                    String country = dataSnapshot.child("country").getValue().toString();
                    String dob = dataSnapshot.child("dob").getValue().toString();
                    String fullName = dataSnapshot.child("fullName").getValue().toString();
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    String relationship = dataSnapshot.child("relationship").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String userName = dataSnapshot.child("userName").getValue().toString();

                    Picasso.get().load(image).placeholder(R.drawable.profile).into(imageSettings);
                    country_s.setText(country);
                    dob_s.setText(dob);
                    fullName_s.setText(fullName);
                    gender_s.setText(gender);
                    relationship_s.setText(relationship);
                    status_S.setText(status);
                    userName_S.setText(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        update_s.setOnClickListener(new View.OnClickListener()
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

        imageSettings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                checker = "clicked";
                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(Settings.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            imageSettings.setImageURI(imageUri);
        }
        else
        {
            Toast.makeText(Settings.this,"Error: Try Again",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.this,Settings.class));
            finish();
        }
    }

    private void userInfoSaved()
    {
        String status = status_S.getText().toString();
        String username = userName_S.getText().toString();
        String fullname = fullName_s.getText().toString();
        String country = country_s.getText().toString();
        String gender = gender_s.getText().toString();
        String relationship = relationship_s.getText().toString();
        String dob = dob_s.getText().toString();

        if (TextUtils.isEmpty(status) || TextUtils.isEmpty(username) || TextUtils.isEmpty(fullname) || TextUtils.isEmpty(country)
                || TextUtils.isEmpty(gender) || TextUtils.isEmpty(relationship) || TextUtils.isEmpty(dob))
        {
            Toast.makeText(this, "Please make sure all fields are entered", Toast.LENGTH_LONG).show();
        }
        else if(checker.equals("clicked"))
        {
            loadingBar.setTitle("Updating Account");
            loadingBar.setMessage("Just a moment ...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            uploadImage(status,username,fullname,country,gender,relationship,dob);
        }
    }

    private void uploadImage(final String status,final String username,final String fullname,final String country,final String gender,final String relationship,final String dob)
    {
        if (imageUri != null)
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
                            setUpMap.put("fullName",fullname);
                            setUpMap.put("userName",username);
                            setUpMap.put("image",myUrl);
                            setUpMap.put("country",country);
                            setUpMap.put("status",status);
                            setUpMap.put("gender",gender);
                            setUpMap.put("dob",dob);
                            setUpMap.put("relationship",relationship);

                            userRef.updateChildren(setUpMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(Settings.this, "Account Updated", Toast.LENGTH_LONG).show();
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(Settings.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });

                        }
                        else
                        {
                            String e = task.getException().getMessage();
                            Toast.makeText(Settings.this,"Error: " + e,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else
            {
                Toast.makeText(Settings.this,"Image is not selected",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateOnlyUserInfo()
    {
        String status = status_S.getText().toString();
        String username = userName_S.getText().toString();
        String fullname = fullName_s.getText().toString();
        String country = country_s.getText().toString();
        String gender = gender_s.getText().toString();
        String relationship = relationship_s.getText().toString();
        String dob = dob_s.getText().toString();

        if (TextUtils.isEmpty(status) || TextUtils.isEmpty(username) || TextUtils.isEmpty(fullname) || TextUtils.isEmpty(country)
        || TextUtils.isEmpty(gender) || TextUtils.isEmpty(relationship) || TextUtils.isEmpty(dob))
        {
            Toast.makeText(this, "Please make sure all fields are entered", Toast.LENGTH_LONG).show();
        }
        else
        {
            loadingBar.setTitle("Updating Account");
            loadingBar.setMessage("Just a moment ...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            HashMap<String, Object> setUpMap = new HashMap<>();
            setUpMap.put("uid",currentUserId);
            setUpMap.put("fullName",fullname);
            setUpMap.put("userName",username);
            setUpMap.put("country",country);
            setUpMap.put("status",status);
            setUpMap.put("gender",gender);
            setUpMap.put("dob",dob);
            setUpMap.put("relationship",relationship);

            userRef.updateChildren(setUpMap).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(Settings.this, "Account Updated", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        String e = task.getException().getMessage();
                        Toast.makeText(Settings.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void initializeFields()
    {
        toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingBar = new ProgressDialog(this);

        imageSettings = findViewById(R.id.settings_profile_image);
        status_S = findViewById(R.id.settings_status);
        userName_S = findViewById(R.id.settings_userName);
        fullName_s = findViewById(R.id.settings_fullName);
        country_s = findViewById(R.id.settings_country);
        gender_s = findViewById(R.id.settings_gender);
        relationship_s = findViewById(R.id.settings_relationship_status);
        dob_s = findViewById(R.id.settings_dob);

        update_s = findViewById(R.id.update_account_settings_button);
    }

}
