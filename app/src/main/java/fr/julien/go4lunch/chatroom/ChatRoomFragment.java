package fr.julien.go4lunch.chatroom;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.FragmentChatRoomBinding;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.home.HomeActivity;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.models.Inbox;
import fr.julien.go4lunch.viewmodel.InboxViewModel;
import fr.julien.go4lunch.viewmodel.UserViewModel;

import java.util.Arrays;
import java.util.Date;


public class ChatRoomFragment extends Fragment {

    private FragmentChatRoomBinding binding;
    private InboxViewModel inboxViewModel;
    private InboxAdapter adapter;
    private String userId;
    private String currentUserId;
    private String currentUPicUrl;

    public ChatRoomFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatRoomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((HomeActivity)getActivity()).findViewById(R.id.toolbar_main).setVisibility(View.VISIBLE);
        ((HomeActivity)getActivity()).findViewById(R.id.bottom_navigation_view).setVisibility(View.VISIBLE);
        this.configureViewModel();

        userId = getArguments().getString("userId");
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

    /** Configuring ViewModel **/
    private void configureViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideInboxViewModelFactory();
        inboxViewModel = new ViewModelProvider(this, viewModelFactory).get(InboxViewModel.class);

    }


    private void configureRecyclerView(){
        binding.inboxRecyclerView.setHasFixedSize(true);
        binding.inboxRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        adapter = new InboxAdapter(inboxViewModel.getPrivateChatRoomMessage(currentUserId, userId), currentUserId);
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
}
