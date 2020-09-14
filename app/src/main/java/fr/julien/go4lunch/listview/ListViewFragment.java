package fr.julien.go4lunch.listview;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.FragmentListViewBinding;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.home.HomeActivity;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.models.FinalRestaurant;
import fr.julien.go4lunch.utils.Utils;
import fr.julien.go4lunch.viewmodel.RestaurantsViewModel;
import fr.julien.go4lunch.viewmodel.UserViewModel;

import java.util.List;

public class ListViewFragment extends Fragment implements AdapterRestaurant.OnRestaurantItemClicked, Utils.OnClickPositiveButtonDialog{

    private FragmentListViewBinding binding;
    private NavController navController;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private RestaurantsViewModel restaurantsViewModel;
    private UserViewModel userViewModel;

    public ListViewFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListViewBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((HomeActivity)getActivity()).findViewById(R.id.toolbar_main).setVisibility(View.VISIBLE);
        ((HomeActivity)getActivity()).findViewById(R.id.bottom_navigation_view).setVisibility(View.VISIBLE);
        navController = Navigation.findNavController(view);
        setHasOptionsMenu(true);
        this.configureRestaurantsViewModel();
        this.configureUserViewModel();

        this.configureRecyclerView();
        this.getAllRestaurants();

    }



    /** Configuring ViewModel **/
    private void configureRestaurantsViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideRestaurantViewModelFactory();
        restaurantsViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantsViewModel.class);
    }

    private void configureUserViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideUserViewModelFactory();
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
        userViewModel.init();
    }

    /** ***************************** **/
    /** **** Recycler view Method *** **/
    /** ***************************** **/

    /** Configuring RecyclerView **/
    private void configureRecyclerView(){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        binding.listRestaurants.setLayoutManager(layoutManager);
    }

    private void getAllRestaurants(){
        restaurantsViewModel.getRestaurants().observe(getViewLifecycleOwner(), this::setAdapter);
    }

    private void setAdapter(List<FinalRestaurant> finalRestaurantList){
        if (finalRestaurantList != null){
            binding.listRestaurants.setAdapter(new AdapterRestaurant(this, finalRestaurantList));
        }else {
            alertDialog("No Match","Sorry we no found any place",2);
        }

    }

    private void alertDialog(String dialogueTitle, String dialogueMessage, int id){
        Utils utils = new Utils(this);
        utils.showAlertDialog(getContext(), dialogueTitle,dialogueMessage,"Done", "Cancel",
                R.drawable.background_alert_dialog, R.drawable.ic_warning_black_24dp, id);
    }

    @Override
    public void onClickedRestaurant(FinalRestaurant restaurant) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("restaurant", restaurant);
        navController.navigate(R.id.detailsFragment, bundle);
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
                public boolean onQueryTextChange(String newText) { return true;}
                @Override
                public boolean onQueryTextSubmit(String query) {
                    userViewModel.getCurrentUserData().observe(getViewLifecycleOwner(), user -> {
                        restaurantsViewModel.getPlaceFromSearch(query, user.getLatitude()+","+user.getLongitude(), user.getRadius(), getViewLifecycleOwner())
                                .observe(getViewLifecycleOwner(), finalRestaurants -> setAdapter(finalRestaurants));
                    });
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

    @Override
    public void positiveButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {
        dialog.dismiss();
    }

    @Override
    public void negativeButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {
        dialog.dismiss();
    }
}
