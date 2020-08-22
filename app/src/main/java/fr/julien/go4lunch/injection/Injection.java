package fr.julien.go4lunch.injection;

import fr.julien.go4lunch.factory.ViewModelFactory;
import fr.julien.go4lunch.repository.UserDataRepository;


public class Injection {

    public static UserDataRepository provideUserRepository(){return new UserDataRepository();}
    public static ViewModelFactory provideViewModelFactory(){
        return new ViewModelFactory();
    }

}

