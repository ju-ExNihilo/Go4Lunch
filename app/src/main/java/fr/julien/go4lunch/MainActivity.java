package fr.julien.go4lunch;

import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import fr.julien.go4lunch.databinding.ActivityMainBinding;
import fr.julien.go4lunch.home.HomeActivity;
import fr.julien.go4lunch.login.LoginFragment;
import fr.julien.go4lunch.worker.ClearEatingPlaceWorker;

import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private ActivityMainBinding binding;
    public static final String KEY_API = "AIzaSyCU1WJIZDjML_4NwlFJdVJQcjwEzls7iOo";
    public static final int ALERT_UPDATE_LOCATION_DIALOG_ID = 1;
    public static final int ALERT_CONNEXION_DIALOG_ID = 2;
    public static final int ALERT_NO_MATCH_DIALOG_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        showBooksListFragment();
        binding.swipeRefresh.setOnRefreshListener(this);
    }

    private void showBooksListFragment(){
        LoginFragment loginFragment = LoginFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_login, loginFragment).commit();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(() -> binding.swipeRefresh.setRefreshing(false), 1000);
        showBooksListFragment();
    }

    /** Used to navigate to this activity **/
    public static void navigate(FragmentActivity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        ActivityCompat.startActivity(activity, intent, null);
        activity.finish();
    }
}