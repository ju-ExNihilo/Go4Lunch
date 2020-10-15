package fr.julien.go4lunch.chatroom;

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
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import fr.julien.go4lunch.MainActivity;
import fr.julien.go4lunch.R;
import fr.julien.go4lunch.injection.Injection;
import fr.julien.go4lunch.models.User;
import fr.julien.go4lunch.repository.InboxRepository;
import fr.julien.go4lunch.repository.UserDataRepository;
import fr.julien.go4lunch.utils.ClickChatButton;
import fr.julien.go4lunch.utils.LiveDataTestUtil;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static fr.julien.go4lunch.utils.RecyclerViewItemCountAssertion.withItemCount;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ChatRoomTest {

    private FirebaseAuth firebaseAuth;
    private UserDataRepository userDataRepository;
    private InboxRepository inboxRepository;
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
        configureInboxRespository();
        device = UiDevice.getInstance(getInstrumentation());
        loginAndCreateUser();
        Thread.sleep(8000);
        userDataRepository.updateName(userId,userName);
    }

    @After
    public void clearDown() {logoutAndDeleteUser();}

    @Test
    public void goToChatAndSeeIfMessagesAreDisplayed() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        onView(withId(R.id.workmatesFragment)).perform(click());
        onView(ViewMatchers.withId(R.id.layout_workmates));
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.list_users)).perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(userName)), new ClickChatButton()));
        onView(ViewMatchers.withId(R.id.chat_room_layout));
        Thread.sleep(2000);
        int nbrMessage = LiveDataTestUtil.getValue(inboxRepository.getMessagesForTest(userId, userId)).size();
        onView(ViewMatchers.withId(R.id.inbox_recycler_view)).check(withItemCount(nbrMessage));
        Thread.sleep(1000);
        device.pressBack();
        device.pressBack();
        Thread.sleep(2000);
    }

    @Test
    public void goToChatAndSendMessageMustBeDisplayed() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mapViewFragment));
        onView(withId(R.id.workmatesFragment)).perform(click());
        onView(ViewMatchers.withId(R.id.layout_workmates));
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.list_users)).perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(userName)), new ClickChatButton()));
        onView(ViewMatchers.withId(R.id.chat_room_layout));
        Thread.sleep(2000);
        int nbrMessage = LiveDataTestUtil.getValue(inboxRepository.getMessagesForTest(userId, userId)).size();
        onView(withClassName(Matchers.equalTo(TextInputEditText.class.getName()))).perform(typeText("hello !"));
        onView(withId(R.id.send_btn)).perform(click());
        onView(ViewMatchers.withId(R.id.inbox_recycler_view)).check(withItemCount(nbrMessage+1));
        Thread.sleep(1000);
        device.pressBack();
        device.pressBack();
        Thread.sleep(2000);
    }


    /** Configure inbox Repository **/
    private void configureInboxRespository(){
        inboxRepository = Injection.provideInboxRepository();
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
       //userDataRepository.deleteUser(uid);
    }
}
