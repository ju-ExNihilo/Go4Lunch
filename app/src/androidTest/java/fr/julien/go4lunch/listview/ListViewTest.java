package fr.julien.go4lunch.listview;

import android.view.Gravity;
import android.widget.EditText;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.*;
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
import fr.julien.go4lunch.repository.RestaurantsDataRepository;
import fr.julien.go4lunch.repository.UserDataRepository;
import fr.julien.go4lunch.utils.LiveDataTestUtil;
import org.junit.*;
import org.junit.runner.RunWith;
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
public class ListViewTest {

    private MainActivity mainActivity;
    private FirebaseAuth firebaseAuth;
    private RestaurantsDataRepository restaurantsDataRepository;
    private UserDataRepository userDataRepository;
    private final String searchInput = "sushi";
    private final String wrongSearchInput = "dsfjsdmjm";
    private UiDevice device;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws InterruptedException, UiObjectNotFoundException {
        mainActivity = mActivityRule.getActivity();
        firebaseAuth = FirebaseAuth.getInstance();
        configureRestaurantsRepository();
        configureUserRespository();
        device = UiDevice.getInstance(getInstrumentation());
        loginAndCreateUser();
        Thread.sleep(8000);
    }

    @After
    public void clearDown() {logoutAndDeleteUser();}

    @Test
    public void goToListViewShouldDisplayRecyclerView() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        onView(withId(R.id.listViewFragment)).perform(click());
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.layout_list_view));
        Thread.sleep(2000);
        int nbrCustomer = LiveDataTestUtil.getValue(restaurantsDataRepository.getRestaurants()).size();
        onView(ViewMatchers.withId(R.id.list_restaurants)).check(withItemCount(nbrCustomer));
    }

    @Test
    public void searchViewShouldDisplayedItemWithInputText() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        onView(withId(R.id.listViewFragment)).perform(click());
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.layout_list_view));
        onView(withId(R.id.action_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(searchInput), pressImeActionButton());
        Thread.sleep(2000);
        onView(withText(searchInput)).check(matches(isDisplayed()));
    }

    @Test
    public void searchViewAfterBackPressShouldDisplayedAllItems() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        onView(withId(R.id.listViewFragment)).perform(click());
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.layout_list_view));
        onView(withId(R.id.action_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(searchInput), pressImeActionButton());
        Thread.sleep(2000);
        onView(withText(searchInput)).check(matches(isDisplayed()));
        Espresso.pressBack();
        Espresso.pressBack();
        Thread.sleep(2000);
        int nbrRestaurant = LiveDataTestUtil.getValue(restaurantsDataRepository.getRestaurants()).size();
        onView(ViewMatchers.withId(R.id.list_restaurants)).check(withItemCount(nbrRestaurant));
    }

    @Test
    public void checkIfWrongSearchRestaurantShouldDisplayAlertDialog() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        onView(withId(R.id.listViewFragment)).perform(click());
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.layout_list_view));
        Thread.sleep(2000);
        onView(withId(R.id.action_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(wrongSearchInput), pressImeActionButton());
        Thread.sleep(2000);
        onView(withText(mainActivity.getString(R.string.no_match))).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform(click());
    }

    @Test
    public void clickOnItemShouldDisplayedDetails() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        onView(withId(R.id.listViewFragment)).perform(click());
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.layout_list_view));
        onView(ViewMatchers.withId(R.id.list_restaurants)).perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));
        onView(ViewMatchers.withId(R.id.details_activity_layout));
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.restaurant_details_name))
                .check(matches(withText(containsString(LiveDataTestUtil.getValue(restaurantsDataRepository.getRestaurants()).get(0).getName()))));
        device.pressBack();
        Thread.sleep(2000);
    }

    /** Configure restaurant Repository **/
    private void configureRestaurantsRepository(){
        LifecycleOwner lifecycle = mock(LifecycleOwner.class);;
        restaurantsDataRepository = Injection.provideRestaurantsRepository(lifecycle, ApplicationProvider.getApplicationContext());
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
