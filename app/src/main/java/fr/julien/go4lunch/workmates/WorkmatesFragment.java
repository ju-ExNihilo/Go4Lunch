package fr.julien.go4lunch.workmates;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.*;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.chatroom.ChatRoomActivity;
import fr.julien.go4lunch.databinding.FragmentWorkmatesBinding;
import fr.julien.go4lunch.details.DetailsActivity;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.viewmodel.UserViewModel;


public class WorkmatesFragment extends Fragment implements  AdapterUser.OnViewClicked{

    private FragmentWorkmatesBinding binding;
    private UserViewModel userViewModel;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private AdapterUser adapterUser;
    private NavController navController;
    private boolean isSearching = false;

    public WorkmatesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWorkmatesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        navController = Navigation.findNavController(view);
        this.configureUserViewModel();
        this.configureRecyclerView();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
               if (isSearching){
                   binding.listUsers.setLayoutManager(new LinearLayoutManager(getContext()));
                   adapterUser.updateOptions(userViewModel.getAllUser());
                   isSearching = false;
               }else {
                   navController.navigateUp();
               }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapterUser.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapterUser.stopListening();
    }

    /** Configure user ViewModel **/
    private void configureUserViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideUserViewModelFactory();
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
    }

    /** ***************************** **/
    /** ***** RecyclerView Method **** **/
    /** ***************************** **/

    /** Configuring RecyclerView **/
    private void configureRecyclerView(){
        binding.listUsers.setLayoutManager(new LinearLayoutManager(this.getContext()));
        adapterUser = new AdapterUser(userViewModel.getAllUser(), false, this);
        binding.listUsers.setAdapter(adapterUser);
    }

    private void getSearchUsers(String query){
        adapterUser.updateOptions(userViewModel.getSearchUser(query));
    }


    @Override
    public void onWorkmateItemClicked(String restaurantId) {
        DetailsActivity.navigate(this.getActivity(), restaurantId);
    }

    @Override
    public void onChatButtonClicked(String userId, String userName) {
        userViewModel.getCurrentUserData().observe(getViewLifecycleOwner(), user -> {
            ChatRoomActivity.navigate(getActivity(),userId, userName, user.getUrlPicture());
        });

    }

    @Override
    public void onDataChanged() {}

    /** ***************************** **/
    /** ***** Search view Method **** **/
    /** ***************************** **/

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if(searchItem != null){
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) { return true; }
                @Override
                public boolean onQueryTextSubmit(String query) {
                    getSearchUsers(query);
                    searchView.clearFocus();
                    isSearching = true;
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return false;
            default:
                break;
        }
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onOptionsItemSelected(item);
    }

}