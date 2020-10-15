package fr.julien.go4lunch.utils;

import android.view.View;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import fr.julien.go4lunch.R;
import org.hamcrest.Matcher;

public class ClickChatButton implements ViewAction{
    @Override
    public Matcher<View> getConstraints() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Click on specific button";
    }

    @Override
    public void perform(UiController uiController, View view) {
        View button = view.findViewById(R.id.message_button);
        // Maybe check for null
        button.performClick();
    }
}
