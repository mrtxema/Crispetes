package cat.mrtxema.crispetes;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EActivity
public abstract class BaseActivity extends Activity {
    @ViewById TextView txtMessages;
    @ViewById View loadingPanel;

    @Override
    public void setContentView(int layoutResID) {
        boolean customTitle = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.setContentView(layoutResID);
        if (customTitle) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        }
    }

    @Click
    void btnTitleHome() {
        startActivityByClass(MainActivity_.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    @Click
    void btnTitleSearch() {
        startActivityByClass(SearchActivity_.class);
    }

    @Click
    void btnTitleSettings() {
        startActivityByClass(SettingsActivity_.class);
    }

    @UiThread
    protected void setLoadingPanelVisibility(int visibility) {
        loadingPanel.setVisibility(visibility);
    }

    @UiThread
    protected void setMessage(String text) {
        txtMessages.setText(text);
        txtMessages.setVisibility(View.VISIBLE);
    }

    @UiThread
    protected void clearMessage() {
        txtMessages.setVisibility(View.GONE);
        txtMessages.setText("");
    }

    protected void startActivityByClass(Class<? extends Activity> target, Integer flags) {
        if (!target.isInstance(this)) {
            Intent intent = new Intent(this, target);
            if (flags != null) {
                intent.setFlags(flags);
            }
            startActivity(intent);
        }
    }

    protected void startActivityByClass(Class<? extends Activity> target) {
        startActivityByClass(target, null);
    }
}
