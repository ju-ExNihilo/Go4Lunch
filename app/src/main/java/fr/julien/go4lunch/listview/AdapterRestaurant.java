package fr.julien.go4lunch.listview;

import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import fr.julien.go4lunch.databinding.ItemRestaurantBinding;
import fr.julien.go4lunch.models.FinalRestaurant;
import fr.julien.go4lunch.utils.Utils;

import java.util.List;

public class AdapterRestaurant extends RecyclerView.Adapter<AdapterRestaurant.RestaurantViewHolder> {



    public interface OnRestaurantItemClicked{void onClickedRestaurant(FinalRestaurant restaurant);}


    private OnRestaurantItemClicked onRestaurantItemClicked;
    private List<FinalRestaurant> finalRestaurants;

    public AdapterRestaurant(OnRestaurantItemClicked onRestaurantItemClicked, List<FinalRestaurant> finalRestaurants) {
        this.onRestaurantItemClicked = onRestaurantItemClicked;
        this.finalRestaurants = finalRestaurants;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterRestaurant.RestaurantViewHolder(ItemRestaurantBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        FinalRestaurant finalRestaurant = finalRestaurants.get(position);
        float rating = (float)(finalRestaurant.getRating()/3.5)*2;
        float[] results = new float[1];
        Location.distanceBetween(finalRestaurant.getLatitude(), finalRestaurant.getLongitude(), 43.117142, 6.144056, results);
        int distance = (int)results[0];

        holder.binding.restaurantRating.setRating(rating);
        holder.binding.restaurantDistance.setText(String.valueOf(distance));
        if (finalRestaurant.getNbrCustomer() == 0){
            holder.binding.restaurantClientNumber.setVisibility(View.INVISIBLE);
            holder.binding.restaurantClientIcon.setVisibility(View.INVISIBLE);
            holder.binding.restaurantClientNumber.setVisibility(View.INVISIBLE);
        }else {
            holder.binding.restaurantClientNumber.setVisibility(View.VISIBLE);
            holder.binding.restaurantClientIcon.setVisibility(View.VISIBLE);
            holder.binding.restaurantClientNumber.setText("("+finalRestaurant.getNbrCustomer()+")");
        }

        Log.i("DEBUGGG", finalRestaurant.getName() + ": nbrCustomer = " + finalRestaurant.getNbrCustomer());

        holder.binding.restaurantName.setText(finalRestaurant.getName());
        Glide.with(holder.binding.restaurantPic.getContext())
                .load(finalRestaurant.getPhoto())
                .into(holder.binding.restaurantPic);
        if (finalRestaurant.getOpeningHours() != null){
            holder.binding.restaurantOpeningHours.setText(finalRestaurant.getOpeningHours().get(Utils.getIndexOfToday()));
        }else {
            holder.binding.restaurantOpeningHours.setText("Opening hours dont set");
        }

        holder.binding.restaurantAddress.setText(finalRestaurant.getAddress());
        holder.itemView.setOnClickListener(v -> onRestaurantItemClicked.onClickedRestaurant(finalRestaurant));
    }

    @Override
    public int getItemCount() {
        return finalRestaurants.size();
    }



    public static class RestaurantViewHolder extends RecyclerView.ViewHolder{

        ItemRestaurantBinding binding;

        public RestaurantViewHolder(ItemRestaurantBinding itemRestaurantBinding) {
            super(itemRestaurantBinding.getRoot());
            binding = itemRestaurantBinding;
        }
    }
}
