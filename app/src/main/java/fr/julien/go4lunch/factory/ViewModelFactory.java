package fr.julien.go4lunch.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.Executor;

import fr.julien.go4lunch.repository.UserDataRepository;
import fr.julien.go4lunch.viewmodel.UserViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {

    public ViewModelFactory() {}

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserViewModel.class)){
            return (T) new UserViewModel();
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
