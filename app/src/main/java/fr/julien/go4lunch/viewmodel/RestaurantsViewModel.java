package fr.julien.go4lunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import fr.juju.googlemaplibrary.model.FinalPlace;
import fr.julien.go4lunch.repository.RestaurantsDataRepository;
import java.util.List;

public class RestaurantsViewModel extends ViewModel {


    private final RestaurantsDataRepository restaurantsDataRepository;

    public RestaurantsViewModel(RestaurantsDataRepository restaurantsDataRepository) {
        this.restaurantsDataRepository = restaurantsDataRepository;
    }

    /** GET **/
    public LiveData<List<FinalPlace>> getRestaurants(){
        return restaurantsDataRepository.getRestaurants();
    }

    public LiveData<FinalPlace> getRestaurantById(String rId){return restaurantsDataRepository.getRestaurantById(rId);}

    public LiveData<List<FinalPlace>> getPlaceFromSearch(String input, String location, int radius){
        return restaurantsDataRepository.getPlaceFromSearch(input, location, radius);
    }

    public LiveData<FinalPlace> getPlaceDetailsInfoFromApi(String placeId){
        return restaurantsDataRepository.getPlaceDetailsInfoFromApi(placeId);
    }

    public LiveData<List<FinalPlace>> getMyRestaurants(String location, int radius, double longitude, double latitude, int distance){
        return restaurantsDataRepository.getMyRestaurants(location, radius, longitude, latitude, distance);
    }

    /** UPDATE **/
    public LiveData<List<FinalPlace>> updateRestaurants(String location, int radius){
        return restaurantsDataRepository.updateRestaurants(location, radius);}
}