package com.example.college_students_communication_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.college_students_communication_app.Adapters.GroupsAdapter;
import com.example.college_students_communication_app.databinding.ActivityGroupsListBinding;
import com.example.college_students_communication_app.models.Chat;
import com.example.college_students_communication_app.models.Group;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GroupsListActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityGroupsListBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private GroupsAdapter groupsAdapter;
    private final List<Group> groups = new ArrayList<>();

    private Query sortedGroupsQuery;

    private boolean showHidden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupsListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        groupsAdapter = new GroupsAdapter(GroupsListActivity.this, groups, showHidden);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        //linearLayoutManager.setStackFromEnd(true);
        binding.groupsRecyclerView.setLayoutManager(linearLayoutManager);
        binding.groupsRecyclerView.setAdapter(groupsAdapter);

        binding.addGroupButton.setOnClickListener(this);
        binding.fab.setOnClickListener(this);
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {

            if (snapshot.exists()){
                int last = groups.size();
                groups.clear();
                groupsAdapter.notifyItemRangeRemoved(0, last);

                for (DataSnapshot data: snapshot.getChildren()){
                    Group group = data.getValue(Group.class);

                    RootRef.child("Memberships").child("users").child(mAuth.getUid()).child(group.groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                if (groups.size() == 0){
                                    groups.add(group);
                                    groupsAdapter.notifyItemInserted(0);
                                }
                                else{
                                    if (groups.get(0).getTimeStamp() < group.getTimeStamp()){
                                        groups.add(0, group);
                                        groupsAdapter.notifyItemInserted(0);
                                    }
                                    else{
                                        groups.add(1, group);
                                        groupsAdapter.notifyItemInserted(1);
                                    }
                                }
                                groupsAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    ChildEventListener childEventListener = new ChildEventListener() {
        boolean groupExists = false;
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d("GroupsListActivity", "onChildAdded:" + dataSnapshot.getKey());

            if (dataSnapshot.exists()){
                int last = groups.size();
                groups.clear();
                groupsAdapter.notifyItemRangeRemoved(0, last);
                Group group = dataSnapshot.getValue(Group.class);

                RootRef.child("Memberships").child("groups").child(group.groupId).child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (groups.size() == last){
                                groups.add(group);
                            }
                            else {
                                groups.add(0, group);
                            }
                            groupsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            /*if (dataSnapshot.exists()){
                Group group = dataSnapshot.getValue(Group.class);

                RootRef.child("Memberships").child("groups").child(group.groupId).child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            int index = 0;

                            for (int i = groups.size()-1; i >= 0; i--) {
                                if (group.getGroupId().equals(groups.get(i).getGroupId())){
                                    index = i;
                                    groupExists = true;
                                    break;
                                }
                            }

                            if (groupExists){
                                //groups.remove(index);
                                //groupsAdapter.notifyItemRemoved(index);
                            }
                            groups.add(group);
                            groupsAdapter.notifyDataSetChanged();
                            groupExists = false;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }*/
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            /*if (dataSnapshot.exists()){

                Group group = dataSnapshot.getValue(Group.class);
                int index = 0;

                for (int i = groups.size()-1; i >= 0; i--) {
                    if (group.getGroupId().equals(groups.get(i).getGroupId())){
                        index = i;
                        groupExists = true;
                        break;
                    }
                }

                if (groupExists){
                    //groups.remove(index);
                    //groupsAdapter.notifyItemRemoved(index);
                }
                groups.add(group);
                groupsAdapter.notifyDataSetChanged();
                groupExists = false;
            }*/
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w("GroupsListActivity", "getGroups:onCancelled", databaseError.toException());
            Toast.makeText(GroupsListActivity.this, "Failed to load groups.",
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        sortedGroupsQuery = RootRef.child("Groups").orderByChild("timeStamp");
        sortedGroupsQuery.addValueEventListener(valueEventListener);
        binding.linearLayout.setVisibility(View.GONE);

        /*FirebaseRecyclerOptions<Group> options =
                new FirebaseRecyclerOptions.Builder<Group>()
                        .setQuery(sortedGroupsQuery, Group.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Group, GroupsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull GroupsViewHolder groupsViewHolder, int position, @NonNull Group group) {

                binding.linearLayout.setVisibility(View.GONE);

                RootRef.child("Memberships").child("groups").child(group.groupId).child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            groupsViewHolder.bindViews(group, dataSnapshot.getValue(Long.class));
                            //groupsViewHolder.itemView.findViewById(R.id.custom_group_layout).setVisibility(View.VISIBLE);
                            //groupsViewHolder.itemView.findViewById(R.id.custom_group_layout).setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        }
                        else {
                            //groupsViewHolder.itemView.findViewById(R.id.custom_group_layout).setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                            groupsViewHolder.itemView.findViewById(R.id.custom_group_layout).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }

            @Override
            public GroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.custom_group_layout, parent, false);

                return new GroupsViewHolder(view);
            }
        };*/
        //adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //adapter.stopListening();
        sortedGroupsQuery.removeEventListener(valueEventListener);
    }

    @Override
    public void onClick(View view) {
        if (view == binding.addGroupButton || view == binding.fab){
            Intent mainIntent = new Intent(GroupsListActivity.this, CreateGroupActivity.class);
            startActivity(mainIntent);
        }
    }

    /*public class GroupsViewHolder extends RecyclerView.ViewHolder
    {

        private View itemView;
        private TextView groupName;
        private TextView latestMessageTxt;
        private TextView receivedMessageTime;
        private TextView unreadMessages;
        private ImageView profileImg;

        public GroupsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            this.itemView = itemView;
            groupName = itemView.findViewById(R.id.groupNameTxt);
            receivedMessageTime = itemView.findViewById(R.id.receivedMessageTime);
            profileImg  = itemView.findViewById(R.id.profileImg);
            latestMessageTxt = itemView.findViewById(R.id.latestMessageTxt);
            unreadMessages = itemView.findViewById(R.id.unreadMessages);
        }

        public void bindViews(Group group, long timeStamp){

            Query unreadMessagesQuery = RootRef.child("Messages").child(group.getGroupId())
                    .orderByChild("time").startAfter(timeStamp);

            unreadMessagesQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long unreadMessagesCount = dataSnapshot.getChildrenCount();

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(group.getTimeStamp());

                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    String hm = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

                    receivedMessageTime.setText(hm);
                    //receivedMessageTime.setVisibility(group.isChecked() ? View.VISIBLE : View.GONE);
                    groupName.setText(group.getGroupName());
                    latestMessageTxt.setText(group.getLatestMessage());

                    if (unreadMessagesCount <= 0){
                        unreadMessages.setVisibility(View.GONE);
                    }
                    else {
                        unreadMessages.setText(String.format(Locale.getDefault(),"%d", unreadMessagesCount));
                        unreadMessages.setVisibility(View.VISIBLE);
                    }

                    String profileImage = TextUtils.isEmpty(group.getProfileImage())? "https://example.com" : group.getProfileImage();
                    Picasso.get()
                            .load(profileImage)
                            .placeholder(R.drawable.group)
                            .error(R.drawable.group)
                            .into(profileImg);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });



            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent mainIntent = new Intent(GroupsListActivity.this, GroupChatActivity.class);
                    mainIntent.putExtra("groupCode", group.getGroupId());
                    mainIntent.putExtra("showHidden", showHidden);
                    startActivity(mainIntent);
                }
            });
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logOut) {

            FirebaseAuth.getInstance().signOut();

            Intent mainIntent = new Intent(GroupsListActivity.this, LoginActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish();

            return true;
        }

        if (id == R.id.action_createGroup) {

            Intent mainIntent = new Intent(GroupsListActivity.this, CreateGroupActivity.class);
            startActivity(mainIntent);

            return true;
        }

        if (id == R.id.show_hidden_messages){
                showHidden = !showHidden;

                if (item.isChecked())
                {

                    item.setChecked(false);
                }
                else
                {
                    item.setChecked(true);
                    //Toast.makeText(getApplication(), "Track Hidden", Toast.LENGTH_SHORT).show();
                }
        }

        return false;
    }
}