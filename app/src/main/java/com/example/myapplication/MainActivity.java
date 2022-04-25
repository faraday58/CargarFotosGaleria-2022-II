package com.example.myapplication;

import androidx.annotation.NonNull;
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
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    //Constantes que definen el nombre y la ruta de las fotos guardadas por la cámara
    private static final int CODIGO_FOTO =20;
    private static final int IMAGEN_SELECCIONADA = 12;
    private static final int REQUERIR_CODIOG_PERMISO =30 ;
    AppCompatButton btnAgregarImagen;
    AppCompatImageView imgvProducto;
    AppCompatButton btnAgregarProducto;
    EditText edtTitulo ;
    EditText edtDescripcion;
    String urlImagen="";
    private StorageReference mStorageRef;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAgregarImagen = findViewById(R.id.btnAgregarImagen);
        imgvProducto= findViewById(R.id.imgvProducto);
        btnAgregarProducto=findViewById(R.id.btnAgregarProducto);

        edtTitulo=findViewById(R.id.edtTitulo);
        edtDescripcion= findViewById(R.id.edtDescripcion);
        db= FirebaseFirestore.getInstance();
        mStorageRef= FirebaseStorage.getInstance("gs://tiendaapp-3a33b.appspot.com").getReference();
        btnAgregarImagen.setOnClickListener(onClickAgregaFoto);
        btnAgregarProducto.setOnClickListener(onClickAProducto);

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

        if( resultCode == RESULT_OK && data != null && data.getData() != null )
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
            Uri filepath= data.getData(); //Obteniendo la uri de la imagen seleccionada
            Log.d("Directorio","Obteniendo Uri");
            final StorageReference filepathr=mStorageRef.child("productos").child(filepath.getLastPathSegment());
            filepathr.putFile(filepath).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful())
                    {
                        throw new Exception();
                    }
                    return filepathr.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(), "Se subió correctamente la aplicación", Toast.LENGTH_SHORT).show();
                        Uri downloadlink= task.getResult();
                        urlImagen= downloadlink.toString();
                        Log.d("Link","Link de descarga: " +urlImagen);
                    }
                }
            });

        }

    }

    View.OnClickListener onClickAProducto = View ->{
        String titulo = edtTitulo.getText().toString();
        String descripcion= edtDescripcion.getText().toString();
        Producto miProducto= new Producto(titulo,urlImagen,descripcion);
        db.collection("Producto").document().set(miProducto).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
               edtDescripcion.setText("");
               edtTitulo.setText("");
               imgvProducto.setImageDrawable(getResources().getDrawable( R.drawable.tenis_tela));
                Toast.makeText(MainActivity.this, "Se agregaron los elementos de forma correcta", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Oops, algo salió mal", Toast.LENGTH_SHORT).show();
            }
        });




    };


}