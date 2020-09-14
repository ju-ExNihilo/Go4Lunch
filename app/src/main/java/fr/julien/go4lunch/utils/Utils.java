package fr.julien.go4lunch.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.GregorianCalendar;

public class Utils {

    private OnClickPositiveButtonDialog onClickPositiveButtonDialog;

    public interface OnClickPositiveButtonDialog{
        void positiveButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch);
        void negativeButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch);
    }

    public Utils(OnClickPositiveButtonDialog onClickPositiveButtonDialog) {
        this.onClickPositiveButtonDialog = onClickPositiveButtonDialog;
    }

    public static int getIndexOfToday() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        int today = calendar.get(calendar.DAY_OF_WEEK);
        int indexOfToday = 0;

        switch (today) {
            case GregorianCalendar.MONDAY:
                indexOfToday = 0;
                break;
            case GregorianCalendar.TUESDAY:
                indexOfToday = 1;
                break;
            case GregorianCalendar.WEDNESDAY:
                indexOfToday = 2;
                break;
            case GregorianCalendar.THURSDAY:
                indexOfToday = 3;
                break;
            case GregorianCalendar.FRIDAY:
                indexOfToday = 4;
                break;
            case GregorianCalendar.SATURDAY:
                indexOfToday = 5;
                break;
            case GregorianCalendar.SUNDAY:
                indexOfToday = 6;
                break;

        }

        return indexOfToday;

    }

    public static void rotateAnimation(FloatingActionButton fab, int rotationAngle, int nextDrawableId){
        fab.animate()
                .rotationBy(rotationAngle)        // rest 180 covered by "shrink" animation
                .setDuration(100)
                .scaleX(1.1f)           //Scaling to 110%
                .scaleY(1.1f)           //Scaling to 110%
                .withEndAction(() -> {

                    //Changing the icon by the end of animation
                    fab.setImageResource(nextDrawableId);

                })
                .start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showAlertDialog(Context context, String dialogTitle, String dialogMessage,
                                String positiveButtonText, String negativeButtonText,
                                int dialogDrawableBackground, int dialogDrawableIcon, int dialogIdForSwitch){

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
        dialogBuilder.setTitle(dialogTitle);
        dialogBuilder.setMessage(dialogMessage);
        dialogBuilder.setIcon(dialogDrawableIcon);
        dialogBuilder.setBackground(context.getResources().getDrawable(dialogDrawableBackground, null));
        dialogBuilder.setPositiveButton(positiveButtonText, (dialog, which) -> onClickPositiveButtonDialog.positiveButtonDialogClicked(dialog, dialogIdForSwitch));
        dialogBuilder.setNegativeButton(negativeButtonText, (dialog, which) -> onClickPositiveButtonDialog.negativeButtonDialogClicked(dialog, dialogIdForSwitch));
        dialogBuilder.show();


    }
}
