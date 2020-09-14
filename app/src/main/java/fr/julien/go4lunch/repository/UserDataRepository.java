package fr.julien.go4lunch.repository;

import java.util.Arrays;
import java.util.List;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import fr.julien.go4lunch.models.Inbox;
import fr.julien.go4lunch.models.User;

public class UserDataRepository {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static final String COLLECTION_USER = "users";

    public UserDataRepository(){}

    private CollectionReference getUserCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_USER);
    }


    /** ***************************** **/
    /** ******* Create Method  ****** **/
    /** ***************************** **/

    /** ******** Insert user in firebase  ****** **/
    public Task<Void> createUser(User userToCreate) {
       return getUserCollection().document(userToCreate.getUid()).set(userToCreate);
    }

    /** ***************************** **/
    /** ******** GET Method  ******** **/
    /** ***************************** **/

    /** ******** Get current user from firebaseAuth  ****** **/
    public FirebaseUser getCurrentUser(){
        return firebaseAuth.getCurrentUser();
    }

    /** ******** Get users after search  ****** **/
    public FirestoreRecyclerOptions<User> getSearchUsers(String searchQuery){
        Query query = getUserCollection().whereGreaterThanOrEqualTo("username", searchQuery);
        return new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();
    }

    /** ******** Get customer of a restaurant  ****** **/
    public FirestoreRecyclerOptions<User> getCustomer(String eatingPlaceId){
        Query query = getUserCollection().whereEqualTo("eatingPlaceId",eatingPlaceId);
        return new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();
    }

    /** ******** Get all users  ****** **/
     public FirestoreRecyclerOptions<User> getAllUsers(){
        Query query = getUserCollection();
        return new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();
    }

    /** ******** Get current user from firestore  ****** **/
    public MutableLiveData<User> getUserFromFirestore(){
        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        MutableLiveData<User> user = new MutableLiveData<>();
        getUserCollection().document(uId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                user.setValue(documentSnapshot.toObject(User.class));
            }else {
                user.setValue(null);
            }
        });
        return user;
    }

    /** ********************************* **/
    /** ******** UPDATE Method  ******** **/
    /** ******************************* **/

    /** ******** Update number of customer  ****** **/
    public void updateNbrCustomer(String placeId, int nbrCustomer){
        getUserCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                for (User user :task.getResult().toObjects(User.class)){
                    updateNbrCustomers(user.getUid(), placeId, nbrCustomer);
                }
            }
        });
    }

    private Task<Void> updateNbrCustomers(String uId, String placeId, int nbrCustomer) {
        return getUserCollection().document(uId).collection("MyResto").document(placeId).update("nbrCustomer", nbrCustomer);
    }
    /** ***************************** **/

    /** ******** Update LatLn  ****** **/
    private Task<Void> updateLongitude(String uId, double longitude) {
        return getUserCollection().document(uId).update("longitude", longitude);
    }
    private Task<Void> updateLatitude(String uId, double latitude) {
        return getUserCollection().document(uId).update("latitude", latitude);
    }

    public void updateLatLng(double longitude, double latitude ){
        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.updateLongitude(uId, longitude);
        this.updateLatitude(uId, latitude);
    }
    /** ***************************** **/

    /** **** Update EatingPlace  **** **/
    private Task<Void> updateEatingPlaceName(String uid, String eatingPlaceName) {
        return getUserCollection().document(uid).update("eatingPlace", eatingPlaceName);
    }
    private Task<Void> updateEatingPlaceId(String uid, String eatingPlaceId) {
        return getUserCollection().document(uid).update("eatingPlaceId", eatingPlaceId);
    }

    public void updateEatingPlace(String uid, String eatingPlaceName, String eatingPlaceId ){
        this.updateEatingPlaceName(uid, eatingPlaceName);
        this.updateEatingPlaceId(uid, eatingPlaceId);
    }
    /** ***************************** **/

    /** **** Update user name  **** **/
    public Task<Void> updateName(String uid, String username) {
        return getUserCollection().document(uid).update("username", username);
    }

    /** **** Update user radius  **** **/
    public Task<Void> updateRadius(String uid, int radius) {
        return getUserCollection().document(uid).update("radius", radius);
    }

}

