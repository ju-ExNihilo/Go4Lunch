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
    private CollectionReference quizRef = firebaseFirestore.collection("users");
    private OnFirestoreTaskComplete onFirestoreTaskComplete;

    public UserDataRepository(){}

    public UserDataRepository(OnFirestoreTaskComplete onFirestoreTaskComplete) {
        this.onFirestoreTaskComplete = onFirestoreTaskComplete;
    }

    // --- CREATE ---

    public Task<Void> createUser(User userToCreate) {
        return FirebaseFirestore.getInstance().collection("users").document(userToCreate.getUid()).set(userToCreate);
    }

    // --- GET ---

    public void getUser(String uid){
        quizRef.document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                onFirestoreTaskComplete.userData(documentSnapshot.toObject(User.class));
            }
        });
    }

    public FirebaseUser getCurentUser(){
        return firebaseAuth.getCurrentUser();
    }

    public void getAllUsers(){
        quizRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                onFirestoreTaskComplete.usersListData(task.getResult().toObjects(User.class));
            }else {
                onFirestoreTaskComplete.onError(task.getException());
            }
        });
    }

    public interface OnFirestoreTaskComplete{
        void usersListData(List<User> usersListModels);
        void userData(User userModel);
        void onError(Exception e);
    }

}
