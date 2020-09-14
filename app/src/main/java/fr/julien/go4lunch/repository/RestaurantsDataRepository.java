package fr.julien.go4lunch.repository;

import android.util.Log;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import fr.julien.go4lunch.MainActivity;
import fr.julien.go4lunch.models.FinalRestaurant;
import fr.julien.go4lunch.models.Inbox;
import fr.julien.go4lunch.models.autocomplete.PlaceSearch;
import fr.julien.go4lunch.models.autocomplete.Prediction;
import fr.julien.go4lunch.models.nearbysearch.Restaurants;
import fr.julien.go4lunch.models.nearbysearch.RestaurantsResult;
import fr.julien.go4lunch.models.place.Place;
import fr.julien.go4lunch.networking.GooglePlaceService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RestaurantsDataRepository {

    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<FinalRestaurant>> restaurants = new MutableLiveData<>();
    private final MutableLiveData<Restaurants> restaurantsResult = new MutableLiveData<>();
    private final GooglePlaceService googlePlaceService;
    private final String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static final String COLLECTION_USER = "users";
    private static final String COLLECTION_RESTAURANT = "MyResto";
    private static final String FIELD = "name,rating,formatted_phone_number,photo,address_component,url,geometry/location,opening_hours,place_id,website,price_level,vicinity";
    private FinalRestaurant finalRestaurant;
    private List<FinalRestaurant> finalRestaurantsArrayList = new ArrayList<>();


    public RestaurantsDataRepository(GooglePlaceService googlePlaceService) {
        this.googlePlaceService = googlePlaceService;
    }

    private CollectionReference getMyRestaurantsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_USER).document(uId).collection(COLLECTION_RESTAURANT);
    }

    /** **** return final restaurant list, if first connection get restaurants from retrofit, if you new location   **** **/
    public MutableLiveData<List<FinalRestaurant>> getMyRestaurants(String location, int radius, double longitude, double latitude, int distance, LifecycleOwner owner){

        if (latitude == 0 && longitude == 0){
            return getFinalPlace(location, radius, owner);
        }
        if ((longitude != 0 && latitude != 0) && (distance > radius)){
            restaurants.setValue(null);
            return  restaurants;
        }
        else {
            return getRestaurants();
        }
    }

    /** ***************************** **/
    /** ******** GET Method  ******** **/
    /** ***************************** **/

    /** **** Get final restaurant from firestore  **** **/
    public MutableLiveData<List<FinalRestaurant>> getRestaurants(){
        MutableLiveData<List<FinalRestaurant>> resto = new MutableLiveData<>();
        getMyRestaurantsCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                final int[] c = {0};
                List<FinalRestaurant> finalRestaurantsGetRestaurants = task.getResult().toObjects(FinalRestaurant.class);
                for (FinalRestaurant finalRestaurantGetRestaurants : finalRestaurantsGetRestaurants){
                    int n = finalRestaurantsGetRestaurants.size();
                    firebaseFirestore.collection("users")
                            .whereEqualTo("eatingPlaceId", finalRestaurantGetRestaurants.getPlaceId()).get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                updateNbrCustomer(finalRestaurantGetRestaurants.getPlaceId(), queryDocumentSnapshots.size());
                                finalRestaurantGetRestaurants.setNbrCustomer(queryDocumentSnapshots.size());
                                c[0]++;
                                if (c[0] ==n){
                                    resto.setValue(finalRestaurantsGetRestaurants);
                                }
                            });
                }
            }else {
                resto.setValue(null);}
        });
        return resto;
    }

    /** **** Get final restaurant for recycler view  **** **/
    public FirestoreRecyclerOptions<FinalRestaurant> getRestaurantForRecyclerView(){
        Query query = getMyRestaurantsCollection();
        return new FirestoreRecyclerOptions.Builder<FinalRestaurant>()
                .setQuery(query, FinalRestaurant.class)
                .build();
    }

    /** **** Get final restaurant from google api  **** **/
    public MutableLiveData<List<FinalRestaurant>> getFinalPlace(String location, int radius, LifecycleOwner owner){
        MutableLiveData<List<FinalRestaurant>> resto = new MutableLiveData<>();
        final int[] c = {0};
        getPlace(location, radius, owner).observe(owner, finalRestaurants -> {
            int n = finalRestaurants.size();
            for (FinalRestaurant finalRestaurant : finalRestaurants) {
                firebaseFirestore.collection("users")
                        .whereEqualTo("eatingPlaceId", finalRestaurant.getPlaceId()).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            finalRestaurant.setNbrCustomer(queryDocumentSnapshots.size());
                            c[0]++;
                            if (c[0] == n){
                                resto.setValue(finalRestaurants);
                            }
                        });
            }
        });
        return resto;
    }


    /** **** Get restaurant by id  **** **/
    public MutableLiveData<FinalRestaurant> getRestaurantById(String rId){
        MutableLiveData<FinalRestaurant> resto = new MutableLiveData<>();
        getMyRestaurantsCollection().document(rId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                resto.setValue(documentSnapshot.toObject(FinalRestaurant.class));
            }else {
                resto.setValue(null);
            }
        }).addOnFailureListener(e -> resto.setValue(null));
        return resto;
    }

    /** **** Get restaurant from retrofit  **** **/
    public MutableLiveData<Restaurants> getRestaurantsFromRetrofit(String location, int radius){
        googlePlaceService.getRestaurants(location, radius,"restaurant", MainActivity.KEY_API).enqueue(new Callback<Restaurants>() {
            @Override
            public void onResponse(Call<Restaurants> call, Response<Restaurants> response) {
                if (response.isSuccessful()){
                    restaurantsResult.setValue(response.body());
                }else {
                    restaurantsResult.setValue(null);
                }
            }
            @Override
            public void onFailure(Call<Restaurants> call, Throwable t) {
                restaurantsResult.setValue(null);}
        });
        return restaurantsResult;
    }

    /** **** Get restaurant from auto complete search  **** **/
    public MutableLiveData<PlaceSearch> getRestaurantsFromSearch(String input, String location, int radius){
        MutableLiveData<PlaceSearch> placeSearch = new MutableLiveData<>();
        googlePlaceService.getPlaceAutoComplete(input,"establishment",location, radius,"", MainActivity.KEY_API, "1234567890").enqueue(new Callback<PlaceSearch>() {
            @Override
            public void onResponse(Call<PlaceSearch> call, Response<PlaceSearch> response) {
                if (response.isSuccessful()){
                    placeSearch.setValue(response.body());
                }else {
                    placeSearch.setValue(null);
                }
            }
            @Override
            public void onFailure(Call<PlaceSearch> call, Throwable t) {
                placeSearch.setValue(null);}
        });
        return placeSearch;
    }

    /** ***************************** **/
    /** ******* Create Method  ****** **/
    /** ***************************** **/

    /** **** Insert restaurant in firestore  **** **/
    public Task<Void> createRestaurant(FinalRestaurant finalRestaurant) {
        return getMyRestaurantsCollection().document(finalRestaurant.getPlaceId())
                .set(finalRestaurant);
    }

    /** ********************************* **/
    /** ******** Delete Method  ******** **/
    /** ******************************* **/

    public Task<Void> deleteRestaurant(String restaurantId){
        return getMyRestaurantsCollection().document(restaurantId).delete();
    }

    /** ******************************* **/
    /** ******** Update Method  ****** **/
    /** ***************************** **/

    public Task<Void> updateNbrCustomer(String restaurantId, int nbrCustomer){
        return getMyRestaurantsCollection().document(restaurantId).update("nbrCustomer", nbrCustomer);
    }

    /** **** Reload restaurants data  **** **/
    public MutableLiveData<List<FinalRestaurant>> updateRestaurants(String location, int radius, LifecycleOwner owner){
        MutableLiveData<List<FinalRestaurant>> resto = new MutableLiveData<>();
        getMyRestaurantsCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                int nbrRestaurants = task.getResult().toObjects(FinalRestaurant.class).size();
                int counter = 0;
                for (FinalRestaurant finalRestaurant : task.getResult().toObjects(FinalRestaurant.class)) {
                    deleteRestaurant(finalRestaurant.getPlaceId());
                    counter++;
                    if (nbrRestaurants == counter) {
                        final int[] c = {0};
                        getPlace(location, radius, owner).observe(owner, finalRestaurants -> {
                            int n = finalRestaurants.size();
                            for (FinalRestaurant finalRestaurant12 : finalRestaurants) {
                                firebaseFirestore.collection("users")
                                        .whereEqualTo("eatingPlaceId", finalRestaurant12.getPlaceId()).get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            finalRestaurant12.setNbrCustomer(queryDocumentSnapshots.size());
                                            c[0]++;
                                            if (c[0] == n){
                                                resto.setValue(finalRestaurants);
                                            }
                                        });
                            }
                        });
                    }
                }
            }
        });
        return resto;
    }

    /** *********************************************** **/
    /** **** Get details restaurant, Utils Method  **** **/
    /** ********************************************* **/

    public MutableLiveData<FinalRestaurant> getPlaceDetailsInfoFromApi(String placeId){
        MutableLiveData<FinalRestaurant> restaurantDetailsInfo = new MutableLiveData<>();
        googlePlaceService.getPlaceInfo(placeId, FIELD, MainActivity.KEY_API).enqueue(new Callback<Place>() {
            @Override
            public void onResponse(Call<Place> call, Response<Place> response) {
                if (response.isSuccessful()){
                    addPlace(response.body());
                    restaurantDetailsInfo.setValue(finalRestaurant);
                }else {
                    restaurantDetailsInfo.setValue(null);
                }
            }
            @Override
            public void onFailure(Call<Place> call, Throwable t) {restaurantDetailsInfo.setValue(null);}
        });
        return restaurantDetailsInfo;
    }

    public MutableLiveData<List<FinalRestaurant>> getPlace(String location, int radius, LifecycleOwner owner){
        MutableLiveData<List<FinalRestaurant>> resto = new MutableLiveData<>();
        finalRestaurantsArrayList.clear();
        getRestaurantsFromRetrofit(location, radius).observe(owner, restaurants -> {
            final int[] c = {0};
            for (RestaurantsResult restaurants1 : restaurants.getRestaurantsResults()){
                int n = restaurants.getRestaurantsResults().size();
                googlePlaceService.getPlaceInfo(restaurants1.getPlaceId(), FIELD,MainActivity.KEY_API).enqueue(new Callback<Place>() {
                    @Override
                    public void onResponse(Call<Place> call, Response<Place> response) {
                        addPlace(response.body());
                        finalRestaurantsArrayList.add(finalRestaurant);
                        c[0]++;
                        if (c[0] == n){ resto.setValue(finalRestaurantsArrayList);}
                    }
                    @Override
                    public void onFailure(Call<Place> call, Throwable t) {}
                });
            }
        });
        return resto;
    }

    public MutableLiveData<List<FinalRestaurant>> getPlaceFromSearch(String input, String location, int radius, LifecycleOwner owner){
        MutableLiveData<List<FinalRestaurant>> restoFromSearch = new MutableLiveData<>();
        finalRestaurantsArrayList.clear();
        getRestaurantsFromSearch(input, location, radius).observe(owner, placeSearch -> {
            if (!placeSearch.getPredictions().isEmpty()) {
                final int[] c = {0};
                for (Prediction prediction : placeSearch.getPredictions()) {
                    int n = placeSearch.getPredictions().size();
                    googlePlaceService.getPlaceInfo(prediction.getPlaceId(), FIELD, MainActivity.KEY_API).enqueue(new Callback<Place>() {
                        @Override
                        public void onResponse(Call<Place> call, Response<Place> response) {
                            if (response.isSuccessful()) {
                                addPlace(response.body());
                                finalRestaurantsArrayList.add(finalRestaurant);
                                c[0]++;
                                if (c[0] == n) {
                                    restoFromSearch.setValue(finalRestaurantsArrayList);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Place> call, Throwable t) {
                            restoFromSearch.setValue(null);
                        }
                    });
                }
            }else{
                Log.i("DEBUGGG", "nulllll : ");
                restoFromSearch.setValue(null);
            }
        });
        return restoFromSearch;
    }

    private void addPlace(Place place){
        finalRestaurant = new FinalRestaurant();

        finalRestaurant.setPlaceId(place.getPlaceResult().getPlaceId());
        if (place.getPlaceResult().getPhotos() != null){
            finalRestaurant.setPhoto("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" +
                    place.getPlaceResult().getPhotos().get(0).getPhotoReference()+ "&key=" + MainActivity.KEY_API  );

        }else {
            finalRestaurant.setPhoto("https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcRwXJLkDvKaRcAS5hcY9ECLsEm7J95s1h7hOg&usqp=CAU");
        }

        finalRestaurant.setName(place.getPlaceResult().getName());
        finalRestaurant.setLatitude(place.getPlaceResult().getGeometry().getLocation().getLat());
        finalRestaurant.setLongitude(place.getPlaceResult().getGeometry().getLocation().getLng());

        if (place.getPlaceResult().getVicinity() != null){
            finalRestaurant.setAddress(place.getPlaceResult().getVicinity());
        }else {
            finalRestaurant.setAddress("address dont set");
        }

        if (place.getPlaceResult().getOpeningHours() != null){
            finalRestaurant.setOpeningHours(place.getPlaceResult().getOpeningHours().getWeekdayText());
        }

        if (place.getPlaceResult().getFormattedPhoneNumber() != null){
            finalRestaurant.setPhone(place.getPlaceResult().getFormattedPhoneNumber());
        }else {
            finalRestaurant.setPhone("Phone dont set");
        }

        if (place.getPlaceResult().getRating() != null){
            finalRestaurant.setRating(place.getPlaceResult().getRating());
        }else {
            finalRestaurant.setRating(0);
        }

        if (place.getPlaceResult().getWebsite() != null){
            finalRestaurant.setWebsite(place.getPlaceResult().getWebsite());
        }else {
            finalRestaurant.setWebsite("site dont set");
        }
        createRestaurant(finalRestaurant);
    }

}
