package fr.julien.go4lunch.models.nearbysearch;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RestaurantsResult {

    @SerializedName("place_id")
    @Expose
    private String placeId;

    public String getPlaceId() {
        return placeId;
    }

}
