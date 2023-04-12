package com.example.college_students_communication_app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.college_students_communication_app.R;
import com.example.college_students_communication_app.Utils.AlertDialogHelper;
import com.example.college_students_communication_app.models.Group;
import com.example.college_students_communication_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupParticipantsAdapter extends RecyclerView.Adapter<GroupParticipantsAdapter.MemberViewHolder>
{
    private ArrayList<User> members;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUid;
    private DatabaseReference mRootRef;
    private String groupId;
    private Context context;
    GroupParticipantsAdapter adapter = this;
    AlertDialogHelper alertDialogHelper;


    public GroupParticipantsAdapter(Context context, ArrayList<User> members, String groupId)
    {
        this.members = members;
        this.groupId = groupId;
        this.context = context;
        alertDialogHelper =new AlertDialogHelper(context);
    }

    @NonNull
    @Override
    public GroupParticipantsAdapter.MemberViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        View view;

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        currentUid = mAuth.getUid();

        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_member_layout, viewGroup, false);

        return new GroupParticipantsAdapter.MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupParticipantsAdapter.MemberViewHolder memberViewHolder, int i)
    {
        memberViewHolder.bindViews(members.get(i));
    }

    @Override
    public int getItemCount()
    {
        return members.size();
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder implements AlertDialogHelper.AlertDialogListener
    {

        private View itemView;
        private TextView userName;
        private TextView phone;
        private ImageView tickedBox;
        private ImageView profileImg;
        private LinearLayout containerBox;

        public MemberViewHolder(@NonNull View itemView)
        {
            super(itemView);

            this.itemView = itemView;
            userName = itemView.findViewById(R.id.userName);
            tickedBox = itemView.findViewById(R.id.tickedBox);
            profileImg  = itemView.findViewById(R.id.profileImg);
            phone = itemView.findViewById(R.id.phone);
            containerBox = itemView.findViewById(R.id.containerBox);
        }

        public void bindViews(User member){

            userName.setText(member.getUsername());
            phone.setText(member.getPhone());


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mRootRef.child("Groups").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                Group group = snapshot.getValue(Group.class);
                                if (currentUid.equals(group.getAdmin())){
                                    alertDialogHelper.showAlertDialog("","Remove Participant","REMOVE","CANCEL",1,false);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

        }

        @Override
        public void onPositiveClick(int from) {
            if(from==1) {
                mRootRef.child("Memberships").child(groupId).child(currentUid).setValue(null);
                mRootRef.child("Memberships").child(currentUid).child(groupId).setValue(null);
                int pos = getAdapterPosition();
                members.remove(pos);
                adapter.notifyItemRemoved(pos);
            }
        }

        @Override
        public void onNegativeClick(int from) {

        }

        @Override
        public void onNeutralClick(int from) {

        }
    }

    public ArrayList<User> getAll() {
        return members;
    }

    public ArrayList<User> getSelected() {
        ArrayList<User> selected = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).isChecked()) {
                selected.add(members.get(i));
            }
        }
        return selected;
    }

}
