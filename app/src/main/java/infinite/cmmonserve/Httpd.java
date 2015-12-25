package infinite.cmmonserve;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.*;
import java.util.*;


public class Httpd extends Activity implements View.OnClickListener
{
    private WebServer server;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        server = new WebServer();
        try {
            server.start();
        } catch(IOException ioe) {
            Log.d("e:",ioe.getMessage().toString());
            Log.w("Httpd", "The server could not start.");
        }
        Log.w("Httpd", "Web server initialized.");

        Toast.makeText(this, "Server set, go connect!", Toast.LENGTH_SHORT);
        findViewById(R.id.connect).setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        Intent intent = VpnService.prepare(getApplicationContext());
        if (intent != null) {
            startActivityForResult(intent, 0);
        } else {
            onActivityResult(0, RESULT_OK, null);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, Vpn.class);
            startService(intent);
        }
    }

    // DON'T FORGET to stop the server
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (server != null)
            server.stop();
    }

    private class WebServer extends NanoHTTPD {

        public WebServer()
        {
            super(8080);
        }


        @Override
        public Response serve(String uri, Method method,
                              Map<String, String> header,
                              Map<String, String> parameters,
                              Map<String, String> files) {
            String answer = "";
            try {
                // Open file from SD Card
                File root = Environment.getExternalStorageDirectory();
                FileReader index = new FileReader(root.getAbsolutePath() +
                        "/www/index.html");
                BufferedReader reader = new BufferedReader(index);
                String line = "";
                while ((line = reader.readLine()) != null) {
                    answer += line;
                }
                reader.close();

            } catch(IOException ioe) {
                Log.w("Httpd", ioe.toString());
            }


            return new NanoHTTPD.Response(answer);
        }
    }

}