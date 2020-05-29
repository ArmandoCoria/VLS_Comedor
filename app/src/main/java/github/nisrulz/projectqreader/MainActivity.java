/*
 * Copyright (C) 2016 Nishant Srivastava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package github.nisrulz.projectqreader;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Map;

import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class MainActivity extends AppCompatActivity {
  // UI
  private TextView text;
  private EditText numempleado;
  Bundle extras;
  // QREader
  private SurfaceView mySurfaceView;
  private QREader qrEader;

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    numempleado = (EditText) findViewById(R.id.empleado);
    numempleado.setOnEditorActionListener(editorListener);

    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    Window window = this.getWindow();
    window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimary));

    text = (TextView) findViewById(R.id.code_info);


    /*getSupportActionBar().setTitle("Veolus Comedor");*/

  //  getSupportActionBar().setDisplayShowHomeEnabled(true);

    //getSupportActionBar().setIcon(R.mipmap.ic_launcher);


    final Button stateBtn = (Button) findViewById(R.id.btn_start_stop);
    // change of reader state in dynamic
    stateBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (qrEader.isCameraRunning()) {
          stateBtn.setText("Iniciar Lector QR");
          qrEader.stop();
        }

       else {
          stateBtn.setText("Detener Lector QR");
          qrEader.start();

        }
            }

    });

        stateBtn.setVisibility(View.VISIBLE);

        Button restartbtn = (Button) findViewById(R.id.btn_restart_activity);
        restartbtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            finish();
          }
        });

    // Setup SurfaceView
    // -----------------
    mySurfaceView = (SurfaceView) findViewById(R.id.camera_view);

    // Init QREader
    // ------------
    qrEader = new QREader.Builder(this, mySurfaceView,  new QRDataListener() {
      @Override
      public void onDetected(final String data) {
      Log.d("QREader", "Value : " + data);
      text.post(new Runnable() {
          @Override
          public void run() {
            text.setText(data);
            Log.e("QREader", "dataqr : " + data);
          }
        });
      try {
        JSONObject jsonObject= new JSONObject(data);
        Log.e("QREader", "nomina : " + jsonObject);
        //Log.e("QREader", "nomina : " + gson.to);
        Intent i = new Intent(getApplicationContext(), ListadoMenus.class);
        i.putExtra("NoNomina", jsonObject.getString("nomina"));
        i.putExtra("PIN",jsonObject.getString("pin"));
        i.putExtra("NombreEmpleado",jsonObject.getString("nombreEmpl"));

        //i.putExtra("")
        //startActivity(i);
        startActivityForResult(i,1);
      } catch (JSONException e) {
        e.printStackTrace();
      }

      }
    }).facing(QREader.BACK_CAM)
        .enableAutofocus(true)
        .height(mySurfaceView.getHeight())
        .width(mySurfaceView.getWidth())
        .build();
  }

  private TextView.OnEditorActionListener editorListener = new TextView.OnEditorActionListener() {
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
      switch (actionId) {
        case EditorInfo.IME_ACTION_SEND:
          try {
            String emp = "{'nomina':'" + numempleado.getText().toString() + "'}";
            JSONObject jsonObject= new JSONObject(emp);
            Log.e("editorListener", "nomina : " + jsonObject);
            //Log.e("QREader", "nomina : " + gson.to);
            Intent i = new Intent(getApplicationContext(), ListadoMenus.class);
            i.putExtra("NoNomina", jsonObject.getString("nomina"));

            //i.putExtra("")
            //startActivity(i);
            startActivityForResult(i,1);
          } catch (JSONException e) {
            e.printStackTrace();
          }
          break;
      }
      return false;
    }
  };

    @Override
    public void onBackPressed(){
    }

  @Override
  protected void onResume() {
    super.onResume();
    qrEader.initAndStart(mySurfaceView);
    Log.e("QREader", "onResumeeeeeeeeee ");
  }

  @Override
  protected void onPause() {
    super.onPause();

    // Cleanup in onPause()
    // --------------------
    qrEader.releaseAndCleanup();
  }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("QREader", "resultaaaaaaadpo ");
        startActivityForResult(new Intent(MainActivity.this, MainActivity.class),1);
        finish();

    }
}
