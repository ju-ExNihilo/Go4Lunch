package fr.julien.go4lunch.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import fr.julien.go4lunch.models.LikedRestaurant;
import fr.julien.go4lunch.models.User;
import fr.julien.go4lunch.repository.UserDataRepository;

public class UserViewModel extends ViewModel {

    private final UserDataRepository userDataRepository ;

    public UserViewModel(UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }


    /** GET **/
    public FirestoreRecyclerOptions getAllUser() {
        return userDataRepository.getAllUsers();
    }

    public FirestoreRecyclerOptions getSearchUser(String query) {
        return userDataRepository.getSearchUsers(query);
    }

    public FirestoreRecyclerOptions getCustomer(String eatingPlaceId) {return userDataRepository.getCustomer(eatingPlaceId); }

    public FirebaseUser getCurrentUser(){ return userDataRepository.getCurrentUser();}

    public LiveData<User> getCurrentUserData(){
        return userDataRepository.getUserFromFirestore();
    }

    public LiveData<List<LikedRestaurant>> getLikedRestaurants(){return userDataRepository.getLikedRestaurants();}

    public LiveData<LikedRestaurant> getLikedRestaurantById(String rId){return userDataRepository.getLikedRestaurantById(rId);}

    /** INSERT **/
    public void createUser(User user) { userDataRepository.createUser(user);}

    public void insertLikedRestaurant(LikedRestaurant likedRestaurant){userDataRepository.insertLikedRestaurant(likedRestaurant);}

    /** UPDATE **/
    public void updateUserLatLn(double longitude, double latitude) { userDataRepository.updateLatLng(longitude,latitude);}

    public void updateEatingPlace(String uid, String eatingPlaceName, String eatingPlaceId ) { userDataRepository.updateEatingPlace(uid, eatingPlaceName, eatingPlaceId);}

    public void updateName(String uid, String username){userDataRepository.updateName(uid, username);}

    public void updateRadius(int radius){userDataRepository.updateRadius(radius);}

    public void updateUserPicture(Uri uri){userDataRepository.uploadPhotoInFirebaseAndUpdateUserPicture(uri);}

    /** DELETE **/
    public void deleteLikedRestaurant(String restaurantId){userDataRepository.deleteLikedRestaurant(restaurantId);}

}

