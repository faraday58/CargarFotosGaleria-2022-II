package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    //Constantes que definen el nombre y la ruta de las fotos guardadas por la cámara
    private static final String DIRECTORIO_APP = "MisImagenesApp";
    private static final String DIRECTORIO_MEDIOS = DIRECTORIO_APP +"ImagenesApp";
    private static final int CODIGO_FOTO =20;
    private static final int IMAGEN_SELECCIONADA = 12;
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
        File archivo = new File(Environment.getExternalStorageDirectory(),DIRECTORIO_MEDIOS);
        boolean DirectorioCreado = archivo.exists();
        if( !DirectorioCreado)
        {
            DirectorioCreado= archivo.mkdirs();
        }
        else
        {
            //Creando archivo con nombre y fecha de creación
            Long timeStamp= System.currentTimeMillis()/1000;
            String nombreImagen = timeStamp.toString() +".jpg";
            mRuta = Environment.getExternalStorageDirectory() + File.separator + DIRECTORIO_MEDIOS
                    + File.separator + nombreImagen;
            File ArchivoNuevo= new File(mRuta);
            Intent intent_archivo= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent_archivo.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(ArchivoNuevo));
            //Si se envía esta selección, la foto será obtenida de la galería y se identificará con CODIGO_FOTO
            startActivityForResult(intent_archivo,CODIGO_FOTO);

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
                    MediaScannerConnection.scanFile(this, new String[]{mRuta}, null, (directorio, uri) -> {
                        Log.i("External Storage","Scanned " + directorio + ":");
                        Log.i("External Storage","-> Uri=" +uri);
                    });
                    Bitmap bitmap= BitmapFactory.decodeFile(mRuta);
                    imgvProducto.setImageBitmap(bitmap);
                    break;
            }
        }

    }
}