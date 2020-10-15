package fr.julien.go4lunch.mapview;

import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.widget.EditText;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LifecycleOwner;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import com.google.firebase.auth.FirebaseAuth;
import fr.juju.googlemaplibrary.model.FinalPlace;
import fr.julien.go4lunch.MainActivity;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.models.User;
import fr.julien.go4lunch.repository.RestaurantsDataRepository;
import fr.julien.go4lunch.repository.UserDataRepository;
import fr.julien.go4lunch.utils.LiveDataTestUtil;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.List;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapViewTest {

    private MainActivity mainActivity;
    private FirebaseAuth firebaseAuth;
    private UserDataRepository userDataRepository;
    private UiDevice device;
    private RestaurantsDataRepository restaurantsDataRepository;
    private final String searchInput = "sushi";
    private String wrongSearchInput = "dsfjsdmjm";

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws InterruptedException, UiObjectNotFoundException {
        mainActivity = mActivityRule.getActivity();
        firebaseAuth = FirebaseAuth.getInstance();
        configureUserRespository();
        configureRestaurantsRepository();
        device = UiDevice.getInstance(getInstrumentation());
        loginAndCreateUser();
        Thread.sleep(8000);
    }

    @After
    public void clearDown() {logoutAndDeleteUser();}

    @Test
    public void checkIfMapIsDisplayedWithMarker() throws InterruptedException, UiObjectNotFoundException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        String restaurantName = LiveDataTestUtil.getValue(restaurantsDataRepository.getRestaurants()).get(0).getName();
        UiObject marker = device.findObject(new UiSelector().descriptionContains(restaurantName));
        marker.click();
        Thread.sleep(2000);
        assertTrue(marker.exists());
    }

    @Test
    public void clickOnMarckerShouldOpenDetails() throws InterruptedException, UiObjectNotFoundException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        String restaurantName = LiveDataTestUtil.getValue(restaurantsDataRepository.getRestaurants()).get(0).getName();
        UiObject marker = device.findObject(new UiSelector().descriptionContains(restaurantName));
        marker.click();
        Thread.sleep(2000);
        Display display = mActivityRule.getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        int x = screenWidth / 2;
        double y = (screenHeight * 0.43);

        device.click(x, (int) y);
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.details_activity_layout));
        Thread.sleep(1000);
        device.pressBack();
        Thread.sleep(2000);
    }

    @Test
    public void checkIfSearchRestaurantShouldDisplayMarker() throws InterruptedException, UiObjectNotFoundException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        onView(withId(R.id.action_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(searchInput), pressImeActionButton());
        Thread.sleep(2000);
        UiObject marker = device.findObject(new UiSelector().descriptionContains(searchInput));
        marker.click();
        Thread.sleep(2000);
        assertTrue(marker.exists());
    }

    @Test
    public void checkIfWrongSearchRestaurantShouldDisplayAlertDialog() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        onView(withId(R.id.action_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(wrongSearchInput), pressImeActionButton());
        Thread.sleep(2000);
        onView(withText(mainActivity.getString(R.string.no_match))).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform(click());
    }


    /** Configure user Repository **/
    private void configureUserRespository(){
        userDataRepository = Injection.provideUserRepository();
    }

    /** Configure restaurant Repository **/
    private void configureRestaurantsRepository(){
        LifecycleOwner lifecycle = mock(LifecycleOwner.class);;
        restaurantsDataRepository = Injection.provideRestaurantsRepository(lifecycle, ApplicationProvider.getApplicationContext());
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
