package fr.julien.go4lunch.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.ActivityHomeBinding;
import fr.julien.go4lunch.workmates.WorkmatesFragment;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        this.configureBottomView();
        this.showListUsers();
        setContentView(view);
    }

    /** listener for Bottom Navigation View **/
    private Boolean updateMainFragment(Integer integer){
        switch (integer) {
            case R.id.action_go_map:
                showSnackBar(binding.constrainLayoutHome, getString(R.string.go_map));
                break;
            case R.id.action_go_list_restaurant:
                showSnackBar(binding.constrainLayoutHome, getString(R.string.go_list));
                break;
            case R.id.action_go_list_users:
                this.showListUsers();
                break;
        }
        return true;
    }

    private void showSnackBar(View view, String message){
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    private void showListUsers(){
        WorkmatesFragment workmatesFragment = (WorkmatesFragment) getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (workmatesFragment == null){
            workmatesFragment = WorkmatesFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, workmatesFragment).commit();
        }
    }


    /** Configuring Bottom Navigation View **/
    private void configureBottomView(){
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> updateMainFragment(item.getItemId()));
    }

    /** Used to navigate to this activity **/
    public static void navigate(FragmentActivity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        ActivityCompat.startActivity(activity, intent, null);
        activity.finish();
    }
}