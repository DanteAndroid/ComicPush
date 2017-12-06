package com.danteandroid.comicpush.base;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.utils.ToastUtils;
import com.bugtags.library.Bugtags;
import com.danteandroid.comicpush.Constants;
import com.danteandroid.comicpush.R;
import com.danteandroid.comicpush.net.Updater;
import com.danteandroid.comicpush.utils.AppUtil;
import com.danteandroid.comicpush.utils.Database;
import com.danteandroid.comicpush.utils.SpUtil;
import com.danteandroid.comicpush.utils.UiUtils;

import io.realm.Realm;


/**
 * the view in setting activity.
 */
public class SettingFragment extends PreferenceFragment {
    public static final String LOG_OFF = "log_off";
    public static final String FEED_BACK = "feedback";
    public static final String ABOUT = "about";
    private static final String TAG = "SettingFragment";
    private static final long DURATION = 300;
    private View rootView;

    private Preference logOff;
    private EditTextPreference feedback;
    private Preference about;
    private SwitchPreference isTraditional;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        logOff = findPreference(LOG_OFF);
        feedback = (EditTextPreference) findPreference(FEED_BACK);
        isTraditional = (SwitchPreference) findPreference(Constants.IS_TRADITIONAL);
        about = findPreference(ABOUT);

        isTraditional.setOnPreferenceChangeListener((preference, newValue) -> {
            AppUtil.restartApp(getActivity());
            return true;
        });
        about.setOnPreferenceClickListener(preference -> {
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.about)
                    .setMessage(R.string.about_message)
                    .setNeutralButton(R.string.update, (dialog12, which) -> Updater.getInstance(getActivity()).check(true))
                    .setPositiveButton(R.string.donate, (dialog1, which) -> AppUtil.donate(getActivity()))
                    .show();
            ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
            return true;
        });

        logOff.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.attention)
                    .setMessage(R.string.logoff_message)
                    .setPositiveButton(R.string.logoff, (dialog, which) -> logOff())
                    .show();
            return true;
        });
        feedback.setOnPreferenceClickListener(preference -> {
            feedback.getEditText().setText("");
            return true;
        });
        feedback.setOnPreferenceChangeListener((preference, newValue) -> {
            if (TextUtils.isEmpty((String) newValue)) return false;
            Bugtags.sendFeedback((String) newValue);
            UiUtils.showSnack(rootView, R.string.thanks_for_feedback);
            return true;
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (null == rootView) {
            rootView = super.onCreateView(inflater, container, savedInstanceState);
        }
        return rootView;

    }

    private void logOff() {
        try {
            SpUtil.clear();
            Database.getInstance(Realm.getDefaultInstance()).clear();
            AppUtil.restartApp(getActivity());
        } catch (Exception e) {
            ToastUtils.showShortToast(e.getMessage());
        }
    }

}
