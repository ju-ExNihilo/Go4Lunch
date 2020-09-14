package fr.julien.go4lunch.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import fr.julien.go4lunch.repository.InboxRepository;
import fr.julien.go4lunch.repository.RestaurantsDataRepository;
import fr.julien.go4lunch.repository.UserDataRepository;
import fr.julien.go4lunch.viewmodel.InboxViewModel;
import fr.julien.go4lunch.viewmodel.RestaurantsViewModel;
import fr.julien.go4lunch.viewmodel.UserViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private UserDataRepository userDataRepository;
    private RestaurantsDataRepository restaurantsDataRepository;
    private InboxRepository inboxRepository;

    public ViewModelFactory(UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }

    public ViewModelFactory(RestaurantsDataRepository restaurantsDataRepository) {
        this.restaurantsDataRepository = restaurantsDataRepository;
    }

    public ViewModelFactory(InboxRepository inboxRepository) {
        this.inboxRepository = inboxRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserViewModel.class)){
            return (T) new UserViewModel(userDataRepository);
        }else if (modelClass.isAssignableFrom(RestaurantsViewModel.class)){
            return (T) new RestaurantsViewModel(restaurantsDataRepository);
        }else if (modelClass.isAssignableFrom(InboxViewModel.class)){
            return (T) new InboxViewModel(inboxRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
