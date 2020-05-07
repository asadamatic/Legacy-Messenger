package com.example.legacymessenger;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;


import com.example.legacymessenger.Adapters.UserAdapter;
import com.example.legacymessenger.Data.Message;
import com.example.legacymessenger.Data.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Friends extends Fragment {


    //RecyclerView Setup
    RecyclerView usersRV;
    RecyclerView.LayoutManager layoutManager;
    UserAdapter userAdapter;

    //User Data
    User user;
    Message message;
    List<Message> messageList = new ArrayList<>();

    //Users lists
    List<User> usersList = new ArrayList<>();
    List<User> emptyList = new ArrayList<>();
    List<User> filteredList;
    List<User> friends;
    List<User> dummyList = new ArrayList<>();


    //Firebase components
    FirebaseUser currentUser;
    DatabaseReference databaseReference, messageDatabaseReference;

    public Friends() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {




        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        usersRV = view.findViewById(R.id.usersRV);

        layoutManager = new LinearLayoutManager(getActivity());

        usersRV.setLayoutManager(layoutManager);

        usersRV.setItemAnimator(new DefaultItemAnimator());

        usersRV.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), usersRV, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // ...

                Toast.makeText(getActivity(), String.valueOf(position), Toast.LENGTH_SHORT).show();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user", (Serializable) dummyList.get(position));


                startActivity(new Intent(getActivity(), ChatActivity.class).putExtras(bundle));
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // ...
            }
        }));

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        user = new User();
        message = new Message();

        getUsers();
        getMessages();



    }

    @Override
    public void onResume() {
        super.onResume();

        dummyList.clear();
        filterFriends();

        dummyList = friends;
    }

    private void getUsers() {


        databaseReference = FirebaseDatabase.getInstance().getReference("UID");


        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {

                    if (!String.valueOf(snapshot.getKey()).equals(currentUser.getUid())) {

                        if (snapshot.getKey() != null) {

                            user = snapshot.getValue(User.class);
                            usersList.add(user);

                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getMessages(){

        messageDatabaseReference = FirebaseDatabase.getInstance().getReference("Messages");

        messageDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){

                        if (snapshot.getKey() != null) {


                            message = snapshot.getValue(Message.class);

                            messageList.add(message);
                        }

                    }
                }

                filterFriends();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void filterFriends() {

        dummyList.clear();

        friends = new ArrayList<>();
        for (User filterUser: usersList){

            for (Message filterMessage: messageList){

                if (filterUser.getUserId() != null) {

                    if (filterMessage.getConversationId().contains(FirebaseAuth.getInstance().getCurrentUser().getUid()) && filterMessage.getConversationId().contains(filterUser.getUserId())) {

                        friends.add(filterUser);

                        break;
                    }

                }
            }
        }

        userAdapter = new UserAdapter(getActivity(), friends);

        usersRV.setAdapter(userAdapter);

        userAdapter.notifyDataSetChanged();

        dummyList = friends;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem searchIcon = menu.findItem(R.id.searchIcon);

        SearchView searchView = (SearchView) searchIcon.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {



                filter(newText);

                return false;
            }
        });

        searchIcon.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {


                dummyList.clear();

                userAdapter = new UserAdapter(getActivity(), emptyList);

                usersRV.setAdapter(userAdapter);

                userAdapter.notifyDataSetChanged();

                dummyList = emptyList;

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {

                dummyList.clear();

                filterFriends();
                dummyList = friends;
                return true;
            }
        });
    }

    private void filter(String newText) {

        dummyList.clear();

        filteredList = new ArrayList<>();

        for (User users: usersList){

            if (users.getDisplayName().toLowerCase().contains(newText.toLowerCase())){

                filteredList.add(users);
            }
        }
        userAdapter.filteredList(filteredList);

        dummyList = filteredList;
    }
}
