package fr.julien.go4lunch.viewmodel;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import fr.julien.go4lunch.models.FinalRestaurant;
import fr.julien.go4lunch.models.autocomplete.PlaceSearch;
import fr.julien.go4lunch.repository.RestaurantsDataRepository;
import java.util.List;

public class RestaurantsViewModel extends ViewModel {


    private final RestaurantsDataRepository restaurantsDataRepository;

    public RestaurantsViewModel(RestaurantsDataRepository restaurantsDataRepository) {
        this.restaurantsDataRepository = restaurantsDataRepository;
    }

    /** Retrofit **/

    public void getRestaurantsFromRetrofit(String location, int radius){
        restaurantsDataRepository.getRestaurantsFromRetrofit(location, radius);
    }

    public LiveData<List<FinalRestaurant>> getPlaceFromSearch(String input, String location, int radius, LifecycleOwner owner){
        return restaurantsDataRepository.getPlaceFromSearch(input, location, radius, owner);
    }

    public LiveData<FinalRestaurant> getPlaceDetailsInfoFromApi(String placeId){
        return restaurantsDataRepository.getPlaceDetailsInfoFromApi(placeId);
    }

    /** Firestore **/

    public LiveData<List<FinalRestaurant>> getMyRestaurants(String location, int radius, double longitude, double latitude, int distance, LifecycleOwner owner){
        return restaurantsDataRepository.getMyRestaurants(location, radius, longitude, latitude, distance, owner);
    }

    public LiveData<List<FinalRestaurant>> getRestaurants(){
        return restaurantsDataRepository.getRestaurants();
    }

    public LiveData<FinalRestaurant> getRestaurantById(String uId, String rId){return restaurantsDataRepository.getRestaurantById(rId);}

    public FirestoreRecyclerOptions<FinalRestaurant> getRestaurantForRecyclerView(){return restaurantsDataRepository.getRestaurantForRecyclerView();}

    public LiveData<List<FinalRestaurant>> updateRestaurants(String location, int radius, LifecycleOwner owner){
        return restaurantsDataRepository.updateRestaurants(location, radius, owner);}
}