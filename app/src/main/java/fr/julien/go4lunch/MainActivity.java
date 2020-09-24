package fr.julien.go4lunch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import fr.julien.go4lunch.databinding.ActivityMainBinding;
import fr.julien.go4lunch.login.LoginFragment;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private ActivityMainBinding binding;
    public static final int ALERT_UPDATE_LOCATION_DIALOG_ID = 1;
    public static final int ALERT_CONNEXION_DIALOG_ID = 2;
    public static final int ALERT_NO_MATCH_DIALOG_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        showLoginFragment();
        binding.swipeRefresh.setOnRefreshListener(this);
    }

    private void showLoginFragment(){
        LoginFragment loginFragment = LoginFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_login, loginFragment).commit();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(() -> binding.swipeRefresh.setRefreshing(false), 1000);
        showLoginFragment();
    }

    /** Used to navigate to this activity **/
    public static void navigate(FragmentActivity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        ActivityCompat.startActivity(activity, intent, null);
        activity.finish();
    }
}