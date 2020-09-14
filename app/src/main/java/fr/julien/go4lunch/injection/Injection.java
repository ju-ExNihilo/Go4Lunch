package fr.julien.go4lunch.injection;

import androidx.lifecycle.LifecycleOwner;
import fr.juju.googlemaplibrary.repository.GooglePlaceRepository;
import fr.julien.go4lunch.MainActivity;
import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.repository.InboxRepository;
import fr.julien.go4lunch.repository.RestaurantsDataRepository;
import fr.julien.go4lunch.repository.UserDataRepository;


public class Injection {

    /** firestore **/
    public static UserDataRepository provideUserRepository(){return new UserDataRepository();}

    public static ViewModelFactory provideUserViewModelFactory(){
        UserDataRepository userDataRepository = provideUserRepository();
        return new ViewModelFactory(userDataRepository);
    }

    public static RestaurantsDataRepository provideRestaurantsRepository(LifecycleOwner owner){
        GooglePlaceRepository googlePlaceRepository = new GooglePlaceRepository(owner, MainActivity.KEY_API);
        return new RestaurantsDataRepository(googlePlaceRepository, owner);
    }

    public static InboxRepository provideInboxRepository(){return new InboxRepository();}

    public static ViewModelFactory provideInboxViewModelFactory(){
        InboxRepository inboxRepository = provideInboxRepository();
        return new ViewModelFactory(inboxRepository);
    }

    public static ViewModelFactory provideRestaurantViewModelFactory(LifecycleOwner owner){
        RestaurantsDataRepository restaurantsDataRepository = provideRestaurantsRepository(owner);
        return new ViewModelFactory(restaurantsDataRepository);
    }

}

