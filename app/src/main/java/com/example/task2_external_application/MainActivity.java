package com.example.task2_external_application;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {
EditText editText;
Button button;
String data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scanCode();
            }

            private void scanCode() {
                ScanOptions options = new ScanOptions();
                options.setPrompt("Vol Up to Flash On");
                options.setBeepEnabled(true);
                options.setOrientationLocked(true);
                options.setCaptureActivity(CaptureAct.class);
                barLauncher.launch(options);

            }
            ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(),result -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Code Validated");
                builder.setMessage(result.getContents());
                String scannedvalue = result.getContents();
                data = scannedvalue.replaceAll("[^0-9]+", "");
                new PostDataTask().execute(data);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            });
        });
    }



    private class PostDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                // Set up the request
                TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                            public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        }
                };

                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                SSLContext sslContext1 = SSLContext.getInstance("SSL");
                sslContext1.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

                // Set up the request
                URL url = new URL("https://10.3.101.40/certificateverification/fetch_component.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Add parameters to the request
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                String postData = "id=" + params[0] + "&search_post_btn=1";
                writer.write(postData);
                writer.flush();

                // Send the request and read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                reader.close();

                // Return the response
                return sb.toString();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Handle the response here
            // For example, update UI with the received data
            String css = "<style>\n" +
                    "* {\n" +
                    "    /* Change your font family */\n" +
                    "    font-family: sans-serif;\n" +
                    "}\n" +
                    "\n" +
                    ".fetched-table{\n" +
                    "    border-collapse: collapse;\n" +
                    "    margin: 25px 0;\n" +
                    "    font-size: 0.9em;\n" +
                    "    min-width: 400px;\n" +
                    "    border-radius: 5px 5px 0 0;\n" +
                    "    overflow: hidden;\n" +
                    "    box-shadow: 0 0 20px rgba(0, 0, 0, 0.15);\n" +
                    "}\n" +
                    "\n" +
                    ".fetched-table .msg{\n" +
                    "    background-color: #009879 ;\n" +
                    "    color: #ffffff;\n" +
                    "    text-align: left;\n" +
                    "    font-weight: bold;\n" +
                    "}\n" +
                    ".content-table th,\n" +
                    ".content-table td {\n" +
                    "    padding: 12px 15px;\n" +
                    "}\n" +
                    "\n" +
                    ".content-table tr{\n" +
                    "    border-bottom: 1px solid #dddddd;\n" +
                    "    \n" +
                    "}\n" +
                    "\n" +
                    ".content-table tr:nth-of-type(even) {\n" +
                    "    background-color: #f3f3f3;\n" +
                    "}\n" +
                    "\n" +
                    ".fetched-table .tbd tr:last-of-type {\n" +
                    "    font-weight: bold;\n" +
                    "    border-bottom: 2px solid #009879;\n" +
                    "}\n" +
                    "\n" +
                    "</style>";
            result = css+"<center>"+result+"</center>";

            System.out.println(result);
            WebView webView = findViewById(R.id.webView);
            webView.loadDataWithBaseURL(null, result, "text/html", "UTF-8", null);
        }
    }}