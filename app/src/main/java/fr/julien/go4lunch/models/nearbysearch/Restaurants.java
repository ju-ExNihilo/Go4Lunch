package fr.julien.go4lunch.models.nearbysearch;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Restaurants {

    @SerializedName("results")
    @Expose
    private List<RestaurantsResult> restaurantsResults = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<RestaurantsResult> getRestaurantsResults() {
        return restaurantsResults;
    }

    public void setRestaurantsResults(List<RestaurantsResult> restaurantsResults) {
        this.restaurantsResults = restaurantsResults;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}