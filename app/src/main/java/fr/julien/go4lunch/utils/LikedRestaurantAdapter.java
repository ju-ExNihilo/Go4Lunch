package fr.julien.go4lunch.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import fr.juju.googlemaplibrary.model.FinalPlace;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.models.LikedRestaurant;

import java.util.List;

public class LikedRestaurantAdapter extends ArrayAdapter<LikedRestaurant> {

    private final Context context;
    private final List<LikedRestaurant> finalPlaces;

    public LikedRestaurantAdapter(@NonNull Context context, int resource, @NonNull List<LikedRestaurant> objects) {
        super(context, resource, objects);
        this.finalPlaces = objects;
        this.context = context;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_assignment_dialog_list_layout, parent, false);
        ImageView profilePic = rowView.findViewById(R.id.iv_user_profile_image);
        TextView userName = rowView.findViewById(R.id.tv_user_name);
        LikedRestaurant finalPlace = finalPlaces.get(position);

        userName.setText(finalPlace.getName());

        Glide.with(profilePic.getContext())
                .load(finalPlace.getPhoto())
                .apply(RequestOptions.circleCropTransform())
                .into(profilePic);


        return rowView;
    }
}
