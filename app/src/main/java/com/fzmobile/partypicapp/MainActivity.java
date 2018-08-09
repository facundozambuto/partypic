package com.fzmobile.partypicapp;


import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;


import org.json.JSONException;

import java.io.IOException;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.R.attr.data;


public class MainActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private ImageView imageViewMarca;
    private ImageView imageViewLogo;

    private GoogleApiClient googleApiClient;
    private SignInButton signInButton;
    public static final int SIGN_IN_CODE = 777;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.activity_main);

        rl.setBackgroundResource(R.drawable.backgroundlogin);
        imageViewMarca = (ImageView) findViewById(R.id.imageViewMarca);
        imageViewMarca.setImageResource(R.drawable.marcapartypic);
        imageViewLogo = (ImageView) findViewById(R.id.imageViewLogo);
        imageViewLogo.setImageResource(R.drawable.logotiponegroapp);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton = (SignInButton) findViewById(R.id.signInButton);

        signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setColorScheme(SignInButton.COLOR_LIGHT);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, SIGN_IN_CODE);
            }
        });


        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult) {
                goToMainMenuScreen(0);
            }

            @Override
            public void onCancel()
            {
                Toast.makeText(getApplicationContext(), "Se canceló el inicio de Sesión", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error)
            {
                Toast.makeText(getApplicationContext(), "Error al iniciar sesión con Facebook", Toast.LENGTH_SHORT).show();
            }
        });


        if(AccessToken.getCurrentAccessToken() != null)
        {
            goToMainMenuScreen(0);
        }
        else
        {
            if(googleApiClient != null && googleApiClient.isConnected())
            {
                goToMainMenuScreen(1);
            }
            else
            {

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_CODE)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else
        {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result)
    {
        if (result.isSuccess())
        {
            goToMainMenuScreen(1);
        }
        else
        {
            Toast.makeText(this, "error: "+ result.getStatus(), Toast.LENGTH_SHORT).show();
        }
    }

    private void goToMainMenuScreen(int red_social)
    {
        Intent intent = new Intent(this, MainMenuScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle b = new Bundle();
        b.putInt("key", red_social);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Toast.makeText(this, "Error al iniciar sesión con Google o Facebook", Toast.LENGTH_SHORT).show();
    }

}
