package fr.julien.go4lunch.listview;

import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import fr.juju.googlemaplibrary.model.FinalPlace;
import fr.julien.go4lunch.databinding.ItemRestaurantBinding;
import fr.julien.go4lunch.utils.Utils;

import java.util.List;

public class AdapterRestaurant extends RecyclerView.Adapter<AdapterRestaurant.RestaurantViewHolder> {



    public interface OnRestaurantItemClicked{void onClickedRestaurant(FinalPlace restaurant);}


    private OnRestaurantItemClicked onRestaurantItemClicked;
    private List<FinalPlace> finalPlaces;

    public AdapterRestaurant(OnRestaurantItemClicked onRestaurantItemClicked, List<FinalPlace> finalPlaces) {
        this.onRestaurantItemClicked = onRestaurantItemClicked;
        this.finalPlaces = finalPlaces;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterRestaurant.RestaurantViewHolder(ItemRestaurantBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        FinalPlace finalPlace = finalPlaces.get(position);
        float rating = (float)(finalPlace.getRating()/3.5)*2;
        float[] results = new float[1];
        Location.distanceBetween(finalPlace.getLatitude(), finalPlace.getLongitude(), 43.117142, 6.144056, results);
        int distance = (int)results[0];

        holder.binding.restaurantRating.setRating(rating);
        holder.binding.restaurantDistance.setText(String.valueOf(distance));
        if (finalPlace.getNbrCustomer() == 0){
            holder.binding.restaurantClientNumber.setVisibility(View.INVISIBLE);
            holder.binding.restaurantClientIcon.setVisibility(View.INVISIBLE);
            holder.binding.restaurantClientNumber.setVisibility(View.INVISIBLE);
        }else {
            holder.binding.restaurantClientNumber.setVisibility(View.VISIBLE);
            holder.binding.restaurantClientIcon.setVisibility(View.VISIBLE);
            holder.binding.restaurantClientNumber.setText("("+ finalPlace.getNbrCustomer()+")");
        }

        Log.i("DEBUGGG", finalPlace.getName() + ": nbrCustomer = " + finalPlace.getNbrCustomer());

        holder.binding.restaurantName.setText(finalPlace.getName());
        Glide.with(holder.binding.restaurantPic.getContext())
                .load(finalPlace.getPhoto())
                .into(holder.binding.restaurantPic);
        if (finalPlace.getOpeningHours() != null){
            holder.binding.restaurantOpeningHours.setText(finalPlace.getOpeningHours().get(Utils.getIndexOfToday()));
        }else {
            holder.binding.restaurantOpeningHours.setText("Opening hours dont set");
        }

        holder.binding.restaurantAddress.setText(finalPlace.getAddress());
        holder.itemView.setOnClickListener(v -> onRestaurantItemClicked.onClickedRestaurant(finalPlace));
    }

    @Override
    public int getItemCount() {
        return finalPlaces.size();
    }



    public static class RestaurantViewHolder extends RecyclerView.ViewHolder{

        ItemRestaurantBinding binding;

        public RestaurantViewHolder(ItemRestaurantBinding itemRestaurantBinding) {
            super(itemRestaurantBinding.getRoot());
            binding = itemRestaurantBinding;
        }
    }
}
