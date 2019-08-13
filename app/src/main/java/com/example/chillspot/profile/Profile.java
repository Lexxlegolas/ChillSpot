package com.example.chillspot.profile;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.chillspot.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity
{
    private CircleImageView myProfileImage;
    private TextView myUserName, myFullName, myStatus, myGender, myDob, myRelationship, myCountry;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

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

                    Picasso.get().load(image).placeholder(R.drawable.profile).into(myProfileImage);
                    myCountry.setText("Country: " + country);
                    myDob.setText(" D.O.B: " + dob);
                    myFullName.setText(fullName);
                    myGender.setText("Gender: " + gender);
                    myRelationship.setText("Relationship: " + relationship);
                    myStatus.setText(status);
                    myUserName.setText("@: " + userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields()
    {
        myProfileImage = findViewById(R.id.my_profile_pic);

        myUserName = findViewById(R.id.my_user_name);
        myFullName = findViewById(R.id.my_full_name);
        myStatus = findViewById(R.id.my_profile_status);
        myCountry = findViewById(R.id.my_country);
        myGender = findViewById(R.id.my_gender);
        myDob = findViewById(R.id.my_dob);
        myRelationship = findViewById(R.id.my_relationship_status);
    }
}
