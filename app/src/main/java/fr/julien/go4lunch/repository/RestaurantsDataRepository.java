package fr.julien.go4lunch.repository;

import android.util.Log;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import fr.juju.googlemaplibrary.model.FinalPlace;
import fr.juju.googlemaplibrary.repository.GooglePlaceRepository;
import java.util.List;


public class RestaurantsDataRepository {

    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<FinalPlace>> restaurants = new MutableLiveData<>();
    private final GooglePlaceRepository googlePlaceRepository;
    private final String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final String COLLECTION_USER = "users";
    private final String COLLECTION_RESTAURANT = "MyResto";
    private final String NEARBYSEARCH_TYPE = "restaurant";
    private final String AUTOCOMPLETE_TYPE = "establishment";
    private final String EATING_PLACE_ID = "eatingPlaceId";
    private final String NBR_CUSTOMERS = "nbrCustomer";
    private final String defaultPicUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcRwXJLkDvKaRcAS5hcY9ECLsEm7J95s1h7hOg&usqp=CAU";
    private final LifecycleOwner owner;


    public RestaurantsDataRepository(GooglePlaceRepository googlePlaceRepository, LifecycleOwner owner) {
        this.googlePlaceRepository = googlePlaceRepository;
        this.owner = owner;
    }

    private CollectionReference getMyRestaurantsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_USER).document(uId).collection(COLLECTION_RESTAURANT);
    }

    /** ****
     * returns a list of restaurants,
     *  if first login get the list from google "nearbySearch",
     *  if your new location is at a distance greater than your radius offers you to update your data,
     *  else recuperate the data in firestore
     *  **** **/
    public MutableLiveData<List<FinalPlace>> getMyRestaurants(String location, int radius, double longitude, double latitude, int distance){

        if (latitude == 0 && longitude == 0){
            return getFinalPlace(location, radius);
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

    /** **** Get List restaurant from firestore  **** **/
    public MutableLiveData<List<FinalPlace>> getRestaurants(){
        MutableLiveData<List<FinalPlace>> data = new MutableLiveData<>();
        getMyRestaurantsCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                final int[] c = {0};
                List<FinalPlace> finalRestaurantsGetPlaces = task.getResult().toObjects(FinalPlace.class);
                for (FinalPlace finalPlaceGetRestaurants : finalRestaurantsGetPlaces){
                    int n = finalRestaurantsGetPlaces.size();
                    firebaseFirestore.collection(COLLECTION_USER)
                            .whereEqualTo(EATING_PLACE_ID, finalPlaceGetRestaurants.getPlaceId()).get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                updateNbrCustomer(finalPlaceGetRestaurants.getPlaceId(), queryDocumentSnapshots.size());
                                finalPlaceGetRestaurants.setNbrCustomer(queryDocumentSnapshots.size());
                                c[0]++;
                                if (c[0] ==n){
                                    data.setValue(finalRestaurantsGetPlaces);
                                }
                            });
                }
            }else {
                data.setValue(null);}
        });
        return data;
    }

    /** **** Get List restaurant from google "nearbySearch" (own library)  **** **/
    public MutableLiveData<List<FinalPlace>> getFinalPlace(String location, int radius){
        MutableLiveData<List<FinalPlace>> data = new MutableLiveData<>();
        final int[] c = {0};
        googlePlaceRepository.getPlace(location, radius, NEARBYSEARCH_TYPE, defaultPicUrl).observe(owner, finalPlaces -> {
            if (finalPlaces != null){
                int n = finalPlaces.size();
                insertFinalPlace(data, finalPlaces, n, c);
            }else {
                data.setValue(null);
            }

        });

        return data;
    }


    /** **** Get restaurant by id from firestore  **** **/
    public MutableLiveData<FinalPlace> getRestaurantById(String rId){
        MutableLiveData<FinalPlace> data = new MutableLiveData<>();
        getMyRestaurantsCollection().document(rId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                data.setValue(documentSnapshot.toObject(FinalPlace.class));
            }else {
                data.setValue(null);
            }
        }).addOnFailureListener(e -> data.setValue(null));
        return data;
    }

    /** **** Get List restaurant from google "autoComplete" (own library) **** **/
    public MutableLiveData<List<FinalPlace>> getPlaceFromSearch(String input, String location, int radius){
        MutableLiveData<List<FinalPlace>> data = new MutableLiveData<>();
        googlePlaceRepository.getPlaceFromAutoComplete(input, location, radius, AUTOCOMPLETE_TYPE, defaultPicUrl).observe(owner, finalPlaces -> {
            if (finalPlaces != null){
                for (FinalPlace finalPlace : finalPlaces){
                    createRestaurant(finalPlace);
                    Log.i("DEBUGGG", finalPlace.getName());
                }
                data.setValue(finalPlaces);

            }else {
                data.setValue(null);
            }

        });
        return data;
    }

    /** **** Get restaurant from google "place" (own library) **** **/
    public MutableLiveData<FinalPlace> getPlaceDetailsInfoFromApi(String placeId){
        MutableLiveData<FinalPlace> data = new MutableLiveData<>();
        googlePlaceRepository.getPlaceDetailsInfoFromId(placeId, defaultPicUrl).observe(owner, finalPlace -> {
            if (finalPlace != null){
                createRestaurant(finalPlace);
                data.setValue(finalPlace);
            }else {
                data.setValue(null);
            }

        });
        return data;
    }

    /** ***************************** **/
    /** ******* Create Method  ****** **/
    /** ***************************** **/

    /** **** Insert restaurant in firestore  **** **/
    private Task<Void> createRestaurant(FinalPlace finalPlace) {
        return getMyRestaurantsCollection().document(finalPlace.getPlaceId())
                .set(finalPlace);
    }

    /** ********************************* **/
    /** ******** Delete Method  ******** **/
    /** ******************************* **/

    /** **** Delete restaurant in firestore  **** **/
    private Task<Void> deleteRestaurant(String restaurantId){
        return getMyRestaurantsCollection().document(restaurantId).delete();
    }

    /** ******************************* **/
    /** ******** Update Method  ****** **/
    /** ***************************** **/

    /** **** Update restaurant in firestore  **** **/
    private Task<Void> updateNbrCustomer(String restaurantId, int nbrCustomer){
        return getMyRestaurantsCollection().document(restaurantId).update(NBR_CUSTOMERS, nbrCustomer);
    }

    /** **** Reload restaurants data  **** **/
    public MutableLiveData<List<FinalPlace>> updateRestaurants(String location, int radius){
        MutableLiveData<List<FinalPlace>> data = new MutableLiveData<>();
        getMyRestaurantsCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                int nbrRestaurants = task.getResult().toObjects(FinalPlace.class).size();
                int counter = 0;
                for (FinalPlace finalRestaurant : task.getResult().toObjects(FinalPlace.class)) {
                    deleteRestaurant(finalRestaurant.getPlaceId());
                    counter++;
                    if (nbrRestaurants == counter) {

                        googlePlaceRepository.getPlace(location,radius, NEARBYSEARCH_TYPE, defaultPicUrl).observe(owner, finalPlaces -> {
                            if (finalPlaces != null){
                                int n = finalPlaces.size();
                                final int[] c = {0};
                                insertFinalPlace(data, finalPlaces, n, c);
                            }else {
                                data.setValue(null);
                            }

                        });

                    }
                }
            }
        });
        return data;
    }

    /** ******************************* **/
    /** ******** Utils Method  ******* **/
    /** ***************************** **/

    private void insertFinalPlace(MutableLiveData<List<FinalPlace>> listMutableLiveData, List<FinalPlace> finalPlaces, int n, int[] c) {
        for (FinalPlace finalPlace : finalPlaces) {
            firebaseFirestore.collection(COLLECTION_USER)
                    .whereEqualTo(EATING_PLACE_ID, finalPlace.getPlaceId()).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        finalPlace.setNbrCustomer(queryDocumentSnapshots.size());
                        createRestaurant(finalPlace);
                        c[0]++;
                        if (c[0] == n){
                            listMutableLiveData.setValue(finalPlaces);
                        }
                    });
        }
    }
}
