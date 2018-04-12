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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView =  findViewById(R.id.text_results);
        mEndpointOptions = findViewById(R.id.options);

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

            try {
                url = "http://" + url;
                return mRequestManager.makeRequest(url);
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
