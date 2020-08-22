package fr.julien.go4lunch.workmates;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fr.julien.go4lunch.databinding.FragmentWorkmatesBinding;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.models.User;
import fr.julien.go4lunch.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;


public class WorkmatesFragment extends Fragment  {

    private FragmentWorkmatesBinding binding;
    private UserViewModel userViewModel;
    private NavController navController;
    private String query;
    public static final String NO_BUNDLE = "NO_BUNDLE";

    public WorkmatesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWorkmatesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        Bundle bundle = this.getArguments();
        if (bundle != null){
            query  = bundle.getString("message",NO_BUNDLE);
            Log.i("DEBUGGG",query);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.configureViewModel();
        this.configureRecyclerView();
        if (query.equals(NO_BUNDLE)){
            this.getAllUsers();
        }else {
            this.getSearchUser();
        }
        this.setUpNavigation();
    }

    /** Configuring ViewModel **/
    private void configureViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideViewModelFactory();
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);

    }

    /** Configuring RecyclerView **/
    private void configureRecyclerView(){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        binding.listUsers.setLayoutManager(layoutManager);
    }

    /** Get All Users **/
    private void getAllUsers(){
        userViewModel.getAllUser().observe(getViewLifecycleOwner(), users -> {
            binding.listUsers.setAdapter(new AdapterUser(users));
        });
    }

    private void getSearchUser(){
        User user = new User("hjhfd", query);
        List<User> userList = new ArrayList<>();
        userList.add(user);
        binding.listUsers.setAdapter(new AdapterUser(userList));
    }

    public void setUpNavigation(){
        NavigationUI.setupWithNavController(binding.bottomNavigationWorkmates, navController);
    }

}