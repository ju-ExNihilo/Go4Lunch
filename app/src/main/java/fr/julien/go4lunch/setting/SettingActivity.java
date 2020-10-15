package fr.julien.go4lunch.setting;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.ActivitySettingBinding;
import fr.julien.go4lunch.details.DetailsActivity;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.models.LikedRestaurant;
import fr.julien.go4lunch.utils.LikedRestaurantAdapter;
import fr.julien.go4lunch.utils.Utils;
import fr.julien.go4lunch.viewmodel.RestaurantsViewModel;
import fr.julien.go4lunch.viewmodel.UserViewModel;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SettingActivity extends AppCompatActivity implements Utils.OnClickButtonInpuDialog, Utils.OnClickItemListAlertDialog, Utils.OnClickButtonAlertDialog{

    private ActivitySettingBinding binding;

    private UserViewModel userViewModel;
    private RestaurantsViewModel restaurantsViewModel;
    private Utils utils;
    private String uId;
    private static final String PERMS = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int RC_IMAGE_PERMS = 100;
    private static final int RC_CHOOSE_PHOTO = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        this.configureToolbar();
        this.configureRestaurantsViewModel();
        this.configureUserViewModel();
        utils = new Utils(this, this, this);
        uId = userViewModel.getCurrentUser().getUid();
        binding.cardLikedRestaurant.setOnClickListener(v -> showLikedRestaurant());
        binding.cardUpdateName.setOnClickListener(v -> inputDialogUpdatName());
        binding.cardUpdateScope.setOnClickListener(v -> inputDialogUpdatRadius());
        binding.cardUpdatePhoto.setOnClickListener(v -> onClickAddFile());
    }

    /** Configuring User ViewModel **/
    private void configureUserViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideUserViewModelFactory();
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
    }
    /** Configuring Restaurant ViewModel **/
    private void configureRestaurantsViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideRestaurantViewModelFactory(this, this);
        restaurantsViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantsViewModel.class);
    }

    /** ********************************* **/
    /** ******** Toolbar Method  ******** **/
    /** ******************************* **/

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
        setSupportActionBar(binding.toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    /** ********************************* **/
    /** ******** Update Method  ******** **/
    /** ******************************* **/

    private void updateName(String uid, String username){
        if (!username.isEmpty()){
            userViewModel.updateName(uid, username);
            Toast.makeText(this, getString(R.string.pseudo_updated), Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, getString(R.string.pseudo_dont_updated), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRadius(TextInputEditText textInputEditText){
        if (!textInputEditText.getText().toString().isEmpty()){
            int scope = Integer.parseInt(textInputEditText.getText().toString());
            if (scope >= 2 && scope <= 10){
                int radius = scope*1000;
                userViewModel.updateRadius(radius);
                updateDataAfterRadiusChange();
                Toast.makeText(this, getString(R.string.scope_updated), Toast.LENGTH_SHORT).show();
            }else {
                utils.showMessageDialog(this,getString(R.string.warning), getString(R.string.scope_between), getString(R.string.ok_btn),
                        R.drawable.background_alert_dialog, R.drawable.ic_warning_black_24dp, 3);
            }
        }else {
            Toast.makeText(this, getString(R.string.scope_dont_updated), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDataAfterRadiusChange(){
        userViewModel.getCurrentUserData().observe(this, user -> {
            String location = user.getLatitude()+","+user.getLongitude();
            restaurantsViewModel.updateRestaurants(location, user.getRadius());
        });
    }

    /** *********************************** **/
    /** ***** Update picture Method  ***** **/
    /** ********************************* **/

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponse(requestCode, resultCode, data);
    }

    private void handleResponse(int requestCode, int resultCode, Intent data){
        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                Uri uriImageSelected = data.getData();
                userViewModel.updateUserPicture(uriImageSelected);
                Toast.makeText(this, getString(R.string.picture_updated), Toast.LENGTH_SHORT).show();

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

    /** ********************************* **/
    /** ***** Alert Dialog Method  ***** **/
    /** ******************************* **/

    private void inputDialogUpdatName(){
        utils.showAlertInputDialog(this, getString(R.string.update_pseudo), getString(R.string.enter_pseudo), getString(R.string.save),
                getString(R.string.cancel),getString(R.string.pseudo), InputType.TYPE_CLASS_TEXT, R.drawable.background_alert_dialog, R.drawable.ic_settings, 1);
    }


    private void inputDialogUpdatRadius(){
        userViewModel.getCurrentUserData().observe(this, user -> {
            int scope = user.getRadius()/1000;
            utils.showAlertInputDialog(this, getString(R.string.update_scope), getString(R.string.error_scope, scope), getString(R.string.save),
                    getString(R.string.cancel),getString(R.string.scope), InputType.TYPE_CLASS_NUMBER , R.drawable.background_alert_dialog, R.drawable.ic_settings, 2);
        });

    }

    private void showLikedRestaurant(){
        userViewModel.getLikedRestaurants().observe(this, likedRestaurants -> {
            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.head_dialog_alert, null);
            TextView title = alertLayout.findViewById(R.id.head_dialog_title);
            title.setText(R.string.your_liked_restaurants);
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

    /** Used to navigate to this activity **/
    public static void navigate(FragmentActivity activity) {
        Intent intent = new Intent(activity, SettingActivity.class);
        ActivityCompat.startActivity(activity, intent, null);
    }
}
