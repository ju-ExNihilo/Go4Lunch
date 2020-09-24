package fr.julien.go4lunch.workmates;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.ItemUserBinding;
import fr.julien.go4lunch.models.User;

public class AdapterUser extends FirestoreRecyclerAdapter<User, AdapterUser.UserViewHolder> {


    public interface OnViewClicked {
        void onWorkmateItemClicked(String restaurantId);
        void onChatButtonClicked(String userId, String userName);
        void onDataChanged();
    }

    private final boolean isFromDetails;
    private final OnViewClicked onViewClicked;

    public AdapterUser(@NonNull FirestoreRecyclerOptions<User> options, boolean isFromDetails, OnViewClicked onViewClicked) {
        super(options);
        this.isFromDetails = isFromDetails;
        this.onViewClicked = onViewClicked;
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {

            Glide.with(holder.binding.userPic.getContext())
                    .load(model.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.binding.userPic);
            holder.binding.userName.setText(model.getUsername());

            holder.binding.messageButton.setOnClickListener(v -> this.onViewClicked.onChatButtonClicked(model.getUid(), model.getUsername()));

            if(isFromDetails){
                holder.binding.eatingPlace.setText(R.string.is_joining);
            }else {
                String eatingPlace = holder.itemView.getContext().getString(R.string.eating_place, model.getEatingPlace());
                if (!model.getEatingPlaceId().equals(holder.itemView.getContext().getString(R.string.none))){
                    holder.binding.eatingPlace.setText(eatingPlace);
                    holder.itemView.setOnClickListener(v -> this.onViewClicked.onWorkmateItemClicked(model.getEatingPlaceId()));
                }
            }

    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        onViewClicked.onDataChanged();
    }

     static class UserViewHolder extends RecyclerView.ViewHolder{
        ItemUserBinding binding;

        public UserViewHolder(ItemUserBinding itemUserBinding){
            super(itemUserBinding.getRoot());
            binding = itemUserBinding;
        }
    }
}
