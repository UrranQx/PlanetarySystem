package ru.spbstu.planetarysystem;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;;

import static java.util.regex.Pattern.matches;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;

public class MySettingsTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("ru.spbstu.planetarysystem", appContext.getPackageName());
    }
    @Rule
    public ActivityTestRule<MySettings> activityRule = new ActivityTestRule<>(MySettings.class);

    @Test
    public void testOnClickWithValidData() {
        // Find the views
        EditText editText = activityRule.getActivity().findViewById(R.id.etxt_time_jump);
        Button button = activityRule.getActivity().findViewById(R.id.button_apply_time_jump);

        // Set the text in the EditText
        editText.setText("10");

        // Click the button
        button.performClick();

        // Verify that the SharedPreferences were updated
        SharedPreferences sharedPreferences = activityRule.getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int timeJump = sharedPreferences.getInt("timeJump", -1);
        assertEquals(10, timeJump);

        // Verify that the toast message is displayed
        onView(withText("Applied")).inRoot(withDecorView(not(activityRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void testOnClickWithInvalidData() {
        MySettings mySettings = new MySettings();
        // Find the views
        EditText editText = mySettings.findViewById(R.id.etxt_time_jump);
        Button button = mySettings.findViewById(R.id.button_apply_time_jump);

        // Set the text in the EditText
        editText.setText("");

        // Click the button
        button.performClick();

        // Verify that the SharedPreferences were not updated
        SharedPreferences sharedPreferences = activityRule.getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int timeJump = sharedPreferences.getInt("timeJump", -1);
        assertEquals(-1, timeJump);

        // Verify that the toast message is displayed
        onView(withText("Enter valid data")).inRoot(withDecorView(not(activityRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));
    }

    /**
     * Что надо точно протестировать:
     * Кнопки
     *
     * Если нажата кнопка Apply, то выдается уведомление (Тост)
     * И если там был текст, то он должен быть записан в память устройтва
     * Если там не было текста, то Ничего не должно перезаписываться
     *
     * Если нажата кнопка Save - сохранить одну конфигурацию ->
     * Если были незаполненные поля, то практически все заполнить дефолтными значениями
     * Если в каком-то из полей достигается порог (Либо значение крайне не верное, либо при таком
     * значении симуляция не поймет пользователя)
     * Если НЕ ЗАПОЛНЕНО поле имени тела, то должен вылезать тост.
     * Должны записываться в память, а именно в должном формате (.json) в data что-то там.
     *
     * Сделать всем полям еще раз проверку на null
     *
     * Кнопка Ресета должна сбрасывать все до заводсвикх настроек
     */
    @Test
    public void onCreate() {
    }
}