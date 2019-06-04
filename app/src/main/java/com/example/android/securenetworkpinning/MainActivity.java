package com.example.android.securenetworkpinning;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private RequestManager requestManager;

    private TextView textView;
    private RadioGroup endpointOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView =  findViewById(R.id.text_results);
        endpointOptions = findViewById(R.id.options);

        requestManager = RequestManager.getInstance(this);
    }

    public void onRequestClick(View v) {
        RequestTask task = new RequestTask();
        switch (endpointOptions.getCheckedRadioButtonId()) {
            case R.id.option_google:
                task.execute("news.google.com/rss");
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
        textView.setText(text);
    }

    private class RequestTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];

            try {
                url = "http://" + url;
                return requestManager.makeRequest(url);
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
