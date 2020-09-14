package fr.julien.go4lunch.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.navigation.NavigationView;
import fr.julien.go4lunch.MainActivity;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.ActivityHomeBinding;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.utils.Utils;
import fr.julien.go4lunch.viewmodel.RestaurantsViewModel;
import fr.julien.go4lunch.viewmodel.UserViewModel;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Utils.OnClickPositiveButtonDialog {

    private ActivityHomeBinding binding;
    private NavController navController;
    private RestaurantsViewModel restaurantsViewModel;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        navController = Navigation.findNavController(this,R.id.nav_host_fragment);
        setSupportActionBar(binding.toolbarMain);
        this.configureNavigationView();
        this.configureDrawerLayout();
        this.setUpBottomNavigation();
        this.configureRestaurantsViewModel();
        this.configureUserViewModel();
    }

    /** Configuring ViewModel **/
    private void configureUserViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideUserViewModelFactory();
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
        userViewModel.init();
    }
    /** Configuring ViewModel **/
    private void configureRestaurantsViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideRestaurantViewModelFactory(this);
        restaurantsViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantsViewModel.class);
    }

    // 2 - Configure Drawer Layout
    private void configureDrawerLayout(){
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbarMain, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    // 3 - Configure NavigationView
    private void configureNavigationView(){
        binding.navView.setNavigationItemSelectedListener(this);
        View header = binding.navView.getHeaderView(0);
        TextView name = (TextView) header.findViewById(R.id.header_avatar_name);
        TextView email = (TextView) header.findViewById(R.id.header_avatar_email);
        ImageView pic = (ImageView) header.findViewById(R.id.header_avatar_pic);

        name.setText(Injection.provideUserRepository().getCurrentUser().getDisplayName());
        email.setText(Injection.provideUserRepository().getCurrentUser().getEmail());
        Glide.with(pic.getContext())
                .load(Injection.provideUserRepository().getCurrentUser().getPhotoUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(pic);
    }

    public void setUpBottomNavigation(){
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
    }

    public void getYourRestaurantId(){
        String uid = Injection.provideUserRepository().getCurrentUser().getUid();
        userViewModel.getCurrentUserData().observe(this, user -> {
            if (user.getEatingPlaceId().equals("none")){
                alertDialog(1);
            }else {
                navigateToYourLunch(user.getEatingPlaceId());
            }

        });
    }

    public void navigateToYourLunch(String restaurantId){
        restaurantsViewModel.getRestaurantById(restaurantId).observe(this, finalRestaurant -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("restaurant", finalRestaurant);
            navController.navigate(R.id.detailsFragment, bundle);
        });
    }

    private void alertDialog(int id){
        Utils utils = new Utils(this);
        utils.showAlertDialog(this, "Sorry !","You dont select eating place yet !!",
                "Ok", "Cancel",
                R.drawable.background_alert_dialog, R.drawable.ic_warning_black_24dp, id);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        switch (id){
            case R.id.your_lunch :
                getYourRestaurantId();
                break;
            case R.id.settings:
                Log.i("DEBUGGG","settings");
                navController.navigate(R.id.settingFragment);
                break;
            case R.id.logout:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnSuccessListener(aVoid -> MainActivity.navigate(this));
                Log.i("DEBUGGG","logout");
                break;
            default:
                break;
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /** Used to navigate to this activity **/
    public static void navigate(FragmentActivity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        ActivityCompat.startActivity(activity, intent, null);
        activity.finish();
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
