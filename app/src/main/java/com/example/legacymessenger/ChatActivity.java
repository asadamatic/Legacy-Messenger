package com.example.legacymessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.legacymessenger.Adapters.MessageListAdapter;
import com.example.legacymessenger.Data.Message;
import com.example.legacymessenger.Data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class ChatActivity extends AppCompatActivity {


    //UI components
    Toolbar toolbar;
    TextView toolbarText;
    CircularImageView toolbarImage;


    EditText chatBox;
    Button sendButton;


    //RecyclerView
    RecyclerView messageRecyclerView;
    MessageListAdapter messageListAdapter;
    RecyclerView.LayoutManager layoutManager;



    //Data
    User user;
    String displayName, phoneNumber, profileImage, userId;

    //Message Data
    List<Message> messageList = new ArrayList<>();
    Message message;
    String textMessage;

    //Firebase components
    DatabaseReference databaseReference;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatBox = findViewById(R.id.edittext_chatbox);
        sendButton = findViewById(R.id.button_chatbox_send);

        messageRecyclerView = findViewById(R.id.reyclerview_message_list);
        layoutManager = new LinearLayoutManager(this);
        messageRecyclerView.setLayoutManager(layoutManager);
        messageListAdapter = new MessageListAdapter(getApplicationContext(), messageList);
        messageRecyclerView.setAdapter(messageListAdapter);




        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarText = toolbar.findViewById(R.id.toolbarText);
        toolbarImage = toolbar.findViewById(R.id.imageToolBar);

        user = (User) getIntent().getExtras().getSerializable("user");

        toolbarText.setText(user.getDisplayName());

        Glide.with(this).load(Uri.parse(user.getProfileImage())).into(toolbarImage);

        loadMessages();




        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void loadMessages() {

        messageList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Messages");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){



                        if (snapshot.getKey() != null) {

                            message = snapshot.getValue(Message.class);

                            if (user.getUserId() != null) {


                                if (message.getConversationId().contains(FirebaseAuth.getInstance().getCurrentUser().getUid()) && message.getConversationId().contains(user.getUserId())) {


                                    messageList.add(message);

                                }

                            }
                        }

                    }

                    messageListAdapter.filteredList(messageList);
                    messageRecyclerView.scrollToPosition(messageList.size() - 1);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(){

        textMessage = chatBox.getText().toString();

        if (textMessage.isEmpty()){

            return;
        }
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Initializing firebase components
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Messages");

        message = new Message();

        displayName = currentUser.getDisplayName();
        phoneNumber = currentUser.getPhoneNumber();
        profileImage = String.valueOf(currentUser.getPhotoUrl());
        userId = currentUser.getUid();

        message.setConversationId(userId + user.getUserId());
        message.setSenderId(userId);
        message.setReceiverId(user.getUserId());
        message.setMessage(textMessage);
        message.setType("text");
        message.setTime(String.valueOf(System.currentTimeMillis()));

        databaseReference.push().setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    loadMessages();
                    chatBox.setText("");
                }else {

                    Toast.makeText(ChatActivity.this, "Some error occured", Toast.LENGTH_SHORT).show();
                }
            }
        });




    }
}
