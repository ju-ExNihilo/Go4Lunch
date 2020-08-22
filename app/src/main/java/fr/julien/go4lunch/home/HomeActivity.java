package fr.julien.go4lunch.home;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.navigation.NavigationView;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.ActivityHomeBinding;
import fr.julien.go4lunch.injection.Injection;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityHomeBinding binding;
    private NavController navController;

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
        // Get the intent, verify the action and get the query
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch(String query) {
        this.getSearchUser(query);
    }

    /** Get All Users **/
    private void getSearchUser(String query){
        Bundle bundle = new Bundle();
        bundle.putString("message", query );
        navController.navigate(R.id.workmatesFragment, bundle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        return true;
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

        name.setText(Injection.provideUserRepository().getCurentUser().getDisplayName());
        email.setText(Injection.provideUserRepository().getCurentUser().getEmail());
        Glide.with(pic.getContext())
                .load(Injection.provideUserRepository().getCurentUser().getPhotoUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(pic);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        switch (id){
            case R.id.your_lunch :
                Log.i("DEBUGGG","your lunch");
                break;
            case R.id.settings:
                Log.i("DEBUGGG","settings");
                break;
            case R.id.logout:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnSuccessListener(aVoid -> finish());
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
    }

}
