package fr.julien.go4lunch.repository;

import androidx.lifecycle.MutableLiveData;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import fr.julien.go4lunch.models.Inbox;
import java.util.Arrays;
import java.util.List;

public class InboxRepository {

    private static final String COLLECTION_NAME = "inbox";
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private CollectionReference getInboxCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    /** ******** Get message from FirestoreRecyclerOptions  ******/
    public FirestoreRecyclerOptions<Inbox> getPrivateChatRoomMessage(String from, String to){
         Query query = getInboxCollection().whereIn("between", Arrays.asList(Arrays.asList(from, to), Arrays.asList(to, from))).orderBy("date", Query.Direction.ASCENDING);
        return new FirestoreRecyclerOptions.Builder<Inbox>()
                .setQuery(query, Inbox.class)
                .build();
    }

    /** ******** Get current user from firebaseAuth  ******/
    public String getCurrentUserId(){
        return firebaseAuth.getCurrentUser().getUid();
    }

    public String getCurrentUserUrlPic(){
        return firebaseAuth.getCurrentUser().getPhotoUrl().toString();
    }

    /** ******** Insert message in firestore  ****** **/
    public Task<Void> newMessage(Inbox newMessage) {
        return getInboxCollection().document().set(newMessage);
    }


    /** ******** Get all message for test  ****** **/
    public MutableLiveData<List<Inbox>> getMessagesForTest(String from, String to){
        MutableLiveData<List<Inbox>> likedRestaurants = new MutableLiveData<>();
        getInboxCollection().whereIn("between", Arrays.asList(Arrays.asList(from, to), Arrays.asList(to, from))).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                likedRestaurants.setValue(task.getResult().toObjects(Inbox.class));
            }else {
                likedRestaurants.setValue(null);
            }
        });
        return likedRestaurants;
    }
}