package com.mikal.chattingapplication;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;

    private String currentGroupName,currentUserID,currentUserName,currentDate,currentTime;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,GroupNameRef,GroupMessageKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentGroupName=getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this,currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);




        initializeFields();

        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                saveMessageInfoToDatabase();

                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists())
                {
                    displayMesssages(dataSnapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists())
                {
                    displayMesssages(dataSnapshot);
                }


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void initializeFields() {

        mToolbar=(Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton=(ImageButton) findViewById(R.id.send_message_button);
        userMessageInput=(EditText) findViewById(R.id.input_group_message);
        displayTextMessage=(TextView) findViewById(R.id.group_chat_text_display);
        mScrollView=(ScrollView) findViewById(R.id.my_scroll_view);
    }

    private void getUserInfo() {


        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                     currentUserName=dataSnapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void saveMessageInfoToDatabase() {


        String message=userMessageInput.getText().toString();
        String messageKey=GroupNameRef.push().getKey();


        if (TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "please write a message first...", Toast.LENGTH_SHORT).show();
        }

        else
        {
            Calendar calForDate=Calendar.getInstance();
            SimpleDateFormat cuurentDateFormat=new SimpleDateFormat("MMM dd, yyyy");
            currentDate=cuurentDateFormat.format(calForDate.getTime());

            Calendar calForTime=Calendar.getInstance();
            SimpleDateFormat cuurentTimeFormat=new SimpleDateFormat("hh:mm a");
            currentTime=cuurentTimeFormat.format(calForTime.getTime());

            HashMap<String,Object> groupMessageKey=new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef=GroupNameRef.child(messageKey);


            HashMap<String,Object> messageInfoMap=new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);

            GroupMessageKeyRef.updateChildren(messageInfoMap);



        }
    }

    private void displayMesssages(DataSnapshot dataSnapshot) {

        Iterator iterator=dataSnapshot.getChildren().iterator();

        String chatDate=(String) ((DataSnapshot) iterator.next()).getValue();
        String chatMessage=(String) ((DataSnapshot) iterator.next()).getValue();
        String chatName=(String) ((DataSnapshot) iterator.next()).getValue();
        String chatTime=(String) ((DataSnapshot) iterator.next()).getValue();

        displayTextMessage.append(chatName + ":\n" + chatMessage + "\n" + chatTime + "   " + chatDate + "\n\n\n");

        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

    }


}
