package fr.julien.go4lunch.details;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.FragmentDetailsBinding;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.home.HomeActivity;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.listview.ListViewFragmentArgs;
import fr.julien.go4lunch.mapview.MapViewFragmentArgs;
import fr.julien.go4lunch.models.FinalRestaurant;
import fr.julien.go4lunch.utils.Utils;
import fr.julien.go4lunch.viewmodel.RestaurantsViewModel;
import fr.julien.go4lunch.viewmodel.UserViewModel;
import fr.julien.go4lunch.workmates.AdapterUser;

import static android.Manifest.permission.CALL_PHONE;


public class DetailsFragment extends Fragment implements AdapterUser.OnWorkmateItemClick, Utils.OnClickPositiveButtonDialog{

    private FragmentDetailsBinding binding;
    private NavController navController;
    private FinalRestaurant restaurant;
    private boolean isCustomer = false;
    private UserViewModel userViewModel;
    private String uId;
    private AdapterUser adapterUser;

    public DetailsFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((HomeActivity)getActivity()).findViewById(R.id.toolbar_main).setVisibility(View.GONE);
        ((HomeActivity)getActivity()).findViewById(R.id.bottom_navigation_view).setVisibility(View.GONE);

        navController = Navigation.findNavController(view);
        uId = Injection.provideUserRepository().getCurrentUser().getUid();
        restaurant = getArguments().getParcelable("restaurant");


        float rating = (float)(restaurant.getRating()/3.5)*2;
        this.configureViewModel();
        this.configureRecyclerView();

        this.getIfCustomer(restaurant.getPlaceId());
        this.configureBotomNavigationView();

        binding.restaurantDetailsName.setText(restaurant.getName());
        binding.restaurantDetailsAddress.setText(restaurant.getAddress());
        binding.restaurantDetailsRating.setRating(rating);
        Glide.with(binding.restaurantDetailsPic.getContext())
                .load(restaurant.getPhoto())
                .into(binding.restaurantDetailsPic);

        binding.restaurantDetailsFloatingBtn.setOnClickListener(v -> this.onClickFloatingActionBtn(restaurant.getName(), restaurant.getPlaceId()));

    }

    @Override
    public void onStop() {
        super.onStop();
        adapterUser.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapterUser.startListening();
    }

    private void onClickFloatingActionBtn(String eatingPlaceName, String eatingPlaceId ){
        if (!isCustomer){
            Utils.rotateAnimation(binding.restaurantDetailsFloatingBtn, 360, R.drawable.ic_check_circle_black_24dp);
            isCustomer = true;
            userViewModel.updateEatingPlace(uId, eatingPlaceName, eatingPlaceId);

        }else {
            Utils.rotateAnimation(binding.restaurantDetailsFloatingBtn, 360, R.drawable.baseline_restaurant_24);
            isCustomer = false;
            userViewModel.updateEatingPlace(uId, "none", "none");
        }
    }

    /** Configuring ViewModel **/
    private void configureViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideUserViewModelFactory();
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
        userViewModel.init();
    }

    /** Configuring RecyclerView **/
    private void configureRecyclerView(){
        binding.restaurantDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        adapterUser = new AdapterUser(userViewModel.getCustomer(restaurant.getPlaceId()), true, this);
        binding.restaurantDetailsRecyclerView.setAdapter(adapterUser);
    }


    private void getIfCustomer(String eatingPlaceId){
        userViewModel.getCurrentUserData().observe(getViewLifecycleOwner(), user -> {
            if (user.getEatingPlaceId() != null){
                if (user.getEatingPlaceId().equals(eatingPlaceId)){
                    binding.restaurantDetailsFloatingBtn.setImageResource(R.drawable.ic_check_circle_black_24dp);
                    isCustomer = true;
                }
            }
        });
    }

    @Override
    public void onWorkmateItemClicked(String restaurantId) {}

    @Override
    public void onChatButtonClicked(String userId) {
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        navController.navigate(R.id.chatRoomFragment, bundle);
    }

    @Override
    public void onDataChanged() {
        if (adapterUser.getItemCount() == 0){
            binding.nobodyText.setVisibility(View.VISIBLE);
            binding.nobodyImage.setVisibility(View.VISIBLE);
            binding.restaurantDetailsRecyclerView.setVisibility(View.GONE);
        }else {
            binding.nobodyText.setVisibility(View.GONE);
            binding.nobodyImage.setVisibility(View.GONE);
            binding.restaurantDetailsRecyclerView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void positiveButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {
        dialog.dismiss();
    }

    @Override
    public void negativeButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {
        dialog.dismiss();
    }

    private void configureBotomNavigationView(){
        binding.restaurantDetailsNavigation.setOnNavigationItemReselectedListener(item -> {
            int id = item.getItemId();
            switch (id){
                case R.id.call_details:
                    this.callRestaurant();
                    break;
                case R.id.website_details:
                    this.goToWebSiteRestaurant();
                    break;
            }
        });
    }

    private void alertDialog(int id, String message){
        Utils utils = new Utils(this);
        utils.showAlertDialog(this.getContext(), "Warning !",message,
                "Ok", "Cancel",
                R.drawable.background_alert_dialog, R.drawable.ic_warning_black_24dp, id);
    }

    private void callRestaurant(){
        if (!restaurant.getPhone().equals("Phone dont set")){
            String tel = restaurant.getPhone().replace(" ", "");
            Log.i("DEBUGGG", tel);
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + tel));
            if (ContextCompat.checkSelfPermission(getActivity(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent);
            } else {
                requestPermissions(new String[]{CALL_PHONE}, 1);
            }
        }else {
            alertDialog(2, "Sorry this restaurant hasn’t added a phone number yet.");
        }
    }

    private void goToWebSiteRestaurant(){
        if (!restaurant.getWebsite().equals("site dont set")){
            String url = restaurant.getWebsite();
            Log.i("DEBUGGG", url);
            Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
            startActivity(intent);
        }else {
            alertDialog(1,"Sorry this restaurant hasn’t added a website yet.");
        }
    }
}
