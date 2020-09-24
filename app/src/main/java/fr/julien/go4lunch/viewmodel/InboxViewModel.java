package fr.julien.go4lunch.viewmodel;

import androidx.lifecycle.ViewModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import fr.julien.go4lunch.models.Inbox;
import fr.julien.go4lunch.repository.InboxRepository;

public class InboxViewModel extends ViewModel {

    InboxRepository inboxRepository;

    public InboxViewModel(InboxRepository inboxRepository) {
        this.inboxRepository = inboxRepository;
    }

    public FirestoreRecyclerOptions<Inbox> getPrivateChatRoomMessage(String from, String to){
        return inboxRepository.getPrivateChatRoomMessage(from, to);
    }

    /** GET **/
    public String getCurrentUserId(){return inboxRepository.getCurrentUserId();}

    public String getCurrentUserUrlPic(){return inboxRepository.getCurrentUserUrlPic();}

    /** INSERT **/
    public void  newMessage(Inbox newMessage){inboxRepository. newMessage(newMessage);}
}
