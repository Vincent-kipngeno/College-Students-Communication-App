package com.example.college_students_communication_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;
import com.example.college_students_communication_app.Adapters.GroupMessageAdapter;
import com.example.college_students_communication_app.contracts.ChatDataReaderContract;
import com.example.college_students_communication_app.contracts.ChatReaderDbHelper;
import com.example.college_students_communication_app.databinding.ActivityGroupChatBinding;
import com.example.college_students_communication_app.ml.BertTransformer;
import com.example.college_students_communication_app.models.Chat;
import com.example.college_students_communication_app.models.Group;
import com.example.college_students_communication_app.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityGroupChatBinding binding;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String messageSenderID;
    private String groupCode;
    ChatReaderDbHelper dbHelper;
    int count;
    private BertTransformer bertTransformer;
    private Handler handler;

    private final List<Chat> chats = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupMessageAdapter groupMessageAdapter;
    private TextView groupName, groupDescription;
    private CircleImageView groupImage;

    private Toolbar ChatToolBar;

    String username = "";
    private boolean showHidden = false;

    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        dbHelper = new ChatReaderDbHelper(GroupChatActivity.this);

        groupCode = getIntent().getExtras().get("groupCode").toString();
        showHidden = (Boolean) getIntent().getExtras().get("showHidden");

        IntializeControllers();

        RootRef.child("Groups").child(groupCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    group = snapshot.getValue(Group.class);

                    groupName.setText(group.getGroupName());
                    groupDescription.setText(group.getDescription());
                    String profileImage = TextUtils.isEmpty(group.getProfileImage())? "https://example.com" : group.getProfileImage();
                    Picasso.get()
                            .load(profileImage)
                            .placeholder(R.drawable.group)
                            .error(R.drawable.group)
                            .into(groupImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        groupMessageAdapter = new GroupMessageAdapter(chats);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        binding.groupChatMessages.setLayoutManager(linearLayoutManager);
        binding.groupChatMessages.setAdapter(groupMessageAdapter);

        binding.sendMessageButton.setOnClickListener(this);

        binding.fab.setOnClickListener(this);

        HandlerThread handlerThread = new HandlerThread("QAClient");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        bertTransformer = new BertTransformer(getApplicationContext());
    }

    @Override
    public void onClick(View view) {
        if (view == binding.sendMessageButton){
            sendChatMessage();
        }

        if (view == binding.fab){
            Intent mainIntent = new Intent(GroupChatActivity.this, AddMembersActivity.class);
            mainIntent.putExtra("groupId", group.getGroupId());
            mainIntent.putExtra("showHidden", true);
            startActivity(mainIntent);
        }
    }

   /* ValueEventListener chatValueEventListener = new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if (dataSnapshot.exists()){

                List<Chat> chats = dataSnapshot.getValue(List<Chat.class>);
                Chat chat = dataSnapshot.getValue(Chat.class);

                saveChatToSqlite(chat);

                RootRef.child("Chats").child(groupCode).removeValue();

                updateChatsView();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }

    };*/

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d("GroupChatActivity", "onChildAdded:" + dataSnapshot.getKey());

            if (dataSnapshot.exists()){

                Chat chat = dataSnapshot.getValue(Chat.class);

                if (!showHidden && chat.getLabel() == 0) {
                    ;
                }else {
                    chats.add(chat);
                    groupMessageAdapter.notifyDataSetChanged();

                    binding.groupChatMessages.smoothScrollToPosition(binding.groupChatMessages.getAdapter().getItemCount());
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            if (dataSnapshot.exists()){
                Chat chat = dataSnapshot.getValue(Chat.class);
                chats.add(chat);
                groupMessageAdapter.notifyDataSetChanged();

                binding.groupChatMessages.smoothScrollToPosition(binding.groupChatMessages.getAdapter().getItemCount());
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w("GroupChatActivity", "getChats:onCancelled", databaseError.toException());
            Toast.makeText(GroupChatActivity.this, "Failed to load chats.",
                    Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onStart()
    {
        super.onStart();

        RootRef.child("Users").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    username = user.getUsername().split(" ")[0];
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        chats.clear();
        RootRef.child("Messages").child(groupCode).addChildEventListener(childEventListener);

        handler.post(
                () -> {
                    bertTransformer.loadDictionary();
                });
    }

    @Override
    protected void onStop() {
        super.onStop();

        long time = System.currentTimeMillis();

        Map<String, Object> lastRead = new HashMap<>();
        lastRead.put("groups/" + groupCode + "/" + mAuth.getUid(), time);
        lastRead.put("users/" + mAuth.getUid() + "/" + groupCode, time);
        RootRef.child("Memberships").updateChildren(lastRead);
        RootRef.child("Messages").child(groupCode).removeEventListener(childEventListener);
    }

    private void IntializeControllers()
    {
        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);
        ChatToolBar.isEnabled();

        groupName = (TextView) findViewById(R.id.custom_profile_name);
        groupDescription = (TextView) findViewById(R.id.custom_user_last_seen);
        groupImage = (CircleImageView) findViewById(R.id.custom_profile_image);
    }

    private void sendChatMessage()
    {
        String chatText = binding.inputMessage.getText().toString();
        long time = System.currentTimeMillis();

        if (!TextUtils.isEmpty(chatText))
        {
            //handler.removeCallbacksAndMessages(null);
            String currentUserID = mAuth.getCurrentUser().getUid();
            String chatId = RootRef.child("Messages").child(groupCode).push().getKey();
            Chat chat = new Chat(chatText, currentUserID, time, groupCode, 1, chatId);

            RootRef.child("Messages").child(groupCode).child(chatId).setValue(chat).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (!task.isSuccessful())
                    {
                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        binding.inputMessage.setText("");
                    }
                    binding.inputMessage.setText("");
                    predict(chat);

                    Map<String, Object> latestMessage = new HashMap<>();
                    latestMessage.put("Groups/" + groupCode + "/" + "latestMessage", chat.getMessage());
                    latestMessage.put("Groups/" + groupCode + "/" + "timeStamp", chat.getTime());
                    latestMessage.put("Groups/" + groupCode + "/" + "senderName", username);

                    RootRef.updateChildren(latestMessage);
                }
            });

            binding.inputMessage.setText("");
        }
    }

    public void saveChatToSqlite(Chat chat){

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long newRowId = db.insert(ChatDataReaderContract.ChatDataEntry.TABLE_NAME, null, chat.getChatValues());
        predict(chat);
    }

    public void updateChatsView(){
        chats.clear();
        chats.addAll(getChatsFromSqlLite());
        groupMessageAdapter.notifyDataSetChanged();
        binding.groupChatMessages.smoothScrollToPosition(binding.groupChatMessages.getAdapter().getItemCount());
    }

    public List<Chat> getChatsFromSqlLite(){
        SQLiteDatabase dbW = dbHelper.getWritableDatabase();
        //dbW.execSQL(ChatDataReaderContract.SQL_DELETE_CHAT);
        dbW.execSQL(ChatDataReaderContract.SQL_CREATE_CHAT);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                ChatDataReaderContract.ChatDataEntry.COLUMN_NAME_MESSAGE,
                ChatDataReaderContract.ChatDataEntry.COLUMN_NAME_SENDER,
                ChatDataReaderContract.ChatDataEntry.COLUMN_NAME_GROUP_CODE,
                ChatDataReaderContract.ChatDataEntry.COLUMN_NAME_TIME,
                ChatDataReaderContract.ChatDataEntry.COLUMN_NAME_LABEL
        };

        String selection = ChatDataReaderContract.ChatDataEntry.COLUMN_NAME_GROUP_CODE + " = ? and "+ChatDataReaderContract.ChatDataEntry.COLUMN_NAME_LABEL + " = ?";
        String[] selectionArgs = { groupCode, "1" };

        String sortOrder = ChatDataReaderContract.ChatDataEntry.COLUMN_NAME_TIME + " ASC";

        Cursor cursor = db.query(
                ChatDataReaderContract.ChatDataEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        List<Chat> chats = new ArrayList<>();
        while(cursor.moveToNext()) {
            String message = cursor.getString(cursor.getColumnIndexOrThrow(ChatDataReaderContract.ChatDataEntry.COLUMN_NAME_MESSAGE));
            String sender = cursor.getString(cursor.getColumnIndexOrThrow(ChatDataReaderContract.ChatDataEntry.COLUMN_NAME_SENDER));
            String groupCode = cursor.getString(cursor.getColumnIndexOrThrow(ChatDataReaderContract.ChatDataEntry.COLUMN_NAME_GROUP_CODE));
            long time = cursor.getLong(cursor.getColumnIndexOrThrow(ChatDataReaderContract.ChatDataEntry.COLUMN_NAME_TIME));
            int label = cursor.getInt(cursor.getColumnIndexOrThrow(ChatDataReaderContract.ChatDataEntry.COLUMN_NAME_LABEL));
            Chat chat = new Chat(message, sender, time, groupCode, label, "ss");
            Log.i("MyAmplifyApp", chat.message + " - label:" + chat.label);
            chats.add(chat);
        }
        cursor.close();

        return chats;
    }

    private void predict(Chat chat){

        String chatFeatures = bertTransformer.getFeatures(chat.getMessage());

        try {
            JSONObject json = new JSONObject();
            String body = json.put("data", chatFeatures).toString().replaceAll("\"", "\\\"");

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-west-2:1d4dafa9-7c5a-4199-87a4-c8f250f57ded", // Identity pool ID
                    Regions.US_WEST_2 // Region
            );

            RestOptions options = RestOptions.builder()
                    .addPath("/predictchatsrelevance")
                    .addBody(body.getBytes())
                    .build();

            Amplify.API.post(options,
                    response -> {
                        Log.i("MyAmplifyApp", "POST succeeded: " + response.getData().asString());
                        String label = response.getData().asString();
                        if (label.equals("\"F\"")){
                            Log.i("MyAmplifyApp", chat.message + " - label:" + chat.label+"; before");
                            chat.setLabel(0);
                            RootRef.child("Messages").child(groupCode).child(chat.getChatId()).setValue(chat);
                            //db.update(ChatDataReaderContract.ChatDataEntry.TABLE_NAME, chat.getChatValues(), ChatDataReaderContract.ChatDataEntry._ID +" = ?", new String[]{String.valueOf(rowId)});
                            Log.i("MyAmplifyApp", chat.message + " - label:" + chat.label+"; updated");
                        }
                    },
                    error -> Log.e("MyAmplifyApp", "POST failed.", error)
            );
        }
        catch (Exception ex){
            Log.e("MyAmplifyApp: Exception", "POST failed."+ ex.getMessage());
            ex.printStackTrace();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu (Menu menu)

    {
        getMenuInflater().inflate(R.menu.chats_menu, menu);
        final MenuItem item = menu.findItem(R.id.show_hidden);
        item.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHidden = !showHidden;

                RootRef.child("Messages").child(groupCode).removeEventListener(childEventListener);
                chats.clear();
                RootRef.child("Messages").child(groupCode).addChildEventListener(childEventListener);

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
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.show_hidden:

                showHidden = !showHidden;

                RootRef.child("Messages").child(groupCode).removeEventListener(childEventListener);
                chats.clear();
                RootRef.child("Messages").child(groupCode).addChildEventListener(childEventListener);

                if (item.isChecked())
                {

                    item.setChecked(false);
                }
                else
                {
                    item.setChecked(true);
                    Toast.makeText(getApplication(), "Track Hidden", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }*/
}