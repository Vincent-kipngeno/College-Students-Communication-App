package com.example.college_students_communication_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.college_students_communication_app.databinding.ActivityGroupsListBinding;
import com.example.college_students_communication_app.models.Group;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;

public class GroupsListActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityGroupsListBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private Query sortedGroupsQuery;
    FirebaseRecyclerAdapter<Group, GroupsViewHolder> adapter;

    private boolean showHidden = false;

    @Override
    protected void onStart() {
        super.onStart();

        sortedGroupsQuery = RootRef.child("Groups").orderByChild("timeStamp");

        FirebaseRecyclerOptions<Group> options =
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
        };

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.groupsRecyclerView.setLayoutManager(linearLayoutManager);
        binding.groupsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupsListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        binding.addGroupButton.setOnClickListener(this);
        binding.fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == binding.addGroupButton || view == binding.fab){
            Intent mainIntent = new Intent(GroupsListActivity.this, CreateGroupActivity.class);
            startActivity(mainIntent);
        }
    }

    public class GroupsViewHolder extends RecyclerView.ViewHolder
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
    }

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
                    Toast.makeText(getApplication(), "Track Hidden", Toast.LENGTH_SHORT).show();
                }
        }

        return false;
    }
}