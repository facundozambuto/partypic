package com.fzmobile.partypicapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.SSLCertificateSocketFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapCircleThumbnail;
import com.beardedhen.androidbootstrap.BootstrapThumbnail;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapSize;
import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.FacebookSdkNotInitializedException;
import com.facebook.login.LoginManager;
import com.facebook.share.widget.LikeView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.EditText;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import java.io.BufferedWriter;
import java.net.URI;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.io.BufferedReader;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.util.Base64;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.provider.MediaStore.AUTHORITY;

public class EventActivity extends FragmentActivity implements SingleUploadBroadcastReceiver.Delegate
{
    private Button btnTakePicture;
    private TextView textViewBienvenida;
    private ImageView photoImageViewEvent;
    private LinearLayout layoutImageView;
    private LinearLayout layoutMensaje;
    private LinearLayout layoutSubirFotografia;

    //private BootstrapCircleThumbnail photoImageViewEvent;
    //ImageView imageView;
    private BootstrapThumbnail imageView;
    private TextView nombrePerfil;

    private BootstrapButton btnSalirEvento;
    private BootstrapButton btnTomarFotografia;
    private BootstrapButton btnSubirFotografia;

    BootstrapButton CaptureImageFromCamera,UploadImageToServer;
    ImageView ImageViewHolder;
    EditText imageName;
    ProgressDialog progressDialog;
    Intent intent;
    public  static final int RequestPermissionCode  = 1 ;
    Bitmap bitmap;
    boolean check = true;
    String GetImageNameFromEditText;
    String ImageNameFieldOnServer = "image_name" ;
    String IdEvento = "id_evento";
    String ProfileId = "profile_id" ;
    String Comentario = "comentario";
    String ImagenPerfil = "profile_pic" ;
    String NombrePerfil = "profile_name" ;
    String ImagePathFieldOnServer = "image" ;
    String ImageUploadPathOnSever ="http://www.partypicaok.com/endpoints/Subir_Imagen.php" ;

    //storage permission code
    private static final int STORAGE_PERMISSION_CODE = 123;

    //Uri to store the image uri
    private Uri filePath;

    private Uri fileUri;

    //Image request code
    private int CAPTURE_IMAGE_REQUEST  = 7;
    String mCurrentPhotoPath;

    private final SingleUploadBroadcastReceiver uploadReceiver =
            new SingleUploadBroadcastReceiver();

    @Override
    protected void onResume() {
        super.onResume();
        uploadReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uploadReceiver.unregister(this);
    }

    @Override
    public void onProgress(int progress) {
        progressDialog = ProgressDialog.show(EventActivity.this,"Subiendo imagen...","Un momento...",false,false);
    }

    @Override
    public void onProgress(long uploadedBytes, long totalBytes) {
        //your implementation
    }

    @Override
    public void onError(Exception exception) {
        //your implementation
    }

    @Override
    public void onCompleted(int serverResponseCode, byte[] serverResponseBody) {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder = new AlertDialog.Builder(EventActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else
        {
            builder = new AlertDialog.Builder(EventActivity.this);
        }
        builder.setTitle("¡Imagen subida!")
                .setMessage("¡Tu imagen ha sido subida con éxito! Pronto vas a verla proyectada. \n" +
                            "¡Continuá subiendo más imágenes! ")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.dismiss();
                        ImageViewHolder.setImageResource(0);
                        imageName.setText("");
                    }
                })
                .setIcon(android.R.drawable.ic_menu_upload)
                .show();
        progressDialog.dismiss();
        ImageViewHolder.setImageResource(0);
        imageName.setText("");
        layoutImageView.setVisibility(View.GONE);
        layoutMensaje.setVisibility(View.GONE);
        layoutSubirFotografia.setVisibility(View.GONE);
        btnTomarFotografia.setText("Tomar Fotografía");
    }

    @Override
    public void onCancelled() {
        //your implementation
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        SharedPreferences mPrefs = getSharedPreferences("IDvalue", 0);
        String nombreDePerfil = mPrefs.getString("profileName", "");
        String fotoDePerfil = mPrefs.getString("profilePicture", "");
        String idPerfil = mPrefs.getString("profileId", "");

        NombrePerfil = nombreDePerfil;
        ImagenPerfil = fotoDePerfil;
        ProfileId = idPerfil;

        btnSalirEvento = (BootstrapButton) findViewById(R.id.btnSalirEvento);
        btnTomarFotografia = (BootstrapButton) findViewById(R.id.btnTomarFotografia);
        btnSubirFotografia = (BootstrapButton) findViewById(R.id.btnSubirFotografia);
        layoutImageView = (LinearLayout) findViewById(R.id.layoutImageView);
        layoutMensaje = (LinearLayout) findViewById(R.id.layoutMensaje);
        layoutSubirFotografia = (LinearLayout) findViewById(R.id.layoutSubirFotografia);

        nombrePerfil = (TextView) findViewById(R.id.nombrePerfil);

        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        ScrollView rl = (ScrollView)findViewById(R.id.eventActivityMainLayout);
        rl.setBackgroundResource(R.drawable.backgroundmenu);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);

        tintManager.setStatusBarTintEnabled(true);

        Bundle bundle = getIntent().getExtras();
        String result = bundle.getString("result");

        JSONArray ja = null;
        try
        {
            ja = new JSONArray(result);
            String nombre = ja.getString(0);
            String id_evento = ja.getString(0);

            for (int i = 0; i < ja.length(); ++i)
            {
                JSONObject jsn = ja.getJSONObject(i);
                nombre = jsn.getString("nombre_evento");
                id_evento = jsn.getString("id_evento");
            }

            photoImageViewEvent = (ImageView) findViewById(R.id.photoImageViewEvent);
//            photoImageViewEvent = (BootstrapCircleThumbnail) findViewById(R.id.photoImageViewEvent);
//            photoImageViewEvent.setBorderDisplayed(false);

            String photoUrl = fotoDePerfil != null || fotoDePerfil != "" ? fotoDePerfil.toString() : "";

            if (photoUrl == "")
            {
                int drawableResourceId = this.getResources().getIdentifier("mobileuser", "drawable", this.getPackageName());
                photoImageViewEvent.setImageResource(drawableResourceId);
            }
            else
            {
                Glide.with(this).load(fotoDePerfil).into(photoImageViewEvent);

//                Glide.with(this).load(fotoDePerfil).into(photoImageViewEvent);
//                photoImageViewEvent.setBorderDisplayed(true);
//                photoImageViewEvent.setBootstrapSize(DefaultBootstrapSize.XL);
            }

            textViewBienvenida = (TextView) findViewById(R.id.textViewBienvenida);
            textViewBienvenida.setText(nombre);
            nombrePerfil.setText(nombreDePerfil);
            IdEvento = id_evento;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        CaptureImageFromCamera = (BootstrapButton)findViewById(R.id.btnTomarFotografia);
        //ImageViewHolder = (ImageView)findViewById(R.id.imageView);
        ImageViewHolder = (BootstrapThumbnail)findViewById(R.id.imageView);
        UploadImageToServer = (BootstrapButton) findViewById(R.id.btnSubirFotografia);
        imageName = (EditText)findViewById(R.id.editText);

        requestStoragePermission();

        CaptureImageFromCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
            intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;
            try
            {
                photoFile = createImageFile();
            }
            catch (IOException ex)
            {
                // Error occurred while creating the File
                Log.d("mylog", "Exception while creating file: " + ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null)
            {
                Log.d("mylog", "Photofile not null");
                Uri photoURI = FileProvider.getUriForFile(EventActivity.this,"com.fzmobile.partypicapp.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
            }
            }
        });

        UploadImageToServer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
            GetImageNameFromEditText = imageName.getText().toString();
            uploadMultipart();
            }
        });

        btnSalirEvento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salirEvento();
            }
        });

    }

    private void salirEvento() {
        this.finish();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_REQUEST  && resultCode == RESULT_OK)
        {
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());
            try
            {
                InputStream ims = new FileInputStream(file);

                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mCurrentPhotoPath, bounds);

                BitmapFactory.Options opts = new BitmapFactory.Options();
                Bitmap bm = BitmapFactory.decodeFile(mCurrentPhotoPath, opts);
                ExifInterface exif = null;

                try
                {
                    exif = new ExifInterface(mCurrentPhotoPath);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;

                int rotationAngle = 0;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

                Matrix matrix = new Matrix();
                matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);

                ImageViewHolder.setImageBitmap(rotatedBitmap);
                Uri tempUri = getImageUri(getApplicationContext(), rotatedBitmap);
                filePath = tempUri;
                layoutImageView.setVisibility(View.VISIBLE);
                layoutMensaje.setVisibility(View.VISIBLE);
                layoutSubirFotografia.setVisibility(View.VISIBLE);
                btnTomarFotografia.setText("Eliminar fotografía actual y tomar otra");
            }
            catch (FileNotFoundException e)
            {
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE)
        {
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Displaying a toast
                Toast.makeText(this, "Permiso concedido para escribir en almacenamiento interno", Toast.LENGTH_LONG).show();
            }
            else
            {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "¡Oops! Denegaste el permiso para escribir en almacenamiento interno. Intentalo nuevamente.", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void uploadMultipart()
    {
        String comentario = imageName.getText().toString().trim();
        try
        {
            String uploadId = UUID.randomUUID().toString();
            uploadReceiver.setDelegate(this);
            uploadReceiver.setUploadID(uploadId);

            UploadNotificationConfig notificationConfig = new UploadNotificationConfig();
            notificationConfig.setCompletedMessage("¡Tu imagen ha sido subida con éxito! Pronto vas a verla proyectada...");
            notificationConfig.setErrorMessage("¡Ups! Ocurrió un error al intentar subir tu imagen. Reportalo al desarrollador.");
            notificationConfig.setInProgressMessage("Tu imagen se está subiendo al servidor, " + NombrePerfil);
            notificationConfig.setRingToneEnabled(true);
            notificationConfig.setTitle("Subiendo imagen");
            notificationConfig.setIcon(R.mipmap.icon_logo);

            new MultipartUploadRequest(this, uploadId, "http://www.partypicok.com/endpoints/Subir_Imagen.php")
                    .addFileToUpload(getRealPathFromURI(filePath), "image") //Adding file
                    .addParameter("comentario", comentario)
                    .addParameter("id_evento", IdEvento)
                    .addParameter("profile_name", NombrePerfil)
                    .addParameter("profile_pic", ImagenPerfil)
                    .addParameter("profile_id", ProfileId)
                    .setUtf8Charset()
                    .setNotificationConfig(notificationConfig)
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload
        }
        catch (Exception exc)
        {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //method to get the file path from uri
    public String getPath(Uri uri)
    {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    //Requesting permission
    private void requestStoragePermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
        {

        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri)
    {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("mylog", "Path: " + mCurrentPhotoPath);
        return image;
    }


}
