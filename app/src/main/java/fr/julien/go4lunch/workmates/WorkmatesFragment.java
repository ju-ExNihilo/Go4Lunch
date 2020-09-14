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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.FragmentWorkmatesBinding;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.home.HomeActivity;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.models.FinalRestaurant;
import fr.julien.go4lunch.viewmodel.RestaurantsViewModel;
import fr.julien.go4lunch.viewmodel.UserViewModel;


public class WorkmatesFragment extends Fragment implements AdapterUser.OnWorkmateItemClick{

    private FragmentWorkmatesBinding binding;
    private UserViewModel userViewModel;
    private RestaurantsViewModel restaurantsViewModel;
    private NavController navController;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private AdapterUser adapterUser;

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

        ((HomeActivity)getActivity()).findViewById(R.id.toolbar_main).setVisibility(View.VISIBLE);
        ((HomeActivity)getActivity()).findViewById(R.id.bottom_navigation_view).setVisibility(View.VISIBLE);

        navController = Navigation.findNavController(view);
        setHasOptionsMenu(true);
        this.configureUserViewModel();
        this.configureRestaurantsViewModel();
        this.configureRecyclerView();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapterUser.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapterUser.stopListening();
    }

    /** Configuring ViewModel **/
    private void configureUserViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideUserViewModelFactory();
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
        userViewModel.init();
    }
    /** Configuring ViewModel **/
    private void configureRestaurantsViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideRestaurantViewModelFactory();
        restaurantsViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantsViewModel.class);
    }

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

        restaurantsViewModel.getRestaurantById(Injection.provideUserRepository().getCurrentUser().getUid(), restaurantId).observe(getViewLifecycleOwner(), finalRestaurant -> {
            if (finalRestaurant != null){
                Log.i("DEBUGGGG", "From Firestore ");
                navController.navigate(R.id.detailsFragment, restaurantBundle(finalRestaurant));
            }else {
                Log.i("DEBUGGGG", "From API ");
                restaurantsViewModel.getPlaceDetailsInfoFromApi(restaurantId).observe(getViewLifecycleOwner(), restaurant -> {
                    navController.navigate(R.id.detailsFragment, restaurantBundle(restaurant));
                });
            }
        });
    }

    private Bundle restaurantBundle(FinalRestaurant finalRestaurant){
        Bundle bundle = new Bundle();
        bundle.putParcelable("restaurant", finalRestaurant);
        return bundle;
    }

    @Override
    public void onChatButtonClicked(String userId) {
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        navController.navigate(R.id.chatRoomFragment, bundle);
    }

    @Override
    public void onDataChanged() {

    }

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

}