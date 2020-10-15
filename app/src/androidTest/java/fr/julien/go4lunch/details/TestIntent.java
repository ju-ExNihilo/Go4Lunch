package fr.julien.go4lunch.details;

import android.content.Intent;
import android.view.Gravity;
import android.widget.EditText;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
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
import fr.julien.go4lunch.repository.UserDataRepository;
import org.junit.*;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.*;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestIntent {

    private UserDataRepository userDataRepository;
    private UiDevice device;
    private FirebaseAuth firebaseAuth;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);

    @Before
    public void setUp() throws InterruptedException, UiObjectNotFoundException {
        firebaseAuth = FirebaseAuth.getInstance();
        Intents.init();
        configureUserRespository();
        device = UiDevice.getInstance(getInstrumentation());
        loginAndCreateUser();
        Thread.sleep(8000);
    }

    @After
    public void clearDown(){
        Intents.release();
        logoutAndDeleteUser();
    }

    @Test
    public void goToDetailsAndClickCallButtonShouldStartACTION_DIALIntent() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        onView(withId(R.id.listViewFragment)).perform(click());
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.layout_list_view));
        onView(ViewMatchers.withId(R.id.list_restaurants)).perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));
        onView(ViewMatchers.withId(R.id.details_activity_layout));
        Thread.sleep(1000);
        onView(withId(R.id.call_details)).perform(click());
        Thread.sleep(5000);
        intended(hasAction(Intent.ACTION_DIAL));
        Thread.sleep(15000);
        device.pressBack();
        Thread.sleep(5000);
        device.pressBack();
        Thread.sleep(2000);
        device.pressBack();
        Thread.sleep(2000);
        device.pressBack();
        Thread.sleep(2000);
    }

    @Test
    public void goToDetailsAndClickWebsiteButtonShouldStartACTION_VIEWIntent() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        onView(withId(R.id.listViewFragment)).perform(click());
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.layout_list_view));
        onView(ViewMatchers.withId(R.id.list_restaurants)).perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));
        onView(ViewMatchers.withId(R.id.details_activity_layout));
        Thread.sleep(1000);
        onView(withId(R.id.website_details)).perform(click());
        Thread.sleep(5000);
        intended(hasAction(Intent.ACTION_VIEW));
        Thread.sleep(15000);
        device.pressBack();
        Thread.sleep(5000);
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
