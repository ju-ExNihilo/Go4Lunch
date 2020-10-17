package fr.julien.go4lunch.details;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
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
        this.configureUserViewModel();
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

    /** Configure restaurant ViewModel **/
    private void configureRestaurantsViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideRestaurantViewModelFactory(this, this);
        restaurantsViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantsViewModel.class);
    }

    /** Configure user ViewModel **/
    private void configureUserViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideUserViewModelFactory();
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
    }

    /** ********************************** **/
    /** ******* Init View Method  ******* **/
    /** ******************************** **/

    /** initialization view **/
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

        binding.restaurantDetailsFloatingBtn.setOnClickListener(v -> this.onClickFloatingActionBtn(restaurant.getName(), restaurant.getPlaceId(), restaurant.getAddress()));
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

    /** ********************************** **/
    /** ******** Toolbar Method  ******** **/
    /** ******************************** **/

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

    /** ********************************** **/
    /** ******** Action Method  ********* **/
    /** ******************************** **/

    private void onClickFloatingActionBtn(String eatingPlaceName, String eatingPlaceId, String eatingPlaceAddress ){
        if (!isCustomer){
            Utils.rotateAnimation(binding.restaurantDetailsFloatingBtn, 360, R.drawable.ic_check_circle_black_24dp);
            isCustomer = true;
            userViewModel.updateEatingPlace(uId, eatingPlaceName, eatingPlaceId);
        }else {
            Utils.rotateAnimation(binding.restaurantDetailsFloatingBtn, 360, R.drawable.baseline_restaurant_24);
            isCustomer = false;
            userViewModel.updateEatingPlace(uId, getString(R.string.none), getString(R.string.none));
        }
        this.notificationWorker(this, eatingPlaceName, eatingPlaceId, eatingPlaceAddress);
    }

    /** init worker for notification **/
    private void notificationWorker(Context context, String eatingPlaceName, String eatingPlaceId, String eatingPlaceAddress){
        Data data = new Data.Builder()
                .putString(EatingPlaceNotificationWorker.KEY_EATING_PLACE, eatingPlaceName)
                .putString(EatingPlaceNotificationWorker.USER_NAME, userViewModel.getCurrentUser().getDisplayName())
                .putString(EatingPlaceNotificationWorker.KEY_EATING_PLACE_ID, eatingPlaceId)
                .putString(EatingPlaceNotificationWorker.KEY_EATING_PLACE_ADDRESS, eatingPlaceAddress)
                .putString(EatingPlaceNotificationWorker.KEY_NOTIF_MESSAGE_JOIN, getString(R.string.notification_joining))
                .putString(EatingPlaceNotificationWorker.KEY_NOTIF_TITLE, getString(R.string.notification_title))
                .putString(EatingPlaceNotificationWorker.KEY_NOTIF_MESSAGE, getString(R.string.notification_message))
                .build();
        OneTimeWorkRequest dailyWorkRequest  = new OneTimeWorkRequest.Builder(EatingPlaceNotificationWorker.class)
                .setInitialDelay(utils.getMillisecondeUntilAHours(12,0), TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(getString(R.string.notif), ExistingWorkPolicy.REPLACE, dailyWorkRequest) ;
    }
    /** ********************************** **/
    /** ***** RecyclerView Method  ****** **/
    /** ******************************** **/

    /** Configure RecyclerView **/
    private void configureRecyclerView(String placeId){
        binding.restaurantDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterUser = new AdapterUser(userViewModel.getCustomer(placeId), true, this);
        binding.restaurantDetailsRecyclerView.setAdapter(adapterUser);
    }

    @Override
    public void onWorkmateItemClicked(String restaurantId) {}

    @Override
    public void onChatButtonClicked(String userId, String userName) {
        userViewModel.getCurrentUserData().observe(this, user -> {
            ChatRoomActivity.navigate(this, userId, userName, user.getUrlPicture());
        });
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

    /** ********************************** **/
    /** ***** NavigationBar Method  ***** **/
    /** ******************************** **/

    private void configureBotomNavigationView(){
        binding.restaurantDetailsNavigation.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            switch (id){
                case R.id.call_details:
                    callRestaurant();
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

    private void callRestaurant(){
        restaurantsViewModel.getRestaurantById(restaurantId).observe(this, restaurant -> {
            if (!restaurant.getPhone().equals(getString(R.string.phone_dont_set))){
                String tel = restaurant.getPhone().replace(" ", "");
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + tel));
                startActivity(callIntent);
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
                alertDialog(1,getString(R.string.sorry_no_website));
            }
        });
    }

    /** ********************************* **/
    /** ***** Alert Dialog Method  ***** **/
    /** ******************************* **/

    private void alertDialog(int id, String message){
        utils.showAlertDialog(this, getString(R.string.warning),message,
                getString(R.string.ok_btn), getString(R.string.cancel_btn),R.drawable.background_alert_dialog, R.drawable.ic_warning_black_24dp, id);
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
