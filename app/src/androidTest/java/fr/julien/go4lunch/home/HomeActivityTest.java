package fr.julien.go4lunch.home;

import android.view.Gravity;
import android.widget.EditText;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
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
import com.google.firebase.auth.*;
import fr.julien.go4lunch.MainActivity;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.repository.UserDataRepository;
import fr.julien.go4lunch.utils.LiveDataTestUtil;
import org.junit.*;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomeActivityTest {

    private MainActivity mainActivity;
    private FirebaseAuth firebaseAuth;
    private UserDataRepository userDataRepository;
    private final String userName = "ju";
    private UiDevice device;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws InterruptedException, UiObjectNotFoundException {
        mainActivity = mActivityRule.getActivity();
        firebaseAuth = FirebaseAuth.getInstance();
        configureUserRespository();
        device = UiDevice.getInstance(getInstrumentation());
        loginAndCreateUser();
        Thread.sleep(8000);
    }

    @After
    public void clearDown() {logoutAndDeleteUser();}

    /** Bottom Navigation View Test **/
    @Test
    public void displayRestaurantsListViewFragmentAfterBottomNavigationClickItem() throws InterruptedException {
        onView(withId(R.id.listViewFragment)).perform(click());
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.layout_list_view));
        Thread.sleep(1000);
    }

    @Test
    public void displayMapViewFragmentAfterBottomNavigationClickItem() throws InterruptedException {
        onView(withId(R.id.mapViewFragment)).perform(click());
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.layout_map_view));
        Thread.sleep(1000);
    }

    @Test
    public void displayWorkmateViewFragmentAfterBottomNavigationClickItem() throws InterruptedException {
        onView(withId(R.id.workmatesFragment)).perform(click());
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.layout_workmates));
        Thread.sleep(1000);
    }

    /** Drawer Layout Test **/
    @Test
    public void displayNavigationViewWithCurrentUserName() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());
        onView(withId(R.id.header_avatar_name)).check(matches(withText(containsString(userName))));
        Thread.sleep(1000);
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
        Thread.sleep(1000);
    }

    @Test
    public void displaySettingActivityAfterDrawerNavigationClickItem() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.settings));
        onView(ViewMatchers.withId(R.id.activity_setting_layout));
        Thread.sleep(2000);
        device.pressBack();
        Thread.sleep(2000);
    }

    @Test
    public void displayYourLunchAfterDrawerNavigationClickItem() throws InterruptedException {
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
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.your_lunch));
        onView(ViewMatchers.withId(R.id.details_activity_layout));
        Thread.sleep(2000);
        onView(withId(R.id.restaurant_details_name))
                .check(matches(withText(containsString(LiveDataTestUtil.getValue(userDataRepository.getUserFromFirestore()).getEatingPlace()))));
        Thread.sleep(1000);
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
