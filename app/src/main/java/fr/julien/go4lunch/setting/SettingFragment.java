package fr.julien.go4lunch.setting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.FragmentSettingBinding;
import fr.julien.go4lunch.databinding.FragmentWorkmatesBinding;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.home.HomeActivity;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.utils.Utils;
import fr.julien.go4lunch.viewmodel.RestaurantsViewModel;
import fr.julien.go4lunch.viewmodel.UserViewModel;

public class SettingFragment extends Fragment implements Utils.OnClickPositiveButtonDialog{

    private FragmentSettingBinding binding;
    private UserViewModel userViewModel;
    private RestaurantsViewModel restaurantsViewModel;
    private String uId;

    public SettingFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((HomeActivity)getActivity()).findViewById(R.id.toolbar_main).setVisibility(View.VISIBLE);
        ((HomeActivity)getActivity()).findViewById(R.id.bottom_navigation_view).setVisibility(View.VISIBLE);

        uId = Injection.provideUserRepository().getCurrentUser().getUid();

        this.configureRestaurantsViewModel();
        this.configureUserViewModel();

        binding.saveBtn.setOnClickListener(v -> {
            if (!binding.editTextName.getText().toString().isEmpty()){
                this.updateName(uId, binding.editTextName.getText().toString());
            }
            if (!binding.editTextRadius.getText().toString().isEmpty()){
                this.updateRadius(uId, Integer.parseInt(binding.editTextRadius.getText().toString()));
                alertDialog(1);
            }
        });
    }

    /** Configuring ViewModel **/
    private void configureUserViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideUserViewModelFactory();
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
        userViewModel.init();
    }
    /** Configuring ViewModel **/
    private void configureRestaurantsViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideRestaurantViewModelFactory();
        restaurantsViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantsViewModel.class);
    }

    private void updateName(String uid, String username){
        userViewModel.updateName(uid, username);
    }

    private void updateRadius(String uid, int radius){
        userViewModel.updateRadius(uid, radius);
    }

    private void updateDataAfterRadiusChange(){
        userViewModel.getCurrentUserData().observe(getViewLifecycleOwner(), user -> {
            String location = user.getLatitude()+","+user.getLongitude();
            restaurantsViewModel.updateRestaurants(location, user.getRadius(), this);
        });
    }

    private void alertDialog(int id){
        Utils utils = new Utils(this);
        utils.showAlertDialog(this.getContext(), "Warning !","Do you want update your restaurants list !!",
                "Yes", "Cancel",
                R.drawable.background_alert_dialog, R.drawable.ic_warning_black_24dp, id);
    }

    @Override
    public void positiveButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {
        this.updateDataAfterRadiusChange();
        dialog.dismiss();
    }

    @Override
    public void negativeButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {
        dialog.dismiss();
    }
}
