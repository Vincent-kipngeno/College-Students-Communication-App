package com.example.college_students_communication_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.college_students_communication_app.Adapters.AddMembersAdapter;
import com.example.college_students_communication_app.databinding.ActivityAddMembersBinding;
import com.example.college_students_communication_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMembersActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityAddMembersBinding binding;

    private String groupId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private final ArrayList<User> users = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private AddMembersAdapter addMembersAdapter;

    Query sortedUsersQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMembersBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        groupId = getIntent().getExtras().get("groupId").toString();

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Add Participants");

        addMembersAdapter = new AddMembersAdapter(users, groupId);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        binding.contactsRecylerView.setLayoutManager(linearLayoutManager);
        binding.contactsRecylerView.setAdapter(addMembersAdapter);

        binding.fab.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        if (view == binding.fab){
            addMembersToGroup();
        }
    }

    private void addMembersToGroup() {
        List<User> selectedMembers = addMembersAdapter.getSelected();
        long time = System.currentTimeMillis();

        for (User member: selectedMembers) {
            Map<String, Object> membership = new HashMap<>();
            membership.put("groups/"+ groupId + "/" + member.getUid(), time);
            membership.put("users/"+ member.getUid() + "/" + groupId, time);
            RootRef.child("Memberships").updateChildren(membership);
        }

        Toast.makeText(AddMembersActivity.this, "Members added successfully.",
                Toast.LENGTH_SHORT).show();

        Intent mainIntent = new Intent(AddMembersActivity.this, GroupChatActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.putExtra("groupCode", groupId);
        mainIntent.putExtra("showHidden", true);
        startActivity(mainIntent);

        finish();
    }

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d("AddMembersActivity", "onChildAdded:" + dataSnapshot.getKey());

            if (dataSnapshot.exists()){

                User user = dataSnapshot.getValue(User.class);
                users.add(user);
                addMembersAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            if (dataSnapshot.exists()){
                User user = dataSnapshot.getValue(User.class);
                users.add(user);
                addMembersAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            if (dataSnapshot.exists()){
                String username = dataSnapshot.child("username").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String phone = dataSnapshot.child("phone").getValue().toString();
                String uid = dataSnapshot.child("uid").getValue().toString();

                User user = new User(uid, email, username, phone);
                users.add(user);
                addMembersAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w("AddMembersActivity", "postComments:onCancelled", databaseError.toException());
            Toast.makeText(AddMembersActivity.this, "Failed to load comments.",
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onStart()
    {
        super.onStart();

        users.clear();
        sortedUsersQuery = RootRef.child("Users")
                .orderByChild("username");
        sortedUsersQuery.addChildEventListener(childEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        sortedUsersQuery.removeEventListener(childEventListener);
    }
}