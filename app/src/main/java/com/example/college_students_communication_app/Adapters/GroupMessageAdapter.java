package com.example.college_students_communication_app.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.college_students_communication_app.R;
import com.example.college_students_communication_app.models.Chat;
import com.example.college_students_communication_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ChatViewHolder>
{
    private List<Chat> groupMessages;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUid;
    private DatabaseReference mRootRef;


    public GroupMessageAdapter(List<Chat> groupMessages)
    {
        this.groupMessages = groupMessages;
    }


    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view;

        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.received_message_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        currentUid = mAuth.getUid();

        assert currentUid != null;
        if (currentUid.equals(groupMessages.get(i).getSenderId())){

            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.sent_message_layout, viewGroup, false);
        }

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatViewHolder chatViewHolder, int i)
    {
        chatViewHolder.bindViews(groupMessages.get(i));
    }

    @Override
    public int getItemCount()
    {
        return groupMessages.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder
    {

        private View itemView;

        public ChatViewHolder(@NonNull View itemView)
        {
            super(itemView);

            this.itemView = itemView;
        }

        public void bindViews(Chat chat){
            if (currentUid.equals(groupMessages.get(getLayoutPosition()).getSenderId())){

                TextView sentMessageText = itemView.findViewById(R.id.sentMessageText);
                TextView sentMessageTime = itemView.findViewById(R.id.sentMessageTime);

                sentMessageText.setText(chat.getMessage());
                long millis = chat.getTime();
                String hm = String.format(Locale.getDefault(),"%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1));
                sentMessageTime.setText(hm);
            }

            else {
                TextView receivedMessageText = itemView.findViewById(R.id.receivedMessageText);
                TextView receivedMessageTime = itemView.findViewById(R.id.receivedMessageTime);
                TextView senderName = itemView.findViewById(R.id.senderName);

                receivedMessageText.setText(chat.getMessage());

                long millis = chat.getTime();
                String hm = String.format(Locale.getDefault(),"%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1));
                receivedMessageTime.setText(hm);

                mRootRef.child("Users").child(chat.getSenderId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        assert user != null;
                        senderName.setText(user.getUsername());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

}