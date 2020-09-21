package fr.julien.go4lunch.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.models.User;
import fr.julien.go4lunch.repository.UserDataRepository;
import fr.julien.go4lunch.viewmodel.RestaurantsViewModel;
import fr.julien.go4lunch.viewmodel.UserViewModel;


public class ClearEatingPlaceWorker extends Worker {

    public static final String KEY_CLEAR_WORKER = "KEY_CLEAR_WORKER";

    public ClearEatingPlaceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    @NonNull
    @Override
    public Result doWork() {
        clearEatingPlace();
        return Result.success();
    }


    private void clearEatingPlace(){
        getUserCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                for (User user : task.getResult().toObjects(User.class)){
                    updateEatingPlace(user.getUid(), "none", "none");
                }
            }
        });
    }

    private CollectionReference getUserCollection(){
        return FirebaseFirestore.getInstance().collection(UserDataRepository.COLLECTION_USER);
    }

    /** **** Update EatingPlace  **** **/
    private Task<Void> updateEatingPlaceName(String uid, String eatingPlaceName) {
        return getUserCollection().document(uid).update("eatingPlace", eatingPlaceName);
    }
    private Task<Void> updateEatingPlaceId(String uid, String eatingPlaceId) {
        return getUserCollection().document(uid).update("eatingPlaceId", eatingPlaceId);
    }

    public void updateEatingPlace(String uId, String eatingPlaceName, String eatingPlaceId ){
        this.updateEatingPlaceName(uId, eatingPlaceName);
        this.updateEatingPlaceId(uId, eatingPlaceId);
    }
    /** ***************************** **/


}
