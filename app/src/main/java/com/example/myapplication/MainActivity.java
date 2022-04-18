package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    //Constantes que definen el nombre y la ruta de las fotos guardadas por la cámara

    private static final int CODIGO_FOTO =20;
    private static final int IMAGEN_SELECCIONADA = 12;
    private static final int REQUERIR_CODIOG_PERMISO =30 ;
    AppCompatButton btnAgregarImagen;
    AppCompatImageView imgvProducto;
    private String mRuta;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAgregarImagen = findViewById(R.id.btnAgregarImagen);
        imgvProducto= findViewById(R.id.imgvProducto);
        btnAgregarImagen.setOnClickListener(onClickAgregaFoto);
    }

    View.OnClickListener onClickAgregaFoto  = view -> {
        //Crea un arreglo de caracteres que es mucho más eficiente que un arreglo de cadenas
        final CharSequence[] opcion ={"Cámara","Elegir de la Galería","Cancelar"};
        //Creando un Cuadro de diálogo personalizado
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Elige una opción");
        builder.setItems(opcion, (dialogo, indice) -> {
            if(opcion[indice]==  "Cámara" )
            {
                AbrirCamara();
            }
            else if (opcion[indice] == "Elegir de la Galería"){
               Intent intentGaleria= new Intent();
               intentGaleria.setType("image/*");
               intentGaleria.setAction(Intent.ACTION_GET_CONTENT);
               //Si se envía esta selección, la foto será obtenida de la galería y se identificará con PICK_FOTO
               startActivityForResult(Intent.createChooser(intentGaleria,"Selecciona una imagen"),IMAGEN_SELECCIONADA);
            }
            else
            {
                dialogo.dismiss();
            }
        });
        builder.show();
    };

    private void AbrirCamara() {
        Log.i("Cámara","Entramos a la rutina para abrir la cámara");
        pedirPermisos();

            Intent intent_archivo= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //Si se envía esta selección, la foto será obtenida de la galería y se identificará con CODIGO_FOTO
            if (intent_archivo.resolveActivity(getPackageManager() ) != null )
            {
                Log.i("Dialogo","debería aparecer la aplicación de cámara");
                startActivityForResult(intent_archivo,CODIGO_FOTO);
            }
        }


    private void pedirPermisos() {
        int permisoCamara= ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if( permisoCamara != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.CAMERA},REQUERIR_CODIOG_PERMISO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case CODIGO_FOTO:
                    Bundle extras = data.getExtras();
                    Bitmap imagenBitmap=(Bitmap) extras.get("data");
                    imgvProducto.setImageBitmap(imagenBitmap);
                    break;

                case IMAGEN_SELECCIONADA:
                    Uri directorio= data.getData();
                    imgvProducto.setImageURI(directorio);
                    break;
            }
        }

    }
}