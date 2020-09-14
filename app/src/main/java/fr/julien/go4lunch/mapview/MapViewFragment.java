package fr.julien.go4lunch.mapview;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import fr.juju.googlemaplibrary.model.FinalPlace;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.FragmentMapViewBinding;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.home.HomeActivity;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.networking.ConnexionInternet;
import fr.julien.go4lunch.utils.Utils;
import fr.julien.go4lunch.viewmodel.RestaurantsViewModel;
import fr.julien.go4lunch.viewmodel.UserViewModel;

import java.io.IOException;
import java.util.List;


public class MapViewFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback, Utils.OnClickPositiveButtonDialog {

    private FragmentMapViewBinding binding;
    private NavController navController;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient client;
    private LatLng latLng;
    private double longitude, latitude;
    private RestaurantsViewModel restaurantsViewModel;
    private UserViewModel userViewModel;
    private String uId, uName, location;
    private float[] results = new float[1];
    private int distance, radius;
    private Utils utils;

    public MapViewFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMapViewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        ((HomeActivity)getActivity()).findViewById(R.id.toolbar_main).setVisibility(View.VISIBLE);
        ((HomeActivity)getActivity()).findViewById(R.id.bottom_navigation_view).setVisibility(View.VISIBLE);
        navController = Navigation.findNavController(view);
        uId = Injection.provideUserRepository().getCurrentUser().getUid();
        uName = Injection.provideUserRepository().getCurrentUser().getDisplayName();
        client = LocationServices.getFusedLocationProviderClient(getActivity());
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        utils = new Utils(this);
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15))

        this.configureUserViewModel();
        this.configureRestaurantsViewModel();
        binding.focusBtn.setOnClickListener(v -> updateLocationDialog());
    }


    @Override
    public void onResume() {
        super.onResume();
        this.checkPermission();
    }


    /** ***************************** **/
    /** ********* User Data  ******** **/
    /** ***************************** **/

    private void configureUserViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideUserViewModelFactory();
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
        userViewModel.init();
    }

    private void getCurrentUserFromFirestore(){
        userViewModel.getCurrentUserData().observe(getViewLifecycleOwner(), user -> {
            try {
                if (ConnexionInternet.isConnected()){
                    Location.distanceBetween(user.getLatitude(), user.getLongitude(), latitude, longitude, results);
                    distance = (int)results[0];
                    location = latitude + "," + longitude;
                    radius = user.getRadius();
                    Log.i("DEBUGGG", location);

                    restaurantsViewModel.getMyRestaurants(location, radius, user.getLongitude(), user.getLatitude(), distance)
                            .observe(getViewLifecycleOwner(), finalRestaurants -> {
                                if (finalRestaurants == null){
                                    updateLocationDialog();
                                }else {
                                    this.setMarkerOnMap(finalRestaurants);
                                }
                            });
                    userViewModel.updateUserLatLn(longitude, latitude);
                }else {
                    alertDialog("Connexion Required","Please connect your device and click \"Done\"",2);
                }
            } catch (InterruptedException | IOException e) {e.printStackTrace();}

        });
    }


    /** ***************************** **/
    /** ***** Restaurants Data  ***** **/
    /** ***************************** **/

    /** Configuring ViewModel **/
    private void configureRestaurantsViewModel(){
        ViewModelFactory viewModelFactory = Injection.provideRestaurantViewModelFactory(getViewLifecycleOwner());
        restaurantsViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantsViewModel.class);

    }

    private void addMarkerToMyPosition(){
        mMap.clear();
        Marker myPosition = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(uName)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location)));
        myPosition.setTag("myPosition");
    }

    /** Get Restaurants from Firestore and add Marker **/
    private void setMarkerOnMap(List<FinalPlace> finalPlaces){
        if (finalPlaces != null){
            this.addMarkerToMyPosition();
            for (FinalPlace finalPlace : finalPlaces) {
                String openingHours = "Opening hours dont set";
                int drawableId;
                if (finalPlace.getOpeningHours() != null) {
                    openingHours = finalPlace.getOpeningHours().get(Utils.getIndexOfToday());
                }
                if (finalPlace.getNbrCustomer() == 0) {
                    drawableId = R.drawable.baseline_place_unbook_24;
                } else {
                    drawableId = R.drawable.baseline_place_booked_24;
                }

                LatLng latLngRestaurant = new LatLng(finalPlace.getLatitude(), finalPlace.getLongitude());
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLngRestaurant)
                        .title(finalPlace.getName())
                        .snippet(openingHours)
                        .icon(BitmapDescriptorFactory.fromResource(drawableId)));
                marker.setTag(finalPlace.getPlaceId());

            }
        }else {
            alertDialog("No Match","Sorry we no found any place",2);
        }
    }

    /** ***************************** **/
    /** ***** Current Location  ***** **/
    /** ***************************** **/

    public void checkPermission(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        }else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }

    /** Get Current User Location **/
    private void getCurrentLocation() {
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null){
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mapFragment.getMapAsync(this);
                this.getCurrentUserFromFirestore();
            }
        });
    }

    /** ***************************** **/
    /** ***** Google Map Method ***** **/
    /** ***************************** **/

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(true);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),  R.raw.style_json));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (!marker.getTag().equals("myPosition")) {
            restaurantsViewModel.getRestaurantById((String) marker.getTag()).observe(getViewLifecycleOwner(),finalRestaurant -> {
                Bundle bundle = new Bundle();
                bundle.putParcelable("restaurant", finalRestaurant);
                navController.navigate(R.id.detailsFragment, bundle);
            });
        }
    }

    /** ***************************** **/
    /** ***** Search view Method **** **/
    /** ***************************** **/

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if(searchItem != null){
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {return true;}
                @Override
                public boolean onQueryTextSubmit(String query) {
                    restaurantsViewModel.getPlaceFromSearch(query, location, radius).observe(getViewLifecycleOwner(), finalRestaurants -> {
                        setMarkerOnMap(finalRestaurants);
                    });
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                // Not implemented here
                return false;
            default:
                break;
        }
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onOptionsItemSelected(item);
    }

    /** ******************************* **/
    /** ***** Alert Dialog Method **** **/
    /** ***************************** **/

    private void alertDialog(String dialogueTitle, String dialogueMessage, int id){
        utils.showAlertDialog(getContext(), dialogueTitle,dialogueMessage,"Done", "Cancel",
                R.drawable.background_alert_dialog, R.drawable.ic_warning_black_24dp, id);
    }

    private void updateLocationDialog(){
        utils.showAlertDialog(this.getContext(), "Update location","Your location change do you want update restaurant list ?",
                "Agree","DisAgree",
                R.drawable.background_alert_dialog, R.drawable.ic_warning_black_24dp, 1);
    }

    @Override
    public void positiveButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {
        switch (dialogIdForSwitch){
            case 1:
                try {
                    if (ConnexionInternet.isConnected()){
                        restaurantsViewModel.updateRestaurants(location, radius).observe(getViewLifecycleOwner(), this::setMarkerOnMap);
                        userViewModel.updateUserLatLn(longitude, latitude);
                    }else {
                        alertDialog("Connexion Required","Please connect your device and click \"Done\"",1);
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
                break;
            case 2:
                this.getCurrentUserFromFirestore();
                dialog.dismiss();
                break;
            default:
                dialog.dismiss();
                break;
        }

    }

    @Override
    public void negativeButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch) {
        switch (dialogIdForSwitch){
            case 1:
                Snackbar.make(binding.layoutMapView, "Data don't update", Snackbar.LENGTH_SHORT).show();
                dialog.dismiss();
                break;
            case 2:
                this.getCurrentUserFromFirestore();
                dialog.dismiss();
                break;
            default:
                dialog.dismiss();
                break;
        }
    }
}
