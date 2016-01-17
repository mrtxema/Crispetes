package cat.mrtxema.crispetes;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import cat.mrtxema.crispetes.model.Credentials;
import cat.mrtxema.crispetes.service.Store;
import cat.mrtxema.crispetes.store.DatabaseManager;
import cat.mrtxema.crispetes.store.StoreException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.Map;

@EActivity(R.layout.activity_credentials)
public class CredentialsActivity extends BaseActivity {
    @Bean DatabaseManager database;
    @Extra Store store;
    @Extra Credentials credentials;
    @ViewById TextView credentialsTitle;
    @ViewById ViewGroup lstParameters;
    private Map<String, EditText> formFields;

    @AfterViews
    void initViews() {
        credentialsTitle.setText(getString(R.string.credentialsTitle, capitalize(store.getCode())));
        formFields = new HashMap<>();
        for (String parameter : store.getLoginParameters()) {
            String parameterValue = credentials == null ? null : credentials.getParameters().get(parameter);
            lstParameters.addView(inflateView(parameter, parameterValue));
        }
        setLoadingPanelVisibility(View.GONE);
    }

    private View inflateView(String item, String value) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View rowView = inflater.inflate(R.layout.listitem_form, null, false);
        TextView fieldLabel = (TextView) rowView.findViewById(R.id.fieldLabel);
        fieldLabel.setText(capitalize(item));
        EditText formField = (EditText) rowView.findViewById(R.id.fieldInput);
        if (item.toLowerCase().contains("password")) {
            formField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        if (value != null) {
            formField.setText(value);
        }
        formFields.put(item, formField);
        return rowView;
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Click
    void btnCancel() {
        finishActivityWithResult(RESULT_CANCELED);
    }

    @Click
    void btnOk() {
        clearMessage();
        saveCredentials();
    }

    private Map<String, String> getFormParameters() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, EditText> entry : formFields.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getText().toString());
        }
        return result;
    }

    @Background
    void saveCredentials() {
        Credentials newCredentials = new Credentials(store.getCode(), getFormParameters());
        try {
            if (credentials != null) {
                database.deleteCredentials(this, credentials);
            }
            database.saveCredentials(this, newCredentials);
            finishActivityWithResult(RESULT_OK);
        } catch (StoreException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
    }

    @UiThread
    protected void finishActivityWithResult(int result) {
        setResult(result);
        finish();
    }
}
