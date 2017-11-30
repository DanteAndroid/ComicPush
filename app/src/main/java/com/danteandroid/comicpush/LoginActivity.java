package com.danteandroid.comicpush;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.utils.KeyboardUtils;
import com.blankj.utilcode.utils.ToastUtils;
import com.danteandroid.comicpush.net.API;
import com.danteandroid.comicpush.net.NetService;
import com.danteandroid.comicpush.utils.AppUtil;
import com.danteandroid.comicpush.utils.SpUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    @BindView(R.id.account)
    TextInputEditText account;
    @BindView(R.id.accountWrapper)
    TextInputLayout accountWrapper;
    @BindView(R.id.psw)
    TextInputEditText psw;
    @BindView(R.id.pswWrapper)
    TextInputLayout pswWrapper;
    @BindView(R.id.login)
    Button login;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.root)
    LinearLayout root;
    @BindView(R.id.register)
    TextView register;

    private boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("登录VOL");
        }
        isLogin = SpUtil.getBoolean(Constants.IS_LOGIN);
        if (isLogin) {
            goMain();
        } else {
            KeyboardUtils.showSoftInput(account);
        }
        psw.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });
        login.setOnClickListener(v -> attemptLogin());
        register.setOnClickListener(v -> AppUtil.openBrowser(LoginActivity.this, API.REGISTER_URL));
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        accountWrapper.setError(null);
        pswWrapper.setError(null);

        String email = account.getText().toString();
        String password = psw.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            pswWrapper.setError("密碼無效");
            focusView = pswWrapper;
            cancel = true;
        }
        if (TextUtils.isEmpty(email) || !isEmailValid(email)) {
            accountWrapper.setError("用戶名無效");
            focusView = accountWrapper;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            login(email, password);
        }
    }

    private void login(String email, String psw) {
        NetService.request().login(email, psw, "on")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    try {
                        String data = response.string();
                        if (data.contains("e400")) {
                            Toast.makeText(LoginActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                        } else if (data.contains("m100")) {
                            isLogin = true;
                            SpUtil.save(Constants.IS_LOGIN, true);
                            goMain();

                        } else {
                            Log.e(TAG, "call: " + data);
                        }
                        showProgress(false);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    ToastUtils.showShortToast("登录失败: " + throwable.getMessage());
                    pswWrapper.requestFocus();
                });
    }

    private void goMain() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }


    private void showProgress(final boolean show) {
        KeyboardUtils.hideSoftInput(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(root);
        }
        if (show) {
            login.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        } else {
            progress.setVisibility(View.INVISIBLE);
            login.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }
}

