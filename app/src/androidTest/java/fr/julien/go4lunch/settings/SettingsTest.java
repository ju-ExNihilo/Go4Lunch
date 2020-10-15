package fr.julien.go4lunch.settings;

import android.view.Gravity;
import android.widget.EditText;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import fr.julien.go4lunch.MainActivity;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.repository.UserDataRepository;
import fr.julien.go4lunch.utils.LiveDataTestUtil;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SettingsTest {

    private MainActivity mainActivity;
    private FirebaseAuth firebaseAuth;
    private UserDataRepository userDataRepository;
    private UiDevice device;
    private final int scope = 2;


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

    @Test
    public void goToSettingUpdateUserNameAndSeeIfNameHasBeenUpdated() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.settings));
        onView(ViewMatchers.withId(R.id.activity_setting_layout));
        onView(withId(R.id.card_update_name)).perform(click());
        onView(withClassName(Matchers.equalTo(TextInputEditText.class.getName())))
                .perform(typeText("UserTest"));
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(2000);
        device.pressBack();
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());
        onView(ViewMatchers.withId(R.id.header_avatar_name)).check(matches(withText("UserTest")));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());

    }

    @Test
    public void goToSettingUpdateScopeAndSeeIfScopeHasBeenUpdated() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.settings));
        onView(ViewMatchers.withId(R.id.activity_setting_layout));
        onView(withId(R.id.card_update_scope)).perform(click());
        onView(withClassName(Matchers.equalTo(TextInputEditText.class.getName())))
                .perform(typeText("2"));
        Thread.sleep(2000);
        device.pressBack();
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(5000);
        onView(withId(R.id.card_update_scope)).perform(click());
        onView(withText(mainActivity.getString(R.string.error_scope, scope))).check(matches(isDisplayed()));
        onView(withId(android.R.id.button2)).perform(click());
        onView(withId(R.id.card_update_scope)).perform(click());
        onView(withClassName(Matchers.equalTo(TextInputEditText.class.getName())))
                .perform(typeText("200"));
        Espresso.pressBack();
        onView(withId(android.R.id.button1)).perform(click());
        onView(withText(mainActivity.getString(R.string.scope_between))).check(matches(isDisplayed()));
        Thread.sleep(2000);
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(2000);
        device.pressBack();
        Thread.sleep(2000);
    }

    @Test
    public void likeOneRestaurantShouldDisplayThisLikedRestaurantInSettingFavoriteList() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        Thread.sleep(2000);
        onView(withId(R.id.listViewFragment)).perform(click());
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.layout_list_view));
        onView(ViewMatchers.withId(R.id.list_restaurants)).perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));
        onView(ViewMatchers.withId(R.id.details_activity_layout));

        onView(withId(R.id.like_details)).perform(click());
        Thread.sleep(2000);
        device.pressBack();
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.settings));
        onView(ViewMatchers.withId(R.id.activity_setting_layout));
        onView(withId(R.id.card_liked_restaurant)).perform(click());
        Thread.sleep(2000);
        String restaurantName = LiveDataTestUtil.getValue(userDataRepository.getLikedRestaurants()).get(0).getName();
        onView(withText(restaurantName)).perform(click());
        onView(ViewMatchers.withId(R.id.details_activity_layout));
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.restaurant_details_name))
                .check(matches(withText(containsString(restaurantName))));
        onView(withId(R.id.like_details)).perform(click());
        Thread.sleep(2000);
        device.pressBack();
        device.pressBack();
        Thread.sleep(2000);
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
