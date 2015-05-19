package com.example.android.securenetworkpinning;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity {

    private RequestManager mRequestManager;

    private TextView mTextView;
    private RadioGroup mEndpointOptions;
    private CheckBox mSecureEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text_results);
        mEndpointOptions = (RadioGroup) findViewById(R.id.options);
        mSecureEnable = (CheckBox) findViewById(R.id.secure_enable);

        mRequestManager = RequestManager.getInstance(this);
    }

    public void onRequestClick(View v) {
        RequestTask task = new RequestTask();
        switch (mEndpointOptions.getCheckedRadioButtonId()) {
            case R.id.option_google:
                task.execute("news.google.com/?output=rss");
                break;
            case R.id.option_httpbin:
                task.execute("httpbin.org/xml");
                break;
            default:
                throw new IllegalArgumentException("Invalid Option Selected");
        }
        setResultText(null);
    }

    public void setResultText(CharSequence text) {
        mTextView.setText(text);
    }

    private class RequestTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];

            boolean useTls = mSecureEnable.isChecked();

            try {
                if (useTls) {
                    url = "https://" + url;
                    return mRequestManager.makePinnedRequest(url);
                } else {
                    url = "http://" + url;
                    return mRequestManager.makeRequest(url);
                }
            } catch (Exception e) {
                Log.w("RequestTask", "Unable to make request", e);
                return "Error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            setResultText(result);
        }
    }
}
