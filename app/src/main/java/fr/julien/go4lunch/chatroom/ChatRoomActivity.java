package fr.julien.go4lunch.chatroom;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.ActivityChatRoomBinding;
import fr.julien.go4lunch.databinding.ActivityHomeBinding;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.home.HomeActivity;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.models.Inbox;
import fr.julien.go4lunch.viewmodel.InboxViewModel;

import java.util.Arrays;
import java.util.Date;

public class ChatRoomActivity extends AppCompatActivity implements InboxAdapter.OnDataChange{

    private ActivityChatRoomBinding binding;
    private InboxViewModel inboxViewModel;
    private InboxAdapter adapter;
    private String userId;
    private String userName;
    private String currentUserId;
    private String currentUPicUrl;
    public static final String EXTRA_USER_ID = "userId";
    public static final String EXTRA_USER_NAME = "userName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        this.configureViewModel();
        Intent intent = this.getIntent();
        userId = intent.getStringExtra(EXTRA_USER_ID);
        userName = intent.getStringExtra(EXTRA_USER_NAME);
        this.configureToolbar();
        currentUserId = inboxViewModel.getCurrentUserId();
        currentUPicUrl = inboxViewModel.getCurrentUserUrlPic();
        this.configureRecyclerView();


        binding.sendBtn.setOnClickListener(v -> {
            if (!binding.editTextMessage.getText().toString().isEmpty()){
                Date date = new Date();
                Inbox inbox = new Inbox(currentUserId, userId, binding.editTextMessage.getText().toString(),currentUPicUrl,
                        date, Arrays.asList(currentUserId, userId));
                inboxViewModel.newMessage(inbox);
                binding.editTextMessage.setText("");
            }
        });
    }

    /** For return button **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void configureToolbar(){
        setSupportActionBar(binding.toolbar);
        TextView mTitle = (TextView) binding.toolbar.findViewById(R.id.pseudo_name);
        mTitle.setText(userName);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    /** Configuring ViewModel **/
    private void configureViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideInboxViewModelFactory();
        inboxViewModel = new ViewModelProvider(this, viewModelFactory).get(InboxViewModel.class);

    }

    private void configureRecyclerView(){
        binding.inboxRecyclerView.setHasFixedSize(true);
        binding.inboxRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InboxAdapter(inboxViewModel.getPrivateChatRoomMessage(currentUserId, userId), currentUserId, this);
        binding.inboxRecyclerView.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
        binding.inboxRecyclerView.scrollToPosition(adapter.getItemCount()-1);
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    /** Used to navigate to this activity **/
    public static void navigate(FragmentActivity activity, String userId, String userName) {
        Intent intent = new Intent(activity, ChatRoomActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_USER_NAME, userName);
        ActivityCompat.startActivity(activity, intent, null);
    }

    @Override
    public void onDataChanged() {
        binding.inboxRecyclerView.scrollToPosition(adapter.getItemCount()-1);
    }
}
