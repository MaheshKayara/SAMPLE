package com.sharedride.utasharedride;

import android.widget.EditText;
import android.widget.TextView;
import android.app.ProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.graphics.Color;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
/**
 * Created by riju on 4/8/16.
 */
public class RegisterActivity extends Activity{

    ProgressDialog prgDialog;
    TextView errorMsg;
    EditText emailText;
    EditText pwdText;
    EditText confirmpwdText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        errorMsg = (TextView)findViewById(R.id.register_error);
        emailText = (EditText)findViewById(R.id.registerEmail);
        pwdText = (EditText)findViewById(R.id.registerPwd);
        confirmpwdText = (EditText)findViewById(R.id.registerCPwd);
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false);
    }

    public void registerUser(View view){
        String username = emailText.getText().toString();
        String password = pwdText.getText().toString();
        String cpassword = confirmpwdText.getText().toString();
        RequestParams params = new RequestParams();
        if(Utility.isNotNull(username) && Utility.isNotNull(password) && Utility.isNotNull(cpassword)){
            if(Utility.validate(username)){
                params.put("username", username);
                if(password.equals(cpassword))
                {
                    params.put("password", password);
                    invokeWS(params);
                }
                else{
                    //Toast.makeText(getApplicationContext(), "Password and Confirm Password Should Match", Toast.LENGTH_LONG).show();
                    errorMsg.setText("Password and Confirm Password Should Match");
                }
            }
            else{
                //Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_LONG).show();
                errorMsg.setText("Please enter valid email");
            }
        }
        else{
           // Toast.makeText(getApplicationContext(), "Please fill the form, don't leave any field blank", Toast.LENGTH_LONG).show();
            errorMsg.setText("Please fill the form, don't leave any field blank");
        }
    }

    public void invokeWS(RequestParams params){
        prgDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestHandle requestHandle = client.get("http://192.168.43.125:8080/SharedRide/signup/doregister", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                prgDialog.hide();
                try {
                    String response = new String(bytes);
                    JSONObject obj = new JSONObject(response);
                    if (obj.getBoolean("status")) {
                        setDefaultValues();
                        errorMsg.setTextColor(Color.GREEN);
                        errorMsg.setText("Registration Successful. Please click on Login Button to Login");
                        //Toast.makeText(getApplicationContext(), "You are successfully registered! Click on Login Button to Login", Toast.LENGTH_LONG).show();
                    }
                    // Else display error message
                    else {
                        errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                prgDialog.hide();
                if (i == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (i == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void navigatetoLoginActivity(View view){
      Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);
      loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(loginIntent);
    }

    /**
     * Set degault values for Edit View controls
     */
    public void setDefaultValues(){
        emailText.setText("");
        pwdText.setText("");
        confirmpwdText.setText("");
    }
}
