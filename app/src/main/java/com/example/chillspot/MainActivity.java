package com.example.chillspot;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chillspot.loginRegister.Login;
import com.example.chillspot.models.Posts;
import com.example.chillspot.post.ClickPost;
import com.example.chillspot.post.Post;
import com.example.chillspot.settings.Settings;
import com.example.chillspot.viewHolder.PostsViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private FirebaseAuth auth;
    private DatabaseReference usersRef, postRef;

    private CircleImageView profImage;
    private TextView nameuser;
    private String currentUserId;
    private ImageButton addNewPostBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        addNewPostBtn = findViewById(R.id.add_new_post_button);

        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigation_view);
        recyclerView = findViewById(R.id.all_users_recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        profImage = navView.findViewById(R.id.profile_image_nav);
        nameuser = navView.findViewById(R.id.user_name_nav);



        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("image"))
                    {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profImage);
                    }

                    if (dataSnapshot.hasChild("userName"))
                    {
                        String name = dataSnapshot.child("userName").getValue().toString();
                        nameuser.setText(name);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Profile image and userName do not exist", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                userMenuSelector(menuItem);
                return false;
            }
        });


        recyclerView = findViewById(R.id.all_users_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addNewPostBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendUserToPost();
            }
        });
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null)
        {
            sendUserToLogin();
        }
        else
        {
            checkUserExistanceInDatabase();
        }

        Query postDescendingOrder = postRef.orderByChild("counter");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(postDescendingOrder, Posts.class)
                .build();

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model)
            {
               final String postKey = getRef(position).getKey();

                holder.postUserName.setText(model.getUserName());
                holder.postDescription.setText(model.getDesc());
                holder.postTime.setText("  " + model.getTime());
                holder.postDate.setText("  " +model.getDate());

                Picasso.get().load(model.getProfileImage()).placeholder(R.drawable.profile).into(holder.postProfileImage);
                Picasso.get().load(model.getPostImage()).into(holder.postImage);

                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent i = new Intent(MainActivity.this, ClickPost.class);
                        i.putExtra("postKey",postKey);
                        startActivity(i);
                    }
                });

            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_posts_layout, viewGroup, false);
                PostsViewHolder holder = new PostsViewHolder(view);
                return holder;
            }
        };
            recyclerView.setAdapter(adapter);
            adapter.startListening();

    }

    private void checkUserExistanceInDatabase()
    {
        final String currentUserId = auth.getCurrentUser().getUid();

        usersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.hasChild(currentUserId))
                {
                    sendUserToSetUp();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void userMenuSelector(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.nav_post:
                sendUserToPost();
                break;
            case R.id.nav_profile:
                Toast.makeText(this, "profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Toast.makeText(this, "friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_friends:
                Toast.makeText(this, "find friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                sendUserToSetting();
                break;
            case R.id.nav_logout:
                auth.signOut();
                sendUserToLogin();
                break;
        }
    }

    private void sendUserToLogin()
    {
        Intent i = new Intent(MainActivity.this, Login.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void sendUserToSetUp()
    {
        Intent i = new Intent(MainActivity.this, SetUp.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void sendUserToPost()
    {
        Intent i = new Intent(this, Post.class);
        startActivity(i);
    }

    private void sendUserToSetting()
    {
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }




}
