package fr.julien.go4lunch.injection;

import android.content.Context;
import androidx.lifecycle.LifecycleOwner;
import fr.juju.googlemaplibrary.repository.GooglePlaceRepository;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.repository.InboxRepository;
import fr.julien.go4lunch.repository.RestaurantsDataRepository;
import fr.julien.go4lunch.repository.UserDataRepository;


public class Injection {


    public static UserDataRepository provideUserRepository(){return new UserDataRepository();}

    public static ViewModelFactory provideUserViewModelFactory(){
        UserDataRepository userDataRepository = provideUserRepository();
        return new ViewModelFactory(userDataRepository);
    }

    public static RestaurantsDataRepository provideRestaurantsRepository(LifecycleOwner owner, Context context){
        GooglePlaceRepository googlePlaceRepository = new GooglePlaceRepository(owner, context.getString(R.string.google_maps_key));
        return new RestaurantsDataRepository(googlePlaceRepository, owner);
    }

    public static InboxRepository provideInboxRepository(){return new InboxRepository();}

    public static ViewModelFactory provideInboxViewModelFactory(){
        InboxRepository inboxRepository = provideInboxRepository();
        return new ViewModelFactory(inboxRepository);
    }

    public static ViewModelFactory provideRestaurantViewModelFactory(LifecycleOwner owner, Context context){
        RestaurantsDataRepository restaurantsDataRepository = provideRestaurantsRepository(owner, context);
        return new ViewModelFactory(restaurantsDataRepository);
    }

}

