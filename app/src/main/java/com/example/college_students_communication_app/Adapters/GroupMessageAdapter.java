package com.example.college_students_communication_app.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ChatViewHolder>
{
    public List<Chat> groupMessages;
    public List<Chat> selectedMessages;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUid;
    private DatabaseReference mRootRef;
    private boolean showHidden;
    private Context context;

    final int LAYOUT_SENT = 0;
    final int LAYOUT_RECEIVED = 1;


    public GroupMessageAdapter(Context context, List<Chat> groupMessages, List<Chat> selectedMessages)
    {
        this.groupMessages = groupMessages;
        this.selectedMessages = selectedMessages;
        this.context = context;
    }

    public void setHidden(boolean isHidden){
        showHidden = isHidden;
    }

    @Override
    public int getItemViewType(int position)
    {

        currentUid = FirebaseAuth.getInstance().getUid();

        if (currentUid.equals(groupMessages.get(position).getSenderId()))
            return LAYOUT_SENT;
        else
            return LAYOUT_RECEIVED;

    }


    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        View view;

        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.received_message_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        currentUid = mAuth.getUid();

        if (viewType == LAYOUT_SENT){

            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.sent_message_layout, viewGroup, false);
        }

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatViewHolder chatViewHolder, int i)
    {
        if(selectedMessages.contains(groupMessages.get(i)))
            chatViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundColor));
        else
            chatViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.black));
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
            ImageView reportImage = itemView.findViewById(R.id.reportIcon);

            if (chat.getLabel() == 0){
                reportImage.setVisibility(View.VISIBLE);
            }
            else {
                reportImage.setVisibility(View.INVISIBLE);
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(chat.getTime());

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            String hm = String.format("%02d:%02d", hour, minute);

            if (currentUid.equals(groupMessages.get(getLayoutPosition()).getSenderId())){

                TextView sentMessageText = itemView.findViewById(R.id.sentMessageText);
                TextView sentMessageTime = itemView.findViewById(R.id.sentMessageTime);

                sentMessageText.setText(chat.getMessage());
                sentMessageTime.setText(hm);
            }

            else {
                TextView receivedMessageText = itemView.findViewById(R.id.receivedMessageText);
                TextView receivedMessageTime = itemView.findViewById(R.id.receivedMessageTime);
                TextView senderName = itemView.findViewById(R.id.senderName);

                receivedMessageText.setText(chat.getMessage());
                receivedMessageTime.setText(hm);

                assert chat.getSenderId()!=null;
                mRootRef.child("Users").child(chat.getSenderId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            User user = dataSnapshot.getValue(User.class);

                            assert user != null;
                            senderName.setText(user.getUsername());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

}
