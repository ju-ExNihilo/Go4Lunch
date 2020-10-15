package fr.julien.go4lunch.workmates;

import android.view.Gravity;
import android.widget.EditText;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LifecycleOwner;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import com.google.firebase.auth.FirebaseAuth;
import fr.julien.go4lunch.MainActivity;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.models.User;
import fr.julien.go4lunch.repository.RestaurantsDataRepository;
import fr.julien.go4lunch.repository.UserDataRepository;
import fr.julien.go4lunch.utils.ClickChatButton;
import fr.julien.go4lunch.utils.LiveDataTestUtil;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static fr.julien.go4lunch.utils.RecyclerViewItemCountAssertion.withItemCount;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class WorkmatesTest {

    private FirebaseAuth firebaseAuth;
    private UserDataRepository userDataRepository;
    private final String userName = "UserForTest";
    private final String userId = "2JVECuHm3iWNMeyl70XLAYkPXY72";
    private UiDevice device;



    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws InterruptedException, UiObjectNotFoundException {
        firebaseAuth = FirebaseAuth.getInstance();
        configureUserRespository();
        device = UiDevice.getInstance(getInstrumentation());
        loginAndCreateUser();
        Thread.sleep(8000);
        userDataRepository.updateName(userId,userName);
    }

    @After
    public void clearDown() {logoutAndDeleteUser();}

    @Test
    public void goToWorkmatesFragmentAndSeeIfAllWorkmatesAreDisplayed() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        onView(withId(R.id.workmatesFragment)).perform(click());
        onView(ViewMatchers.withId(R.id.layout_workmates));
        Thread.sleep(2000);
        int nbrUser = LiveDataTestUtil.getValue(userDataRepository.getAllUsersForTest()).size();
        onView(ViewMatchers.withId(R.id.list_users)).check(withItemCount(nbrUser));
    }

    @Test
    public void goToWorkmatesFragmentAndSeeIfAfterSearchWorkmatesAreDisplayed() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        onView(withId(R.id.workmatesFragment)).perform(click());
        onView(ViewMatchers.withId(R.id.layout_workmates));
        onView(withId(R.id.action_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(userName), pressImeActionButton());
        Thread.sleep(2000);
        int nbrUser = LiveDataTestUtil.getValue(userDataRepository.getSearchUsersForTest(userName)).size();
        onView(ViewMatchers.withId(R.id.list_users)).check(withItemCount(nbrUser));
    }

    @Test
    public void goToWorkmatesFragmentAndSeeIfPressBackAfterSearchDisplayedAllWorkmates() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        onView(withId(R.id.workmatesFragment)).perform(click());
        onView(ViewMatchers.withId(R.id.layout_workmates));
        onView(withId(R.id.action_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(userName), pressImeActionButton());
        Thread.sleep(2000);
        int nbrSearchUser = LiveDataTestUtil.getValue(userDataRepository.getSearchUsersForTest(userName)).size();
        onView(ViewMatchers.withId(R.id.list_users)).check(withItemCount(nbrSearchUser));
        Espresso.pressBack();
        Espresso.pressBack();
        Thread.sleep(2000);
        int nbrUser = LiveDataTestUtil.getValue(userDataRepository.getAllUsersForTest()).size();
        onView(ViewMatchers.withId(R.id.list_users)).check(withItemCount(nbrUser));
    }

    @Test
    public void goToWorkmatesFragmentAndSeeIfClickItemDisplayTheChosenLunch() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        onView(withId(R.id.listViewFragment)).perform(click());
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.layout_list_view));
        onView(ViewMatchers.withId(R.id.list_restaurants)).perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));
        onView(ViewMatchers.withId(R.id.details_activity_layout));
        Thread.sleep(1000);
        onView(withId(R.id.restaurant_details_floating_btn)).perform(click());
        Thread.sleep(1000);
        device.pressBack();
        onView(withId(R.id.workmatesFragment)).perform(click());
        onView(ViewMatchers.withId(R.id.layout_workmates));
        Thread.sleep(1000);
        onView(ViewMatchers.withId(R.id.list_users)).perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(userName)), click()));
        Thread.sleep(1000);
        onView(ViewMatchers.withId(R.id.details_activity_layout));
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.restaurant_details_name))
                .check(matches(withText(containsString(LiveDataTestUtil.getValue(userDataRepository.getUserFromFirestore()).getEatingPlace()))));
        device.pressBack();
    }

    @Test
    public void goToWorkmatesFragmentAndSeeIfClickOnChatButtonDisplayedChatRoomFragment() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        onView(withId(R.id.workmatesFragment)).perform(click());
        onView(ViewMatchers.withId(R.id.layout_workmates));
        onView(ViewMatchers.withId(R.id.list_users)).perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(userName)), new ClickChatButton()));
        onView(ViewMatchers.withId(R.id.chat_room_layout));
        Thread.sleep(2000);
        device.pressBack();
        device.pressBack();
    }


    /** Configure user Repository **/
    private void configureUserRespository(){
        userDataRepository = Injection.provideUserRepository();
    }


    public void loginAndCreateUser() throws InterruptedException, UiObjectNotFoundException {
        onView(withId(R.id.iden_button)).perform(click());
        Thread.sleep(2000);
        device.pressBack();

        // Set Email
        UiObject emailInput = device.findObject(new UiSelector()
                .instance(0)
                .className(EditText.class));
        emailInput.setText("enzotouti@outlook.com");
        device.pressEnter();

        // Set Password
        UiObject passwordInput = device.findObject(new UiSelector()
                .instance(0)
                .className(EditText.class));
        Thread.sleep(2000);
        passwordInput.setText("azerty");
        device.pressEnter();
    }

    public void logoutAndDeleteUser() {
        // Logout
        String uid = firebaseAuth.getCurrentUser().getUid();
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.logout));
        onView(ViewMatchers.withId(R.id.login_activity));
        userDataRepository.deleteUser(uid);
    }

}
