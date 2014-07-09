package com.example.mytodo_app;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mytodo_app.provider.MyToDo;
import com.example.mytodo_app.utils.Constant;
import com.example.mytodo_app.utils.NetworkUtils;

public class SignUpActivity extends AccountAuthenticatorActivity {

  private static final String TAG = "SIGNUP";
  private static final String URL_HOST = "http://192.168.1.77/mytodo-service/";
  public static final String JSON_TAG_ERROR = "error";
  // Sync interval constants
  public static final long SECONDS_PER_MINUTE = 60L;
  public static final long SYNC_INTERVAL_IN_MINUTES = 1L;
  public static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;

  EditText signUpName, signUpPassword, signUpFullName;
  TextView signUpError;
  Button btnSignUp;

  // Progress Dialog
  private ProgressDialog pDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);

    signUpName = (EditText) findViewById(R.id.signUpName);
    signUpPassword = (EditText) findViewById(R.id.signUpPassword);
    signUpFullName = (EditText) findViewById(R.id.signUpFullName);
    signUpError = (TextView) findViewById(R.id.signUpError);

    // Đăng ký
    btnSignUp = (Button) findViewById(R.id.btnRegister);
    btnSignUp.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        SignUp signUp = new SignUp(SignUpActivity.this);
        signUp.execute();
      }
    });

    // Chuyển sang trang đăng ký
    Button btnLinkToRegisterScreen = (Button) findViewById(R.id.btnLinkToLoginScreen);
    btnLinkToRegisterScreen.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        pDialog = ProgressDialog.show(SignUpActivity.this, "Loading...", "Please wait...", false, true);
        Intent i = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(i);
        finish();
      }

    });

  }

  /**
   * Background Async Task to Get complete user details
   */
  private class SignUp extends AsyncTask<String, Void, Boolean> {
    Context mContext;

    public SignUp(Context context) {
      mContext = context;
      btnSignUp.setEnabled(false);

      pDialog = ProgressDialog.show(context, "Loading...", "Please wait...", false, true);
    }

    /**
     * Getting user details in background thread
     */
    @Override
    protected Boolean doInBackground(String... params) {

      // Check for error tag
      boolean error;

      String username = signUpName.getText().toString();
      String password = signUpPassword.getText().toString();
      String fullName = signUpFullName.getText().toString();
      
      try {
        // Building Parameters
        String[] keys = new String[] { "username", "password", "name" };

        String[] values = new String[] { username, password, fullName };

        // getting product details by making HTTP request
        // Note that product details url will use GET request
        JSONObject json = NetworkUtils.postJSONObjFromUrl(URL_HOST + "sign-up.php", keys, values);

        // check your log for json response
        Log.d("Json sign-up response : ", json.toString());

        // json success tag
        error = json.getBoolean(JSON_TAG_ERROR);
        if (!error) {

          Bundle result = null;
          Account account = new Account(username, Constant.ACCOUNT_TYPE);
          AccountManager acountManager = AccountManager.get(mContext);

          if (acountManager.addAccountExplicitly(account, password, null)) {
            result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            setAccountAuthenticatorResult(result);

            /*
             * Turn on periodic syncing
             */
            ContentResolver.setSyncAutomatically(account, MyToDo.AUTHORITY, true);

            ContentResolver.addPeriodicSync(account, MyToDo.AUTHORITY, new Bundle(), SYNC_INTERVAL);
            return true;
          } else {
            return false;
          }
        } else {
          signUpError.setText(json.getString("message"));
          return false;
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }

      return null;
    }

    /**
     * After completing background task Dismiss the progress dialog
     **/
    @Override
    protected void onPostExecute(Boolean result) {
      // dismiss the dialog once got all details
      btnSignUp.setEnabled(true);
      pDialog.dismiss();
      if (result) {
        Intent i = new Intent(SignUpActivity.this, TaskListActivity.class);
        startActivity(i);
        finish();
      }
    }
  }
}
