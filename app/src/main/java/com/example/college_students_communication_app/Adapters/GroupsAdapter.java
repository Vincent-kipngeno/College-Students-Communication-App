package com.example.college_students_communication_app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.college_students_communication_app.GroupChatActivity;
import com.example.college_students_communication_app.R;
import com.example.college_students_communication_app.models.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder>
{
    private List<Group> groups;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUid;
    private DatabaseReference mRootRef;
    private Context context;
    public boolean showHidden;


    public GroupsAdapter(Context context, List<Group> groups, boolean showHidden)
    {
        this.groups = groups;
        this.context = context;
        this.showHidden = showHidden;
    }

    @NonNull
    @Override
    public GroupsAdapter.GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        View view;

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        currentUid = mAuth.getUid();

        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_group_layout, viewGroup, false);

        return new GroupsAdapter.GroupsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsAdapter.GroupsViewHolder groupsViewHolder, int position) {

        //binding.linearLayout.setVisibility(View.GONE);
        Group group = groups.get(position);
        groupsViewHolder.bindViews(group);
    }

    @Override
    public int getItemCount()
    {
        return groups.size();
    }

    public class GroupsViewHolder extends RecyclerView.ViewHolder
    {

        private View itemView;
        private TextView groupName;
        private TextView latestMessageTxt;
        private TextView receivedMessageTime;
        private TextView unreadMessages;
        private ImageView profileImg;
        private LinearLayout unreadMessagesLayout;

        public GroupsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            this.itemView = itemView;
            groupName = itemView.findViewById(R.id.groupNameTxt);
            receivedMessageTime = itemView.findViewById(R.id.receivedMessageTime);
            profileImg  = itemView.findViewById(R.id.profileImg);
            latestMessageTxt = itemView.findViewById(R.id.latestMessageTxt);
            unreadMessages = itemView.findViewById(R.id.unreadMessages);
            unreadMessagesLayout = itemView.findViewById(R.id.unreadMessagesLayout);
        }

        public void bindViews(Group group){

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(group.getTimeStamp());

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            String hm = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

            receivedMessageTime.setText(hm);
            //receivedMessageTime.setVisibility(group.isChecked() ? View.VISIBLE : View.GONE);
            groupName.setText(group.getGroupName());
            latestMessageTxt.setText(group.getLatestMessage());

            String profileImage = TextUtils.isEmpty(group.getProfileImage())? "https://example.com" : group.getProfileImage();
            Picasso.get()
                    .load(profileImage)
                    .placeholder(R.drawable.group)
                    .error(R.drawable.group)
                    .into(profileImg);

            unreadMessagesLayout.setVisibility(View.GONE);

            mRootRef.child("Memberships").child("groups").child(group.groupId).child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){

                        Query unreadMessagesQuery = mRootRef.child("Messages").child(group.getGroupId())
                                .orderByChild("time").startAfter(dataSnapshot.getValue(Long.class));

                        unreadMessagesQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long unreadMessagesCount = dataSnapshot.getChildrenCount();
                                if (unreadMessagesCount <= 0){
                                    unreadMessagesLayout.setVisibility(View.GONE);
                                }
                                else {
                                    unreadMessages.setText(String.format(Locale.getDefault(),"%d", unreadMessagesCount));
                                    unreadMessagesLayout.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent mainIntent = new Intent(itemView.getContext(), GroupChatActivity.class);
                    mainIntent.putExtra("groupCode", group.getGroupId());
                    mainIntent.putExtra("showHidden", showHidden);
                    context.startActivity(mainIntent);
                }
            });
        }
    }
}
