package fr.julien.go4lunch.chatroom;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.databinding.MessageItemBinding;
import fr.julien.go4lunch.models.Inbox;
import java.text.DateFormat;

public class InboxAdapter extends FirestoreRecyclerAdapter<Inbox, InboxAdapter.InboxHolder> {

    public interface OnDataChange {void onDataChanged();}

    private LinearLayout profileContainer;
    private RelativeLayout rootView;
    private RelativeLayout messageContainer;
    private final String currentUserId;
    private final OnDataChange onDataChange;

    public InboxAdapter(@NonNull FirestoreRecyclerOptions<Inbox> options, String currentUserId, OnDataChange onDataChange ) {
        super(options);
        this.currentUserId = currentUserId;
        this.onDataChange = onDataChange;
    }

    @Override
    protected void onBindViewHolder(@NonNull InboxHolder holder, int position, @NonNull Inbox model) {
        messageContainer = holder.binding.activityMentorChatItemMessageContainer;
        rootView = holder.binding.activityMentorChatItemRootView;
        profileContainer = holder.binding.activityMentorChatItemProfileContainer;
        holder.binding.activityMentorChatItemMessageContainerTextMessageContainerTextView.setText(model.getMessage());
        holder.binding.activityMentorChatItemMessageContainerTextViewDate.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(model.getDate()));
        updateDesignDependingUser(model.getFrom().equals(currentUserId));
        Glide.with(holder.binding.activityMentorChatItemProfileContainerProfileImage.getContext())
                .load( model.getUrlPicFrom())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.binding.activityMentorChatItemProfileContainerProfileImage);
    }

    private void updateDesignDependingUser(Boolean isSender){

        // PROFILE CONTAINER
        RelativeLayout.LayoutParams paramsLayoutHeader = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsLayoutHeader.addRule(isSender ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT);
        this.profileContainer.setLayoutParams(paramsLayoutHeader);

        // MESSAGE CONTAINER
        RelativeLayout.LayoutParams paramsLayoutContent = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsLayoutContent.addRule(isSender ? RelativeLayout.LEFT_OF : RelativeLayout.RIGHT_OF, R.id.activity_mentor_chat_item_profile_container);
        this.messageContainer.setLayoutParams(paramsLayoutContent);

        this.rootView.requestLayout();
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        onDataChange.onDataChanged();
    }

    @NonNull
    @Override
    public InboxHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InboxHolder(MessageItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    public static class InboxHolder extends RecyclerView.ViewHolder {

        MessageItemBinding binding;

        public InboxHolder(MessageItemBinding messageItemBinding) {
            super(messageItemBinding.getRoot());
            binding = messageItemBinding;
        }
    }
}
