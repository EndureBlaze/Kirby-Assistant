package cn.endureblaze.kirby.base;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import cn.bmob.v3.Bmob;
import cn.endureblaze.kirby.R;
import cn.endureblaze.kirby.manager.ActManager;
import cn.endureblaze.kirby.util.FileUtil;
import cn.endureblaze.kirby.util.LanguageUtil;
import cn.endureblaze.kirby.util.ThemeUtil;
import com.oasisfeng.condom.CondomContext;

import java.util.Objects;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    private final String WINDOW_HIERARCHY_TAG = "window_save";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActManager.addActivity(this);
        super.onCreate(savedInstanceState);
        //保证缓存目录下重要目录的存在
        if (!FileUtil.isfFileIsExists(Objects.requireNonNull(this.getExternalCacheDir()) + "/bmob/0"))
            FileUtil.newFile(this.getExternalCacheDir() + "/bmob/", "0");
        if (!FileUtil.isfFileIsExists(Objects.requireNonNull(this.getCacheDir()) + "/image_manager_disk_cache/0"))
            FileUtil.newFile(this.getCacheDir() + "/image_manager_disk_cache/", "0");
        LanguageUtil.setLanguage();
        ThemeUtil.setClassTheme(this);
        Bmob.initialize(CondomContext.wrap(this, "Bmob"), "e39c2e15ca40b358b0dcc933236c1165");
        if (ThemeUtil.getThemeId(this) == ThemeUtil.WHITE_THEME) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStatusBar(ThemeUtil.getThemeColorById(this, R.attr.colorPrimary));
            getWindow().setNavigationBarColor(ThemeUtil.getThemeColorById(this, R.attr.colorPrimary));
            if (layoutResID == R.layout.activity_game || layoutResID == R.layout.activity_user_avatar) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBar(int color) {
        getWindow().setStatusBarColor(color);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(WINDOW_HIERARCHY_TAG, getWindow().saveHierarchyState());
    }

    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (getWindow() != null) {
            Bundle windowState = savedInstanceState.getBundle(WINDOW_HIERARCHY_TAG);
            if (windowState != null) {
                getWindow().restoreHierarchyState(windowState);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /*@Override
    protected void attachBaseContext(Context newBase) {

        Locale newLocale = Locale.getDefault();
        //读取SharedPreferences数据，默认选中第一项
        SharedPreferences preferences = newBase. getSharedPreferences("setting", 0);
        String language = preferences.getString("language", "auto");

        switch (language)
        {
            case "auto":
                newLocale = Locale.getDefault();
                break;
            case "zh_cn":
                newLocale = Locale.CHINA;
                break;
            case "zh_tw":
                newLocale = Locale.TAIWAN;
                break;
            case "en":
                newLocale = Locale.ENGLISH;
                break;
            default:
                break;
        }

        Context context = MyContextWrapper.wrap(newBase, newLocale);
        super.attachBaseContext(context);
    }*/
}
