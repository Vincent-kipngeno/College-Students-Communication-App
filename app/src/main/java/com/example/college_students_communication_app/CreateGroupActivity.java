package com.example.college_students_communication_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.college_students_communication_app.databinding.ActivityCreateGroupBinding;
import com.example.college_students_communication_app.models.Chat;
import com.example.college_students_communication_app.models.Group;
import com.example.college_students_communication_app.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateGroupActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityCreateGroupBinding binding;

    private String groupId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    // Uri indicates, where the image will be picked from
    private Uri filePath;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;
    private Uri imageUrl = Uri.parse("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Create Group");

        binding.profileImage.setOnClickListener(this);
        binding.createGroupBtn.setOnClickListener(this);
    }

    private void SelectImage()
    {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }

    public boolean validateInputs(String groupName, String description){
        if (TextUtils.isEmpty(groupName))
        {
            binding.groupNameEditText.setError("Please enter group name...");
            return false;
        }
        else if (TextUtils.isEmpty(description))
        {
            binding.groupDescriptionEditText.setError("Please enter group description...");
            return  false;
        }
        else {
            return true;
        }
    }

    @Override
    public void onClick(View view) {

        if (view == binding.profileImage){
            SelectImage();
        }

        if (view == binding.createGroupBtn){
            createGroup();
        }
    }

    public void createGroup(){
        String groupName = binding.groupNameEditText.getText().toString();
        String description = binding.groupDescriptionEditText.getText().toString();
        long timeStamp = System.currentTimeMillis();

        if(validateInputs(groupName, description)){
            RootRef.child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        User user = dataSnapshot.getValue(User.class);

                        assert user != null;
                        String groupId = RootRef.child("Groups").push().getKey();
                        assert groupId != null;
                        Group group = new Group(groupName, description, imageUrl.toString(), mAuth.getUid(), "Created Group " + groupName, user.getUsername().split(" ")[0], timeStamp, groupId);
                        RootRef.child("Groups").child(groupId).setValue(group);

                        Map<String, Object> membership = new HashMap<>();
                        membership.put("groups/"+ groupId + "/" + mAuth.getUid(), timeStamp);
                        membership.put("users/"+ mAuth.getUid() + "/" + groupId, timeStamp);
                        RootRef.child("Memberships").updateChildren(membership);

                        String messageId = RootRef.child("Messages").push().getKey();

                        Chat chat = new Chat("Created Group " + groupName, mAuth.getUid(), timeStamp, groupId, 1, messageId);
                        chat.setInfo(true);
                        assert messageId != null;
                        RootRef.child("Messages").child(messageId).setValue(chat);

                        Intent mainIntent = new Intent(CreateGroupActivity.this, AddMembersActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        mainIntent.putExtra("groupId", groupId);
                        startActivity(mainIntent);

                        finish();
                    }

                    else {
                        Toast
                                .makeText(CreateGroupActivity.this,
                                        "InExistent user ",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast
                            .makeText(CreateGroupActivity.this,
                                    "User fetching failed. ",
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
    }

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data)
    {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                binding.profileImage.setImageBitmap(bitmap);

                uploadImage();
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    // UploadImage method
    private void uploadImage()
    {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            UploadTask uploadTask = ref.putFile(filePath);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        imageUrl = task.getResult();
                        // Image uploaded successfully
                        // Dismiss dialog
                        progressDialog.dismiss();
                        Toast
                                .makeText(CreateGroupActivity.this,
                                        "Image Uploaded!!",
                                        Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        // Error, Image not uploaded
                        progressDialog.dismiss();
                        Toast
                                .makeText(CreateGroupActivity.this,
                                        "Failed " + task.getException(),
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });
        }
    }
}