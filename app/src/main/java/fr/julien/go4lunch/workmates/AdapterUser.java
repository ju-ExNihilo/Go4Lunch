package fr.julien.go4lunch.workmates;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import com.bumptech.glide.request.RequestOptions;
import fr.julien.go4lunch.databinding.ItemUserBinding;
import fr.julien.go4lunch.models.User;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.UserViewHolder> {

    private final List<User> users;

    public AdapterUser(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterUser.UserViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        if (users.get(position) != null){
            User user = users.get(position);
            String urlPicUser;
            if (user.getUrlPicture() != null){
                urlPicUser = user.getUrlPicture();
            }else {
                urlPicUser = "https://cdn.pixabay.com/photo/2016/08/08/09/17/avatar-1577909_1280.png";
            }
            Glide.with(holder.binding.userPic.getContext())
                    .load(urlPicUser)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.binding.userPic);
            holder.binding.userName.setText(user.getUsername());
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder{
        ItemUserBinding binding;

        public UserViewHolder(ItemUserBinding itemUserBinding){
            super(itemUserBinding.getRoot());
            binding = itemUserBinding;
        }
    }
}
