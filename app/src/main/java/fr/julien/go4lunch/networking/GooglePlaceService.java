package fr.julien.go4lunch.networking;

import fr.julien.go4lunch.models.autocomplete.PlaceSearch;
import fr.julien.go4lunch.models.nearbysearch.Restaurants;
import fr.julien.go4lunch.models.place.Place;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GooglePlaceService {

    //https://maps.googleapis.com/maps/api/place/autocomplete/json?input=Pizza
    // &types=establishment&location=43.120541,6.128639&radius=500&strictbounds
    // &key=AIzaSyCU1WJIZDjML_4NwlFJdVJQcjwEzls7iOo&sessiontoken=1234567890

    @GET("/maps/api/place/nearbysearch/json")
    Call<Restaurants> getRestaurants(@Query("location") String location, @Query("radius") int radius, @Query("type") String type, @Query("key") String key);

    @GET("/maps/api/place/details/json")
    Call<Place> getPlaceInfo(@Query("place_id") String placeId, @Query("fields") String fields, @Query("key") String key);

    @GET("/maps/api/place/autocomplete/json")
    Call<PlaceSearch> getPlaceAutoComplete(@Query("input") String input, @Query("types") String types, @Query("location") String location,
                                           @Query("radius") int radius, @Query("strictbounds") String strictbounds, @Query("key") String key, @Query("sessiontoken") String sessiontoken);


}