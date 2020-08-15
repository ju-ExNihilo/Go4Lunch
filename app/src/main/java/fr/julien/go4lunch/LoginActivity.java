package fr.julien.go4lunch;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.Objects;

import fr.julien.go4lunch.databinding.ActivityLoginBinding;
import fr.julien.go4lunch.home.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        this.rooting();
        this.onClickGoogleLoginButton();
        this.onClickFacebookLoginButton();
        this.onClickTwitterLoginButton();
        this.onClickMailLoginButton();
        setContentView(view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IdpResponse response = IdpResponse.fromResultIntent(data);
        if (requestCode == RC_SIGN_IN) {
            this.handleResponseAfterSignIn(response);
        }
    }

    /** Rooting **/
    public void rooting(){
        new Handler().postDelayed(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null){
                HomeActivity.navigate(this);
            }else {
                binding.loadingPanel.setVisibility(View.GONE);
            }
        }, 3*1000); // wait for 3 seconds
    }

    /** Login with Google **/
    public void onClickGoogleLoginButton() {
        binding.googleButton.setOnClickListener(v -> startSignInActivity(new AuthUI.IdpConfig.GoogleBuilder().build()) );
    }
    /** Login with Facebook **/
    public void onClickFacebookLoginButton() {
        binding.facebookButton.setOnClickListener(v -> startSignInActivity(new AuthUI.IdpConfig.FacebookBuilder().build()) );
    }
    /** Login with Twitter **/
    public void onClickTwitterLoginButton() {
        binding.twitterButton.setOnClickListener(v -> startSignInActivity(new AuthUI.IdpConfig.TwitterBuilder().build()) );
    }
    /** Login with Mail **/
    public void onClickMailLoginButton() {
        binding.idenButton.setOnClickListener(v -> startSignInActivity(new AuthUI.IdpConfig.EmailBuilder().build()) );
    }

    /** Login with FirebaseUI **/
    private void startSignInActivity(AuthUI.IdpConfig authUI){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                                Collections.singletonList(authUI))
                        .setIsSmartLockEnabled(false, true)
                        .build(),
                RC_SIGN_IN);
    }


    private void showSnackBar(View view, String message){
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    private void handleResponseAfterSignIn(IdpResponse response){
        if (response == null) {
            showSnackBar(binding.constrainLayout, getString(R.string.error_authentication_canceled));
        } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
            showSnackBar(binding.constrainLayout, getString(R.string.error_no_internet));
        } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
            showSnackBar(binding.constrainLayout, getString(R.string.error_unknown_error));
        }else {
            HomeActivity.navigate(this);
        }
    }
}
