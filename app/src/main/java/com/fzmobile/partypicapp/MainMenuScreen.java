package com.fzmobile.partypicapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapCircleThumbnail;
import com.beardedhen.androidbootstrap.BootstrapThumbnail;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapSize;
import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Facundo on 12/06/2017.
 */

public class MainMenuScreen extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

    private ImageView photoImageView;
    private TextView nameTextView;
    private TextView idTextView;
    private TextView emailTextView;
    private TextView instructionsTextView;
    //private Button btnScanQRCode;
    private BootstrapButton btnCerrarSesion;
    private BootstrapButton btnScanQRCode;
    BootstrapCircleThumbnail profileImage;

    public static final int REQUEST_CODE = 100;
    public static final int PERMISSION_REQUEST = 200;
    public boolean facebookLogin = false;

    private ProfileTracker profileTracker;

    private GoogleApiClient googleApiClient;

    private ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_screen);

        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);


        // clear FLAG_TRANSLUCENT_STATUS flag:
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);

        tintManager.setStatusBarTintEnabled(true);


        LinearLayout Ll = (LinearLayout)findViewById(R.id.activity_main_menu_screen);
        Ll.setBackgroundResource(R.drawable.backgroundmenu);


        photoImageView = (ImageView) findViewById(R.id.photoImageView);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        instructionsTextView = (TextView) findViewById(R.id.instructionsTextView);
        btnScanQRCode = (BootstrapButton) findViewById(R.id.btnScanQRCode);
        btnCerrarSesion = (BootstrapButton) findViewById(R.id.btnCerrarSesion);

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null)
                {
                    displayProfileInfo(currentProfile);
                }
            }
        };

        Bundle b = getIntent().getExtras();
        int value = -1; // or other values
        if (b != null) {
            value = b.getInt("key");
        }

        if (value == 1)
        {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            googleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }
        else if (value == 0)
        {
            if (AccessToken.getCurrentAccessToken() == null)
            {
                goToMainActitvityScreen();
            }
            else
            {
                requestEmail(AccessToken.getCurrentAccessToken());
                Profile profile = Profile.getCurrentProfile();
                if (profile != null)
                {
                    displayProfileInfo(profile);
                    facebookLogin = true;

                }
                else
                {
                    Profile.fetchProfileForCurrentAccessToken();
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST);
        }

        btnScanQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(MainMenuScreen.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Escaneá el código QR");
                integrator.setOrientationLocked(false);
                integrator.setCameraId(0);
                integrator.setBeepEnabled(true);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (googleApiClient != null && googleApiClient.isConnected())
                {
                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess())
                            {
                                goToMainActitvityScreen();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "No se ha podido cerrar sesión", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else if (AccessToken.getCurrentAccessToken() != null)
                {
                    LoginManager.getInstance().logOut();
                    goToMainActitvityScreen();
                }
                else
                {

                }
            }
        });
    }

    private void requestEmail(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                if (response.getError() != null)
                {
                    Toast.makeText(getApplicationContext(), response.getError().getErrorMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                /*try
                {
                    String email = object.getString("email");
                    setEmail(email);
                }
                catch (JSONException e)
                {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }*/
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, email, gender, birthday, location");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void setEmail(String email) {
        emailTextView.setText(email);
    }

    private void displayProfileInfo(Profile profile) {
        String id = profile.getId();
        String name = profile.getName();

        nameTextView.setText("¡Bienvenid@ " + name + "!");
        //idTextView.setText(id);

        SharedPreferences profilePreferences = getSharedPreferences("IDvalue", 0);
        SharedPreferences.Editor profileEditor = profilePreferences.edit();
        profileEditor.putString("profileName", name);
        profileEditor.putString("profileId", id);
        profileEditor.putString("redSocial", "facebook");

        String photoUrlFacebook = profile.getProfilePictureUri(100, 100).toString() != null ? profile.getProfilePictureUri(100, 100).toString() : "";

        if (photoUrlFacebook == "")
        {
            int drawableResourceId = this.getResources().getIdentifier("mobileuser", "drawable", this.getPackageName());
            photoImageView.setImageResource(drawableResourceId);
            //profileImage.setImageResource(drawableResourceId);
            profileEditor.putString("profilePicture", photoUrlFacebook);
            profileEditor.apply();
        }
        else
        {
            Glide.with(this).load(profile.getProfilePictureUri(400, 400).toString()).into(photoImageView);
            profileEditor.putString("profilePicture", profile.getProfilePictureUri(400, 400).toString());
            profileEditor.apply();

            //Glide.with(this).load(profile.getProfilePictureUri(400, 400).toString()).into(profileImage);
            //profileImage.setBorderDisplayed(true);
            //profileImage.setBootstrapSize(DefaultBootstrapSize.XL);
        }
    }

    private void goToMainActitvityScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        profileTracker.stopTracking();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(facebookLogin)
        {

        }
        else {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
            if (opr.isDone()) {
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {

            GoogleSignInAccount account = result.getSignInAccount();

            nameTextView.setText("¡Bienvenid@ " + account.getDisplayName() + "!");
            //emailTextView.setText(account.getEmail());
            //idTextView.setText(account.getId());

            SharedPreferences profilePreferences = getSharedPreferences("IDvalue", 0);
            SharedPreferences.Editor profileEditor = profilePreferences.edit();
            profileEditor.putString("profileName", account.getDisplayName());
            profileEditor.putString("profileId", account.getId());
            profileEditor.putString("redSocial", "google");

            String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "";

            if (photoUrl == "")
            {
                int drawableResourceId = this.getResources().getIdentifier("mobileuser", "drawable", this.getPackageName());
                photoImageView.setImageResource(drawableResourceId);
                //profileImage.setImageResource(drawableResourceId);
                profileEditor.putString("profilePicture", photoUrl);
                profileEditor.apply();
            }
            else
            {
                Glide.with(this).load(account.getPhotoUrl()).into(photoImageView);
                //Glide.with(this).load(account.getPhotoUrl()).into(profileImage);
                //profileImage.setBorderDisplayed(true);
                //profileImage.setBootstrapSize(DefaultBootstrapSize.XL);
                profileEditor.putString("profilePicture", account.getPhotoUrl().toString());
                profileEditor.apply();
            }
        }
        else
        {
            goToMainActitvityScreen();
        }
    }

    public void logOut(View view) {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    goToMainActitvityScreen();
                } else {
                    Toast.makeText(getApplicationContext(), "No se ha podido cerrar sesión", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void revoke(View view) {
        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    goToMainActitvityScreen();
                } else {
                    Toast.makeText(getApplicationContext(), "No se ha podido revocar los vínculos con la aplicación", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null)
        {
            if (result.getContents() == null)
            {
                Toast.makeText(this, "Has cancelado la operación de escanear", Toast.LENGTH_LONG).show();
            }
            else
            {
                String url = "http://www.partypicok.com/endpoints/Get_EventoByScan.php?codigo=" + result.getContents().toString();
                Log.d("ADebugTag", "Value: " + url);
                new ConsultarDatos().execute(url);
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class ConsultarDatos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try
            {
                return downloadUrl(urls[0]);
            }
            catch (IOException e)
            {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            String str1 = result;
            String str2 = "Macri Gato";

            if (str1.toLowerCase().contains(str2.toLowerCase()))
            {
                AlertDialog alertDialog = new AlertDialog.Builder(MainMenuScreen.this).create();
                alertDialog.setTitle("¡Ups!");
                alertDialog.setMessage("El código ingresado es inválido o el evento está inhabilitado. Intentá nuevamente.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            else
            {
                JSONArray ja = null;
                try
                {
                    ja = new JSONArray(result);
                    String nombre = ja.getString(0);

                    for (int i = 0; i < ja.length(); ++i)
                    {
                        JSONObject jsn = ja.getJSONObject(i);
                        nombre = jsn.getString("nombre_evento");
                    }

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainMenuScreen.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("usuario_logueado", "true");
                    editor.apply();

                    Intent eventActivity = new Intent(MainMenuScreen.this, EventActivity.class);

                    Bundle b = new Bundle();
                    b.putString("result", result);
                    eventActivity.putExtras(b);
                    MainMenuScreen.this.startActivity(eventActivity);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private String downloadUrl(String myurl) throws IOException {
        Log.i("URL", "" + myurl);
        myurl = myurl.replace(" ", "%20");
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("Respuesta", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    public void displayExceptionMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}