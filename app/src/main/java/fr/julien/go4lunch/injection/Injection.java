package fr.julien.go4lunch.injection;

import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.networking.GooglePlaceService;
import fr.julien.go4lunch.networking.RetrofitService;
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

    public static RestaurantsDataRepository provideRestaurantsRepository(){
        GooglePlaceService googlePlaceService = RetrofitService.createService(GooglePlaceService.class);
        return new RestaurantsDataRepository(googlePlaceService);
    }

    public static InboxRepository provideInboxRepository(){return new InboxRepository();}

    public static ViewModelFactory provideInboxViewModelFactory(){
        InboxRepository inboxRepository = provideInboxRepository();
        return new ViewModelFactory(inboxRepository);
    }

    public static ViewModelFactory provideRestaurantViewModelFactory(){
        RestaurantsDataRepository restaurantsDataRepository = provideRestaurantsRepository();
        return new ViewModelFactory(restaurantsDataRepository);
    }

}

