package fr.julien.go4lunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import fr.julien.go4lunch.models.User;
import fr.julien.go4lunch.repository.UserDataRepository;

public class UserViewModel extends ViewModel implements UserDataRepository.OnRequestTaskComplete {

    private UserDataRepository userDataRepository = new UserDataRepository(this);
    /** DATA **/
    private MutableLiveData<List<User>> users = new MutableLiveData<>();
    private MutableLiveData<List<User>> user = new MutableLiveData<>();


    public UserViewModel() {userDataRepository.getAllUsers();}

    /** GET **/
    public LiveData<List<User>> getAllUser() { return this.users;  }

    public LiveData<List<User>> getSearchUser(String query) {
        userDataRepository.getSearchUsers(query);
        return this.user;  }

    /** INSERT **/
    public void createUser(User user) { userDataRepository.createUser(user);}

    @Override
    public void usersListData(List<User> usersListModels) { users.setValue(usersListModels); }

    @Override
    public void usersSearchData(List<User> userModel) { user.setValue(userModel); }

    @Override
    public void onError(Exception e) {}
}
