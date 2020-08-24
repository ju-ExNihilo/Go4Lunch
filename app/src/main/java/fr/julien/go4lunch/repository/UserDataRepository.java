package fr.julien.go4lunch.repository;

import java.util.List;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import fr.julien.go4lunch.models.User;

public class UserDataRepository {

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private Query quizRef = firebaseFirestore.collection("users");
    private OnRequestTaskComplete onRequestTaskComplete;

    public UserDataRepository(){}

    public UserDataRepository(OnRequestTaskComplete onRequestTaskComplete) {
        this.onRequestTaskComplete = onRequestTaskComplete;
    }

    // --- CREATE ---

    public Task<Void> createUser(User userToCreate) {
        return FirebaseFirestore.getInstance().collection("users").document(userToCreate.getUid()).set(userToCreate);
    }

    // --- GET ---

    public void getSearchUsers(String query){
        quizRef.whereGreaterThanOrEqualTo("username",query)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        onRequestTaskComplete.usersSearchData(task.getResult().toObjects(User.class));
                    }else {
                        onRequestTaskComplete.onError(task.getException());
                    }
                });
    }

    public FirebaseUser getCurentUser(){
        return firebaseAuth.getCurrentUser();
    }

    public void getAllUsers(){
        quizRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                onRequestTaskComplete.usersListData(task.getResult().toObjects(User.class));
            }else {
                onRequestTaskComplete.onError(task.getException());
            }
        });
    }

    public interface OnRequestTaskComplete {
        void usersListData(List<User> usersListModels);
        void usersSearchData(List<User> userModel);
        void onError(Exception e);
    }

}
