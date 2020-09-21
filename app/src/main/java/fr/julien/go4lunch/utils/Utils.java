package fr.julien.go4lunch.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Build;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import fr.julien.go4lunch.R;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.GregorianCalendar;

public class Utils {

    private OnClickButtonAlertDialog onClickButtonAlertDialog;
    private OnClickItemListAlertDialog onClickItemListAlertDialog;
    private OnClickButtonInpuDialog onClickButtonInpuDialog;

    public interface OnClickButtonAlertDialog {
        void positiveButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch);
        void negativeButtonDialogClicked(DialogInterface dialog, int dialogIdForSwitch);
    }

    public interface OnClickItemListAlertDialog {
        void onItemListDialogClicked(DialogInterface dialog, ArrayAdapter arrayAdapter, int position);
    }

    public interface OnClickButtonInpuDialog {
        void onClickedPositiveButtonInpuDialog(DialogInterface dialog, TextInputEditText textInputEditText, int dialogIdForSwitch);
        void onClickedNegativeButtonInpuDialog(DialogInterface dialog);
    }

    /** Construct **/
    public Utils(OnClickButtonAlertDialog onClickButtonAlertDialog) {
        this.onClickButtonAlertDialog = onClickButtonAlertDialog;
    }

    public Utils(OnClickButtonAlertDialog onClickButtonAlertDialog, OnClickItemListAlertDialog onClickItemListAlertDialog, OnClickButtonInpuDialog onClickButtonInpuDialog) {
        this.onClickButtonAlertDialog = onClickButtonAlertDialog;
        this.onClickItemListAlertDialog = onClickItemListAlertDialog;
        this.onClickButtonInpuDialog = onClickButtonInpuDialog;
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


    public void showAlertDialog(Context context, String dialogTitle, String dialogMessage,
                                String positiveButtonText, String negativeButtonText,
                                int dialogDrawableBackground, int dialogDrawableIcon, int dialogIdForSwitch){

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
        dialogBuilder.setTitle(dialogTitle);
        dialogBuilder.setMessage(dialogMessage);
        dialogBuilder.setPositiveButton(positiveButtonText, (dialog, which) -> onClickButtonAlertDialog.positiveButtonDialogClicked(dialog, dialogIdForSwitch));
        dialogBuilder.setNegativeButton(negativeButtonText, (dialog, which) -> onClickButtonAlertDialog.negativeButtonDialogClicked(dialog, dialogIdForSwitch));
        alertBody(context, dialogDrawableBackground, dialogDrawableIcon, dialogBuilder);


    }

    public void showMessageDialog(Context context, String dialogTitle, String dialogMessage,String negativeButtonText,
                                int dialogDrawableBackground, int dialogDrawableIcon, int dialogIdForSwitch){

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
        dialogBuilder.setTitle(dialogTitle);
        dialogBuilder.setMessage(dialogMessage);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton(negativeButtonText, (dialog, which) -> onClickButtonAlertDialog.negativeButtonDialogClicked(dialog, dialogIdForSwitch));
        alertBody(context, dialogDrawableBackground, dialogDrawableIcon, dialogBuilder);


    }


    public void showAlertListDialog(Context context, View dialogTitle, int dialogDrawableBackground, int dialogDrawableIcon, ArrayAdapter arrayAdapter){

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
        dialogBuilder.setCustomTitle(dialogTitle);
        dialogBuilder.setIcon(dialogDrawableIcon);
        ContextCompat.getDrawable(context, dialogDrawableBackground);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            dialogBuilder.setBackground(context.getResources().getDrawable(dialogDrawableBackground, null));
        } else{

            dialogBuilder.setBackground(context.getResources().getDrawable(dialogDrawableBackground));
        }

        dialogBuilder.setView(R.layout.assignment_dialog_list_view);
        dialogBuilder.setAdapter(arrayAdapter, (dialog, which) ->
            onClickItemListAlertDialog.onItemListDialogClicked(dialog, arrayAdapter, which)
        );

        dialogBuilder.show();
    }


    public void showAlertInputDialog(Context context, String dialogTitle, String dialogMessage,
                                String positiveButtonText, String negativeButtonText,String hint,int inputType,
                                int dialogDrawableBackground, int dialogDrawableIcon, int dialogIdForSwitch){

        TextInputLayout textInputLayout = new TextInputLayout(context);
        textInputLayout.setHint(hint);
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setBackgroundColor(Color.WHITE);
        textInputLayout.setBoxBackgroundColor(Color.WHITE);
        textInputLayout.setPadding(15,0,15,0);
        textInputLayout.setBoxCornerRadii(5, 5, 5, 5);
        TextInputEditText textInputEditText = new TextInputEditText(textInputLayout.getContext());
        textInputEditText.setInputType(inputType);
        textInputLayout.addView(textInputEditText);

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
        dialogBuilder.setTitle(dialogTitle);
        dialogBuilder.setMessage(dialogMessage);
        dialogBuilder.setView(textInputLayout);
        dialogBuilder.setPositiveButton(positiveButtonText, (dialog, which) -> onClickButtonInpuDialog.onClickedPositiveButtonInpuDialog(dialog, textInputEditText, dialogIdForSwitch));
        dialogBuilder.setNegativeButton(negativeButtonText, (dialog, which) -> onClickButtonInpuDialog.onClickedNegativeButtonInpuDialog(dialog));
        alertBody(context, dialogDrawableBackground, dialogDrawableIcon, dialogBuilder);


    }

    private void alertBody(Context context, int dialogDrawableBackground, int dialogDrawableIcon, MaterialAlertDialogBuilder dialogBuilder) {
        dialogBuilder.setIcon(dialogDrawableIcon);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            dialogBuilder.setBackground(context.getResources().getDrawable(dialogDrawableBackground, null));
        } else{
            dialogBuilder.setBackground(context.getResources().getDrawable(dialogDrawableBackground));
        }

        dialogBuilder.show();
    }

    public long getMillisecondeUntilAHours(int hours, int minutes){
        Calendar dueDate = Calendar.getInstance();
        Calendar currentDate = Calendar.getInstance();
        dueDate.set(Calendar.HOUR_OF_DAY, hours);
        dueDate.set(Calendar.MINUTE, minutes);
        dueDate.set(Calendar.SECOND, 0);
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }
        return dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
    }
}
