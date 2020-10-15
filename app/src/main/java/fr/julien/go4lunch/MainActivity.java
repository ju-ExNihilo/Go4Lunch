package fr.julien.go4lunch;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import fr.julien.go4lunch.login.LoginFragment;


public class MainActivity extends AppCompatActivity {

    public static final int ALERT_UPDATE_LOCATION_DIALOG_ID = 1;
    public static final int ALERT_CONNEXION_DIALOG_ID = 2;
    public static final int ALERT_NO_MATCH_DIALOG_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showLoginFragment();
    }

    private void showLoginFragment(){
        LoginFragment loginFragment = LoginFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_login, loginFragment).commit();
    }

    /** Used to navigate to this activity **/
    public static void navigate(FragmentActivity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        ActivityCompat.startActivity(activity, intent, null);
        activity.finish();
    }
}