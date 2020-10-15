package fr.julien.go4lunch.repository;

import java.util.List;
import java.util.UUID;
import android.net.Uri;
import androidx.lifecycle.MutableLiveData;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import fr.julien.go4lunch.models.LikedRestaurant;
import fr.julien.go4lunch.models.User;

public class UserDataRepository {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    public static final String COLLECTION_USER = "users";

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

    /** ******** Insert Liked restaurant in firebase  ****** **/
    public Task<Void> insertLikedRestaurant(LikedRestaurant likedRestaurant) {
        String uId = getCurrentUserId();
        return getUserCollection().document(uId).collection("LikedRestaurants").document(likedRestaurant.getId()).set(likedRestaurant);
    }

    /** ***************************** **/
    /** ******** GET Method  ******** **/
    /** ***************************** **/

    /** ******** Get current user from firebaseAuth  ****** **/
    public FirebaseUser getCurrentUser(){
        return firebaseAuth.getCurrentUser();
    }

    public String getCurrentUserId(){
        return  getCurrentUser().getUid();
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

    /** ******** Get all user for test  ****** **/
    public MutableLiveData<List<User>> getCustomerForTest(String eatingPlaceId){
        MutableLiveData<List<User>> likedRestaurants = new MutableLiveData<>();
        getUserCollection().whereEqualTo("eatingPlaceId",eatingPlaceId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                likedRestaurants.setValue(task.getResult().toObjects(User.class));
            }else {
                likedRestaurants.setValue(null);
            }
        });
        return likedRestaurants;
    }

    /** ******** Get all user for test  ****** **/
    public MutableLiveData<List<User>> getAllUsersForTest(){

        MutableLiveData<List<User>> likedRestaurants = new MutableLiveData<>();
        getUserCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                likedRestaurants.setValue(task.getResult().toObjects(User.class));
            }else {
                likedRestaurants.setValue(null);
            }
        });
        return likedRestaurants;
    }

    /** ******** Get all user for test  ****** **/
    public MutableLiveData<List<User>> getSearchUsersForTest(String searchQuery){

        MutableLiveData<List<User>> likedRestaurants = new MutableLiveData<>();
        getUserCollection().whereGreaterThanOrEqualTo("username", searchQuery).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                likedRestaurants.setValue(task.getResult().toObjects(User.class));
            }else {
                likedRestaurants.setValue(null);
            }
        });
        return likedRestaurants;
    }

    /** ******** Get current user from firestore  ****** **/
    public MutableLiveData<User> getUserFromFirestore(){
        String uId = getCurrentUserId();
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

    /** ******** Get Liked restaurant from firestore  ****** **/
    public MutableLiveData<List<LikedRestaurant>> getLikedRestaurants(){
        String uId = getCurrentUserId();
        MutableLiveData<List<LikedRestaurant>> likedRestaurants = new MutableLiveData<>();
        getUserCollection().document(uId).collection("LikedRestaurants").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                likedRestaurants.setValue(task.getResult().toObjects(LikedRestaurant.class));
            }else {
                likedRestaurants.setValue(null);
            }
        });
        return likedRestaurants;
    }

    /** **** Get restaurant by id from firestore  **** **/
    public MutableLiveData<LikedRestaurant> getLikedRestaurantById(String rId){
        String uId = getCurrentUserId();
        MutableLiveData<LikedRestaurant> data = new MutableLiveData<>();
        getUserCollection().document(uId).collection("LikedRestaurants").document(rId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                data.setValue(documentSnapshot.toObject(LikedRestaurant.class));
            }else {
                data.setValue(null);
            }
        }).addOnFailureListener(e -> data.setValue(null));
        return data;
    }

    /** ********************************* **/
    /** ******** UPDATE Method  ******** **/
    /** ******************************* **/

    /** ******** Update LatLn  ****** **/
    private Task<Void> updateLongitude(String uId, double longitude) {
        return getUserCollection().document(uId).update("longitude", longitude);
    }
    private Task<Void> updateLatitude(String uId, double latitude) {
        return getUserCollection().document(uId).update("latitude", latitude);
    }

    public void updateLatLng(double longitude, double latitude ){
        String uId = getCurrentUserId();
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
    public Task<Void> updateRadius(int radius) {
        String uId = getCurrentUserId();
        return getUserCollection().document(uId).update("radius", radius);
    }

    /** **** Update user picture  **** **/
    private Task<Void> updateUserPicture(String urlPicture) {
        String uId = getCurrentUserId();
        return getUserCollection().document(uId).update("urlPicture", urlPicture);
    }

    /** **** Upload user picture  **** **/
    public void uploadPhotoInFirebaseAndUpdateUserPicture(Uri uri) {
        String uuid = UUID.randomUUID().toString();
        StorageReference mImageRef = FirebaseStorage.getInstance().getReference(uuid);
        UploadTask uploadTask = mImageRef.putFile(uri);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return mImageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                updateUserPicture(downloadUri.toString());
            }
        });
    }

    /** ********************************* **/
    /** ******** DELETE Method  ******** **/
    /** ******************************* **/

    /** **** Delete restaurant in firestore  **** **/
    public Task<Void> deleteLikedRestaurant(String restaurantId){
        String uId = getCurrentUserId();
        return getUserCollection().document(uId).collection("LikedRestaurants").document(restaurantId).delete();
    }

    /** **** Delete user after test  **** **/
    public Task<Void> deleteUser(String userId){
        return getUserCollection().document(userId).delete();
    }
}

