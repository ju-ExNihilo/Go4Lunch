package fr.julien.go4lunch.repository;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.Query;
import fr.julien.go4lunch.models.Inbox;

import java.util.Arrays;

public class InboxRepository {

    private static final String COLLECTION_NAME = "inbox";
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private CollectionReference getInboxCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

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
        String urlPic;
        if (firebaseAuth.getCurrentUser().getPhotoUrl() != null){
            urlPic = firebaseAuth.getCurrentUser().getPhotoUrl().toString();
        }else {
            urlPic = "https://cdn.pixabay.com/photo/2016/08/08/09/17/avatar-1577909_1280.png";
        }
        return urlPic;
    }

    /** ******** Insert user in firebase  ****** **/
    public Task<Void> newMessage(Inbox newMessage) {
        return getInboxCollection().document().set(newMessage);
    }
}
