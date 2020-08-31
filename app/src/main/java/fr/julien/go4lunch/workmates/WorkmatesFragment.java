package fr.julien.go4lunch.workmates;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.FragmentWorkmatesBinding;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.viewmodel.UserViewModel;


public class WorkmatesFragment extends Fragment {

    private FragmentWorkmatesBinding binding;
    private UserViewModel userViewModel;
    private NavController navController;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

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
        setHasOptionsMenu(true);
        this.configureViewModel();
        this.configureRecyclerView();
        this.getAllUsers();
    }

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
                public boolean onQueryTextChange(String newText) {
                    Log.i("DEBUGGGG", "onQueryTextChange : " + newText);

                    return true;
                }
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.i("DEBUGGGG","onQueryTextSubmit : "+ query);
                    getSearchUsers(query);
                    searchView.clearFocus();
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
                // Not implemented here
                return false;
            default:
                break;
        }
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onOptionsItemSelected(item);
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

    private void getSearchUsers(String query){
        userViewModel.getSearchUser(query).observe(getViewLifecycleOwner(), users -> {
            binding.listUsers.setAdapter(new AdapterUser(users));
        });
    }

}