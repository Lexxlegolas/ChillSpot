package com.example.chillspot.post;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chillspot.MainActivity;
import com.example.chillspot.R;
import com.example.chillspot.SetUp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPost extends AppCompatActivity
{
    private ImageView postImageClicked;
    private TextView postDescClicked;
    private Button editClicked, deleteClicked;
    private String postKey, currentUserId, databaseUserId, desc, image;
    private DatabaseReference postRef, userRef;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        postKey = getIntent().getExtras().get("postKey").toString();
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        postImageClicked = findViewById(R.id.post_image_clicked);

        postDescClicked = findViewById(R.id.post_desc_clicked);

        editClicked = findViewById(R.id.edit_post_clicked);
        deleteClicked = findViewById(R.id.delete_post_clicked);

        editClicked.setVisibility(View.INVISIBLE);
        deleteClicked.setVisibility(View.INVISIBLE);

        postRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                     desc = dataSnapshot.child("desc").getValue().toString();
                     image = dataSnapshot.child("postImage").getValue().toString();

                    databaseUserId = dataSnapshot.child("uid").getValue().toString();

                    postDescClicked.setText(desc);
                    Picasso.get().load(image).into(postImageClicked);

                    if (currentUserId.equals(databaseUserId))
                    {
                        editClicked.setVisibility(View.VISIBLE);
                        deleteClicked.setVisibility(View.VISIBLE);
                    }

                    editClicked.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            editPost(desc);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        deleteClicked.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                deletePost();
                sendUserToMain();
                Toast.makeText(ClickPost.this, "Post Deleted Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editPost(String desc)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPost.this);
        builder.setTitle("Edit Post");

        final EditText inputField = new EditText(ClickPost.this);
        inputField.setText(desc);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                postRef.child("desc").setValue(inputField.getText().toString());
                Toast.makeText(ClickPost.this, "Post Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    private void deletePost()
    {
        postRef.removeValue();
    }

    private void sendUserToMain()
    {
        Intent i = new Intent(ClickPost.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
