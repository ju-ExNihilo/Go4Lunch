package fr.julien.go4lunch.setting;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import fr.juju.googlemaplibrary.model.FinalPlace;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.chatroom.ChatRoomActivity;
import fr.julien.go4lunch.databinding.ActivityMainBinding;
import fr.julien.go4lunch.databinding.ActivitySettingBinding;
import fr.julien.go4lunch.details.DetailsActivity;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.home.HomeActivity;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.models.LikedRestaurant;
import fr.julien.go4lunch.utils.LikedRestaurantAdapter;
import fr.julien.go4lunch.utils.Utils;
import fr.julien.go4lunch.viewmodel.RestaurantsViewModel;
import fr.julien.go4lunch.viewmodel.UserViewModel;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import java.util.UUID;

public class SettingActivity extends AppCompatActivity implements Utils.OnClickButtonInpuDialog, Utils.OnClickItemListAlertDialog, Utils.OnClickButtonAlertDialog{

    private ActivitySettingBinding binding;
    private UserViewModel userViewModel;
    private RestaurantsViewModel restaurantsViewModel;
    private Utils utils;
    private String uId;
    private static final String PERMS = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int RC_IMAGE_PERMS = 100;
    private Uri uriImageSelected;
    private static final int RC_CHOOSE_PHOTO = 200;

   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        utils = new Utils(this, this, this);
        uId = Injection.provideUserRepository().getCurrentUser().getUid();
        this.configureToolbar();
        this.configureRestaurantsViewModel();
        this.configureUserViewModel();
        binding.cardLikedRestaurant.setOnClickListener(v -> showLikedRestaurant());
        binding.cardUpdateName.setOnClickListener(v -> inputDialogUpdatName());
        binding.cardUpdateScope.setOnClickListener(v -> inputDialogUpdatRadius());
        binding.cardUpdatePhoto.setOnClickListener(v -> onClickAddFile());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 2 - Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 6 - Calling the appropriate method after activity result
        this.handleResponse(requestCode, resultCode, data);
    }

    private void configureToolbar(){
        setSupportActionBar(binding.toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
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

    /** Used to navigate to this activity **/
    public static void navigate(FragmentActivity activity) {
        Intent intent = new Intent(activity, SettingActivity.class);
        ActivityCompat.startActivity(activity, intent, null);
    }

    private void updateName(String uid, String username){
        userViewModel.updateName(uid, username);
    }


    private void updateRadius(TextInputEditText textInputEditText){
        int scope = Integer.parseInt(textInputEditText.getText().toString());
        if (scope >= 2 && scope <= 10){
            int radius = scope*1000;
            userViewModel.updateRadius(radius);
            updateDataAfterRadiusChange();
            Toast.makeText(this, "Your scope have been updated", Toast.LENGTH_SHORT).show();
        }else {
            utils.showMessageDialog(this,"Warning", "Your scope must be between 2kn and 10km", "Ok",
                    R.drawable.background_alert_dialog, R.drawable.ic_warning_black_24dp, 3);
        }

    }

    private void updateDataAfterRadiusChange(){
        userViewModel.getCurrentUserData().observe(this, user -> {
            String location = user.getLatitude()+","+user.getLongitude();
            restaurantsViewModel.updateRestaurants(location, user.getRadius());
        });
    }

    private void handleResponse(int requestCode, int resultCode, Intent data){
        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                this.uriImageSelected = data.getData();
                userViewModel.updateUserPicture(this.uriImageSelected);
                Toast.makeText(this, "Your profile picture have been updated", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, getString(R.string.toast_title_no_image_chosen), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @AfterPermissionGranted(RC_IMAGE_PERMS)
    public void onClickAddFile() {
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_files_access), RC_IMAGE_PERMS, PERMS);
            return;
        }
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RC_CHOOSE_PHOTO);
    }


    private void inputDialogUpdatName(){
        utils.showAlertInputDialog(this, "Update Your Pseudo", "Enter your new pseudo :", "Save",
                "Cancel","Pseudo", InputType.TYPE_CLASS_TEXT, R.drawable.background_alert_dialog, R.drawable.ic_settings, 1);
    }


    private void inputDialogUpdatRadius(){
        userViewModel.getCurrentUserData().observe(this, user -> {
            int scope = user.getRadius()/1000;
            utils.showAlertInputDialog(this, "Update Your Scope", "Update your search scope in km. Actual ("+ scope +"km)", "Save",
                    "Cancel","Scope", InputType.TYPE_CLASS_NUMBER , R.drawable.background_alert_dialog, R.drawable.ic_settings, 2);
        });

    }

    private void showLikedRestaurant(){
        userViewModel.getLikedRestaurants().observe(this, likedRestaurants -> {
            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.head_dialog_alert, null);
            TextView title = alertLayout.findViewById(R.id.head_dialog_title);
            title.setText("Your Liked Restaurants :");
            utils.showAlertListDialog(this, alertLayout,R.drawable.background_alert_dialog, R.drawable.ic_favorite,
                    new LikedRestaurantAdapter(this, R.layout.item_assignment_dialog_list_layout, likedRestaurants));
        });
    }



    @Override
    public void onItemListDialogClicked(DialogInterface dialog, ArrayAdapter arrayAdapter, int position) {
        LikedRestaurant likedRestaurant = (LikedRestaurant) arrayAdapter.getItem(position);
        DetailsActivity.navigate(this, likedRestaurant.getId());
    }

    @Override
    public void onClickedPositiveButtonInpuDialog(DialogInterface dialog, TextInputEditText textInputEditText, int dialogIdForSwitch) {
        switch (dialogIdForSwitch){
            case 1:
                updateName(uId, textInputEditText.getText().toString());
                Toast.makeText(this, "Your pseudo have been updated", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                updateRadius(textInputEditText);
                break;

        }
    }

    @Override
    public void onClickedNegativeButtonInpuDialog(DialogInterface dialog) {dialog.dismiss(); }

    @Override
    public void positiveButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {}

    @Override
    public void negativeButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {dialog.dismiss();}
}
