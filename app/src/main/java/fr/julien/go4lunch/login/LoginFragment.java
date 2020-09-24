package fr.julien.go4lunch.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import fr.julien.go4lunch.MainActivity;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.FragmentLoginBinding;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.home.HomeActivity;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.models.User;
import fr.julien.go4lunch.networking.ConnexionInternet;
import fr.julien.go4lunch.utils.Utils;
import fr.julien.go4lunch.viewmodel.UserViewModel;

import java.io.IOException;
import java.util.Collections;

public class LoginFragment extends Fragment implements Utils.OnClickButtonAlertDialog {

    private FragmentLoginBinding binding;
    private UserViewModel userViewModel;
    private static final int RC_SIGN_IN = 123;


    public static LoginFragment newInstance() {return new LoginFragment();}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.configureViewModel();
        this.rooting();
        this.onClickFacebookLoginButton();
        this.onClickGoogleLoginButton();
        this.onClickMailLoginButton();
        this.onClickTwitterLoginButton();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            this.handleResponseAfterSignIn(response);
        }
    }

    private void handleResponseAfterSignIn(IdpResponse response) {
        if (response != null) {
            if (this.getCurrentUser() != null){
                this.createUserInFireStore();
                HomeActivity.navigate(this.getActivity());
            }else {
                showSnackBar(binding.constrainLayout, getString(R.string.error_unknown_error));
            }
        } else {
            showSnackBar(binding.constrainLayout, getString(R.string.error_authentication_canceled));
        }
    }

    /** Configure user ViewModel **/
    private void configureViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideUserViewModelFactory();
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
    }

    /** Rooting **/
    public void rooting(){
        new Handler().postDelayed(() -> {

            try {
                if (ConnexionInternet.isConnected()){
                    if (FirebaseAuth.getInstance().getCurrentUser() != null){
                        HomeActivity.navigate(this.getActivity());
                    }else {
                        binding.loadingPanel.setVisibility(View.GONE);
                    }
                }else {
                    Utils utils = new Utils(this);
                    utils.showAlertDialog(this.getContext(), getString(R.string.connexion_required),getString(R.string.please_connect),getString(R.string.done), getString(R.string.cancel),
                            R.drawable.background_alert_dialog, R.drawable.ic_warning_black_24dp, MainActivity.ALERT_CONNEXION_DIALOG_ID);
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }

        }, 2*1000); // wait for 2 seconds
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

    /** Create user for FireStore **/
    private void createUserInFireStore(){
        if (this.getCurrentUser() != null){
            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : getString(R.string.url_default_pic);
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();
            User userToCreate = new User(uid,username, urlPicture);
            userViewModel.getCurrentUserData().observe(getViewLifecycleOwner(), user -> {
                if (user == null){
                    userViewModel.createUser(userToCreate);
                }
            });

        }
    }

    /** Get Current User **/
    private FirebaseUser getCurrentUser(){ return userViewModel.getCurrentUser(); }

    private void showSnackBar(View view, String message){
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void positiveButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {
        this.rooting();
        dialog.dismiss();
    }

    @Override
    public void negativeButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {
        this.rooting();
        dialog.dismiss();
    }
}
