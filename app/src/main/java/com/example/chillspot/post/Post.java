package com.example.chillspot.post;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.chillspot.MainActivity;
import com.example.chillspot.R;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Post extends AppCompatActivity
{
    private Toolbar toolbar;
    private ImageButton selectPostImage;
    private Button updateBtn;
    private EditText postDesc;
    private Uri imageUri;
    private StorageReference postImagesRef;
    private FirebaseAuth auth;
    private String currentUserId, saveCurrentDate, saveCurrentTime, postRandomName;
    private String myUrl = "";
    private DatabaseReference rootRef;
    private StorageTask uploadTask;

    private static final int Gallary_pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        postImagesRef = FirebaseStorage.getInstance().getReference();
        rootRef = FirebaseDatabase.getInstance().getReference();

        selectPostImage = findViewById(R.id.select_post_image);
        updateBtn = findViewById(R.id.update_post_button);
        postDesc = findViewById(R.id.post_description);

        toolbar = findViewById(R.id.update_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        selectPostImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i,Gallary_pick);
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String desc = postDesc.getText().toString();

                if (imageUri == null)
                {
                    Toast.makeText(Post.this, "Please Select Image", Toast.LENGTH_LONG).show();
                }
                else if(TextUtils.isEmpty(desc))
                {
                    Toast.makeText(Post.this, "Please write something about your post", Toast.LENGTH_LONG).show();
                }
                else
                {
                    post(desc);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallary_pick && resultCode == RESULT_OK && data != null)
        {
            imageUri = data.getData();
            selectPostImage.setImageURI(imageUri);
        }

    }

    private void post(final String desc)
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        final StorageReference filePath = postImagesRef.child("Post Images").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");

        uploadTask = filePath.putFile(imageUri);

        uploadTask.continueWithTask(new Continuation()
        {
            @Override
            public Object then(@NonNull Task task) throws Exception
            {
                if (!task.isSuccessful())
                {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>()
        {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUrl = task.getResult();
                    myUrl = downloadUrl.toString();

                    rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String uName = dataSnapshot.child("userName").getValue().toString();
                                String prpfImage = dataSnapshot.child("image").getValue().toString();

                                HashMap postMap = new HashMap();
                                postMap.put("uid", currentUserId);
                                postMap.put("date", saveCurrentDate);
                                postMap.put("time", saveCurrentTime);
                                postMap.put("postImage", myUrl);
                                postMap.put("desc", desc);
                                postMap.put("profileImage", prpfImage);
                                postMap.put("userName", uName);

                                rootRef.child("Posts").child(currentUserId + postRandomName).updateChildren(postMap)
                                        .addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(Post.this, "Post Updated Successfully", Toast.LENGTH_LONG).show();
                                                    sendUserToMain();
                                                } else {
                                                    String e = task.getException().getMessage();
                                                    Toast.makeText(Post.this, "Error : " + e, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            sendUserToMain();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMain()
    {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
