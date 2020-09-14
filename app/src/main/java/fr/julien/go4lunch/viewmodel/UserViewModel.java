package fr.julien.go4lunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import fr.julien.go4lunch.models.User;
import fr.julien.go4lunch.repository.UserDataRepository;

public class UserViewModel extends ViewModel {

    private final UserDataRepository userDataRepository ;
    /** DATA **/
    private MutableLiveData<List<User>> users;

    public UserViewModel(UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }

    public void init(){

    }

    /** GET **/
    public FirestoreRecyclerOptions getAllUser() {
        return userDataRepository.getAllUsers();
    }

    public FirestoreRecyclerOptions getSearchUser(String query) {
        return userDataRepository.getSearchUsers(query);
    }

    public FirestoreRecyclerOptions getCustomer(String eatingPlaceId) {
        return userDataRepository.getCustomer(eatingPlaceId);
    }

    public LiveData<User> getCurrentUserData(){
        return userDataRepository.getUserFromFirestore();
    }

    /** INSERT **/
    public void createUser(User user) { userDataRepository.createUser(user);}

    /** UPDATE **/
    public void updateUserLatLn(double longitude, double latitude) { userDataRepository.updateLatLng(longitude,latitude);}

    public void updateEatingPlace(String uid, String eatingPlaceName, String eatingPlaceId ) { userDataRepository.updateEatingPlace(uid, eatingPlaceName, eatingPlaceId);}

    public void updateName(String uid, String username){userDataRepository.updateName(uid, username);}

    public void updateRadius(String uid, int radius){userDataRepository.updateRadius(uid, radius);}

    public void updateNbrCustomer(String placeId, int nbrCustomer){userDataRepository.updateNbrCustomer(placeId, nbrCustomer);}

}

