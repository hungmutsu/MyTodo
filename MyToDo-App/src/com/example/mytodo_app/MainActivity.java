package com.example.mytodo_app;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
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

public class MainActivity extends AccountAuthenticatorActivity {

  private static final String TAG = "LOGIN";
  private static final String URL_HOST = "http://192.168.1.77/mytodo-service/";
  public static final String JSON_TAG_ERROR = "error";
 
  EditText loginName, loginPassword;
  TextView loginError;
  Button btnLogin;
  
  // Progress Dialog
  private ProgressDialog pDialog;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Log.i(TAG, "URI " + ContentUris.withAppendedId(MyToDo.Tasks.CONTENT_ID_URI_BASE, 10));
    //Check exist account
    AccountManager accountManager = AccountManager.get(this);
    Account[] accounts = accountManager.getAccountsByType(Constant.ACCOUNT_TYPE);
    if(accounts.length > 0) {
      // Get the content resolver for your app
      //ContentResolver mResolver = getContentResolver();
      // Turn on automatic syncing for the default account and authority
      //mResolver.setSyncAutomatically(accounts[0], MyToDo.AUTHORITY, true);
      Log.i(TAG, accounts[0].name);
      //Chuyen thang den TaskList
      Intent i = new Intent(MainActivity.this, TaskListActivity.class);
      startActivity(i);
      finish();
    }
    
    loginName = (EditText) findViewById(R.id.loginName);
    loginPassword = (EditText) findViewById(R.id.loginPassword);
    loginError = (TextView) findViewById(R.id.loginError);
    
    //Đăng nhập
    btnLogin = (Button) findViewById(R.id.btnLogin);
    btnLogin.setOnClickListener(new View.OnClickListener() {
      
      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        Login login = new Login(MainActivity.this);
        login.execute();
      }
    });
    
    //Chuyển sang trang đăng ký
    Button btnLinkToRegisterScreen = (Button) findViewById(R.id.btnLinkToRegisterScreen);
    btnLinkToRegisterScreen.setOnClickListener(new View.OnClickListener() {
      
      @Override
      public void onClick(View v) {
        pDialog = ProgressDialog.show(MainActivity.this, "Loading...", "Please wait...", false, true);
        Intent i = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(i);
        finish();
      }
      
    });
    
  }

  /**
   * Background Async Task to Get complete user details
   * */
  private class Login extends AsyncTask<String, Void, Boolean> {
    Context mContext;
    
    public Login(Context context) {
      mContext = context;
      btnLogin.setEnabled(false);

      pDialog = ProgressDialog.show(context, "Loading...", "Please wait...", false, true);
    }
    /**
     * Getting user details in background thread
     * */
    @Override
    protected Boolean doInBackground(String... params) {

      // Check for success tag
      boolean error;
      System.out.println(loginPassword.getText().toString());

      String username = loginName.getText().toString();
      String password = loginPassword.getText().toString();
      /*String deviceId = Secure.getString(getActivity().getContentResolver(),
                    Secure.ANDROID_ID);
      Log.d("android_id:", deviceId);*/
      
      try {
        // Building Parameters
        String[] keys = new String[] { "username", "password"};
        String[] values = new String[] { username, password };
        
        JSONObject json = NetworkUtils.postJSONObjFromUrl(
            URL_HOST + "login.php", keys, values);
        // json error tag
        error = json.getBoolean(JSON_TAG_ERROR);
        
        if (!error) {
         
          // get first User object from JSON Array
          JSONObject userObj = json.getJSONArray("user").getJSONObject(0); // JSON Array

          
          Bundle result = null;
          Account account = new Account(username, Constant.ACCOUNT_TYPE);
          AccountManager acountManager = AccountManager.get(mContext);
          
          if (acountManager.addAccountExplicitly(account, password, null)) {
            result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            setAccountAuthenticatorResult(result);
            
            return true;
          } else {
            Log.i("LOGIN", "TON TAI");
            return false;
          }
        } else {
          loginError.setText("Incorrect username/password");
          return false;
        }
      } catch (JSONException e) {
        e.printStackTrace();
        return false;
      }
     
    }

    /**
     * After completing background task Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(Boolean result) {
      // dismiss the dialog once got all details
      btnLogin.setEnabled(true);
      pDialog.dismiss();
      if(result)
      {
        Intent i = new Intent(MainActivity.this, TaskListActivity.class);
        startActivity(i);
        finish();
      }
    }
  }
}
