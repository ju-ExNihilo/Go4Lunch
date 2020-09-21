package fr.julien.go4lunch.details;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.*;
import com.bumptech.glide.Glide;
import fr.juju.googlemaplibrary.model.FinalPlace;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.chatroom.ChatRoomActivity;
import fr.julien.go4lunch.databinding.ActivityDetailsBinding;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.models.LikedRestaurant;
import fr.julien.go4lunch.utils.Utils;
import fr.julien.go4lunch.viewmodel.RestaurantsViewModel;
import fr.julien.go4lunch.viewmodel.UserViewModel;
import fr.julien.go4lunch.worker.EatingPlaceNotificationWorker;
import fr.julien.go4lunch.workmates.AdapterUser;

import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.CALL_PHONE;

public class DetailsActivity extends AppCompatActivity implements AdapterUser.OnViewClicked, Utils.OnClickButtonAlertDialog{

    private ActivityDetailsBinding binding;
    private boolean isCustomer = false;
    private UserViewModel userViewModel;
    private RestaurantsViewModel restaurantsViewModel;
    private String uId;
    private Utils utils;
    private String restaurantId;
    private AdapterUser adapterUser;
    public static final String PARCELABLE_RESTAURANT = "PARCELABLE_RESTAURANT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        utils = new Utils(this);
        this.configureRestaurantsViewModel();
        this.configureViewModel();
        this.configureToolbar();
        uId = Injection.provideUserRepository().getCurrentUser().getUid();
        Intent intent = getIntent();
        if (intent.hasExtra(PARCELABLE_RESTAURANT)){
            restaurantId = intent.getStringExtra(PARCELABLE_RESTAURANT);
            this.configureRecyclerView(restaurantId);
            restaurantsViewModel.getRestaurantById(restaurantId).observe(this, finalPlace -> {
                if (finalPlace != null){
                    initView(finalPlace);
                }else {
                    restaurantsViewModel.getPlaceDetailsInfoFromApi(restaurantId).observe(this, this::initView);
                }

            });

        }
    }

    private void initView(FinalPlace restaurant){
        float rating = (float)(restaurant.getRating()/3.5)*2;
        this.getIfCustomer(restaurant.getPlaceId());
        this.configureBotomNavigationView();
        this.isLikedRestaurant();
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
        setSupportActionBar(binding.toolbarDetails);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    /** Configuring ViewModel **/
    private void configureRestaurantsViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideRestaurantViewModelFactory(this);
        restaurantsViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantsViewModel.class);
    }

    private void onClickFloatingActionBtn(String eatingPlaceName, String eatingPlaceId ){
        if (!isCustomer){
            Utils.rotateAnimation(binding.restaurantDetailsFloatingBtn, 360, R.drawable.ic_check_circle_black_24dp);
            isCustomer = true;
            userViewModel.updateEatingPlace(uId, eatingPlaceName, eatingPlaceId);

        }else {
            Utils.rotateAnimation(binding.restaurantDetailsFloatingBtn, 360, R.drawable.baseline_restaurant_24);
            isCustomer = false;
            userViewModel.updateEatingPlace(uId, getString(R.string.none), getString(R.string.none));
        }
        this.notificationWorker(this);
    }

    private void notificationWorker(Context context){
        userViewModel.getCurrentUserData().observe(this, user -> {
            Data data = new Data.Builder().put(EatingPlaceNotificationWorker.KEY_EATING_PLACE, user.getEatingPlace()).build();
            OneTimeWorkRequest dailyWorkRequest  = new OneTimeWorkRequest.Builder(EatingPlaceNotificationWorker.class)
                    .setInitialDelay(utils.getMillisecondeUntilAHours(14, 55), TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .build();
            WorkManager.getInstance(context).enqueueUniqueWork(getString(R.string.notif), ExistingWorkPolicy.REPLACE, dailyWorkRequest) ;

        });
    }


    /** Configuring ViewModel **/
    private void configureViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideUserViewModelFactory();
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
        userViewModel.init();
    }

    /** Configuring RecyclerView **/
    private void configureRecyclerView(String placeId){
        binding.restaurantDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterUser = new AdapterUser(userViewModel.getCustomer(placeId), true, this);
        binding.restaurantDetailsRecyclerView.setAdapter(adapterUser);
    }


    private void getIfCustomer(String eatingPlaceId){
        userViewModel.getCurrentUserData().observe(this, user -> {
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
    public void onChatButtonClicked(String userId, String userName) {
        ChatRoomActivity.navigate(this,userId, userName);
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



    private void configureBotomNavigationView(){
        binding.restaurantDetailsNavigation.setOnNavigationItemSelectedListener(item -> {
            Log.i("DEBUGGG", "clicked");
            int id = item.getItemId();
            switch (id){
                case R.id.call_details:
                    this.callRestaurant();
                    break;
                case R.id.website_details:
                    this.goToWebSiteRestaurant();
                    break;
                case R.id.like_details:
                    this.insertLikedRestaurant();
                    break;
            }
            return true;
        });
    }

    private void insertLikedRestaurant(){
        restaurantsViewModel.getRestaurantById(restaurantId).observe(this, restaurant -> {
            LikedRestaurant likedRestaurant = new LikedRestaurant(restaurant.getPlaceId(), restaurant.getName(), restaurant.getPhoto());
            userViewModel.getLikedRestaurantById(restaurant.getPlaceId()).observe(this, likedR -> {
                if (likedR != null){
                    userViewModel.deleteLikedRestaurant(likedRestaurant.getId());
                }else {
                    userViewModel.insertLikedRestaurant(likedRestaurant);
                }
                isLikedRestaurant();
            });
        });
    }

    private void isLikedRestaurant(){
        userViewModel.getLikedRestaurantById(restaurantId).observe(this, likedRestaurant -> {
            if (likedRestaurant != null){
                binding.restaurantDetailsNavigation.getMenu().getItem(1).setIcon(R.drawable.ic_favorite);
                binding.restaurantDetailsNavigation.getMenu().getItem(1).setTitle(R.string.liked);
            }else {
                binding.restaurantDetailsNavigation.getMenu().getItem(1).setIcon(R.drawable.ic_star);
                binding.restaurantDetailsNavigation.getMenu().getItem(1).setTitle(R.string.like);
            }
        });
    }

    private void alertDialog(int id, String message){

        utils.showAlertDialog(this, getString(R.string.warning),message,
                getString(R.string.ok_btn), getString(R.string.cancel_btn),
                R.drawable.background_alert_dialog, R.drawable.ic_warning_black_24dp, id);
    }

    private void callRestaurant(){
        restaurantsViewModel.getRestaurantById(restaurantId).observe(this, restaurant -> {
            if (!restaurant.getPhone().equals(getString(R.string.phone_dont_set))){
                String tel = restaurant.getPhone().replace(" ", "");
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + tel));
                if (ContextCompat.checkSelfPermission(this, CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                } else {
                    requestPermissions(new String[]{CALL_PHONE}, 1);
                }
            }else {
                alertDialog(2, getString(R.string.sorry_no_phone));
            }
        });

    }

    private void goToWebSiteRestaurant(){
        restaurantsViewModel.getRestaurantById(restaurantId).observe(this, restaurant -> {
            if (!restaurant.getWebsite().equals(getString(R.string.site_sont_set))){
                String url = restaurant.getWebsite();
                Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
                startActivity(intent);
            }else {
                alertDialog(1,getString(R.string.soory_no_website));
            }
        });

    }

    @Override
    public void positiveButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {
        dialog.dismiss();
    }

    @Override
    public void negativeButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {
        dialog.dismiss();
    }

    /** Used to navigate to this activity **/
    public static void navigate(FragmentActivity activity, String finalPlaceId) {
        Intent intent = new Intent(activity, DetailsActivity.class);
        intent.putExtra(PARCELABLE_RESTAURANT, finalPlaceId);
        ActivityCompat.startActivity(activity, intent, null);
    }
}
