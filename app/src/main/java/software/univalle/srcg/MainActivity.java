package software.univalle.srcg;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    View root_view;
    Context context = this;
    TextView txt_documento,txt_nombre,txt_url;
    String documento_manual="0",server="http://127.0.0.1/srcg",pass="1234";
    ProgressDialog prgDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Comunicándose con el  servidor.\n" +
                "Por favor espera...");
        prgDialog.setCancelable(false);

        SharedPreferences preferences=getSharedPreferences("SRCG_DATA",Context.MODE_PRIVATE);
        String memoria =  preferences.getString("server","");
        if(memoria.isEmpty()||memoria == null){
            server="http://127.0.0.1/srcg";
        }else {
            server =memoria;
        }

        memoria =  preferences.getString("pass","");
        if(memoria.isEmpty()||memoria == null){
            pass="1234";
        }else {
            pass =memoria;
        }

        root_view =  (LinearLayout) findViewById(R.id.content_main);
        txt_documento = (TextView) findViewById(R.id.txt_documento);
        txt_nombre = (TextView) findViewById(R.id.txt_nombre);
        txt_url = (TextView) findViewById(R.id.txt_url);

        txt_url.setText(server);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt_documento.setText("");
                txt_nombre.setText("");
                txt_url.setText(server);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    boolean formato(String resultado){
        return resultado.contains(" -:- ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Snackbar.make(root_view, "Cancelado", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                String resultado = result.getContents();
                if(formato(resultado)){
                    String info[] = resultado.split(" -:- ", 2);
                    String documento = info[0];
                    String nombre = info[1];
                    txt_documento.setText(documento);
                    txt_nombre.setText(nombre);
                    txt_url.setText(server);
                }else{
                    txt_documento.setText("Formato desconocido");
                    txt_nombre.setText(resultado);
                    txt_url.setText(server);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void  search(String data) {
        Intent intent = null;
        String link = String.valueOf(txt_url.getText())+"";
        if(data.equals("")){
            intent = new Intent(MainActivity.this, ErrorActivity.class);
            intent.putExtra("MESSAGE", "Por favor ingrese el nombre del graduando.");
            startActivity(intent);
        }else if(!data.matches("[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð ,.'-]+")){
            intent = new Intent(MainActivity.this, ErrorActivity.class);
            intent.putExtra("MESSAGE", "Por favor ingrese unicamente letras para el nombre del graduando.");
            startActivity(intent);
        }else if(link.equals("")){
            intent = new Intent(MainActivity.this, ErrorActivity.class);
            intent.putExtra("MESSAGE", "Por favor ingrese el la direccion IP del servidor.");
            startActivity(intent);
        }
        else if(pass.equals("")){
            intent = new Intent(MainActivity.this, ErrorActivity.class);
            intent.putExtra("MESSAGE", "Por favor ingrese la contraseña.");
            startActivity(intent);
        }else{

            if (!link.contains("http://")) {
                link = "http://" + link;
            }


                link = link+"/search";

            send(link,data,1);

 }

    }



    public void  validate() {
        String data = String.valueOf(txt_documento.getText())+"";
        String link = String.valueOf(txt_url.getText())+"";
        Intent intent = null;
        if(data.equals("")){
            intent = new Intent(MainActivity.this, ErrorActivity.class);
            intent.putExtra("MESSAGE", "Por favor ingrese el numero de documento del invitado.");
            startActivity(intent);
        }else if(!data.matches("[0-9]+")){
            intent = new Intent(MainActivity.this, ErrorActivity.class);
            intent.putExtra("MESSAGE", "Por favor ingrese unicamente numeros para el documento del invitado.");
            startActivity(intent);
        }else if(link.equals("")){
            intent = new Intent(MainActivity.this, ErrorActivity.class);
            intent.putExtra("MESSAGE", "Por favor ingrese el la direccion IP del servidor.");
            startActivity(intent);
        }
        else if(pass.equals("")){
            intent = new Intent(MainActivity.this, ErrorActivity.class);
            intent.putExtra("MESSAGE", "Por favor ingrese la contraseña.");
            startActivity(intent);
        }else{


        if (!link.contains("http://")) {
            link = "http://" + link;
        }


            link = link+"/validate";

            send(link,data,0);
    }
    }

    public void send(String link, final String data, final int operacion) {

        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();

        params.put("data", data);
        params.put("password", pass);
        Snackbar.make(root_view,  link, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        client.post(link, params, new AsyncHttpResponseHandler() {

            public void onStart() {
                super.onStart();
                prgDialog.show();
            }

            public void onFinish() {
                super.onFinish();
                    prgDialog.hide();;
            }

            public void onFailure(int statusCode, Header[] headers, java.lang.Throwable throwable, org.json.JSONObject response) {
                Intent intent = new Intent(MainActivity.this, ExceptionActivity.class);
                intent.putExtra("MESSAGE", throwable.getMessage()+" "+statusCode);
                startActivity(intent);
            }


            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    Intent intent = null;
                   String result = new String(responseBody, "UTF-8");

                    switch (operacion){
                        case 0:
                    switch (result){

                        case "0":
                            intent = new Intent(MainActivity.this, ErrorActivity.class);
                            intent.putExtra("MESSAGE", "Contraseña incorrecta.");
                            startActivity(intent);
                            break;

                        case "1":
                            intent = new Intent(MainActivity.this, FailActivity.class);
                            intent.putExtra("MESSAGE", "El invitado \""+data+"\" no ha sido registrado.");
                            startActivity(intent);
                            break;

                        case "2":
                            intent = new Intent(MainActivity.this, AlertActivity.class);
                            intent.putExtra("MESSAGE", "El invitado \""+data+"\" ya ha sido registrado.");
                            startActivity(intent);
                            break;

                        case "3":
                            intent = new Intent(MainActivity.this, SuccessActivity.class);
                            intent.putExtra("MESSAGE", "El invitado \""+data+"\" ha sido registrado.");
                            startActivity(intent);
                            break;

                        default:
                            intent = new Intent(MainActivity.this, ExceptionActivity.class);
                            intent.putExtra("MESSAGE", "Respuesta inesperada del servidor");
                            startActivity(intent);
                            break;
                    }
                            break;
                        case 1:

                            switch (result){

                                case "0":
                                    intent = new Intent(MainActivity.this, ErrorActivity.class);
                                    intent.putExtra("MESSAGE", "Contraseña incorrecta.");
                                    startActivity(intent);
                                    break;

                                case "1":
                                    intent = new Intent(MainActivity.this, FailActivity.class);
                                    intent.putExtra("MESSAGE", "El graduando con el nombre \""+data+"\" no ha sido registrado.");
                                    startActivity(intent);
                                    break;

                                default:
                                    GsonBuilder builder = new GsonBuilder();
                                    Gson mGson = builder.create();
                                    intent = new Intent(MainActivity.this, SuccessActivity.class);
                                    intent.putExtra("MESSAGE", result);
                                    startActivity(intent);
                            }

                            break;

                        default:
                            intent = new Intent(MainActivity.this, ExceptionActivity.class);
                            intent.putExtra("MESSAGE", "Operacion indefinida");
                            startActivity(intent);
                            break;
                    }
                } catch (Exception e) {
                    Snackbar.make(root_view,  e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
            // When error occured
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Intent intent = null;
                if(statusCode == 404){
                    intent = new Intent(MainActivity.this, ExceptionActivity.class);
                    intent.putExtra("MESSAGE", "Pagina no encontrada");
                    startActivity(intent);
                }else if(statusCode == 403){
                    intent.putExtra("MESSAGE", "No tiene permiso para acceder");
                    startActivity(intent);
                }else if(statusCode == 500){
                    intent = new Intent(MainActivity.this, ExceptionActivity.class);
                    intent.putExtra("MESSAGE", "Hubo un error en el servidor");
                    startActivity(intent);
                }else{
                    intent = new Intent(MainActivity.this, ExceptionActivity.class);
                    intent.putExtra("MESSAGE", error.getMessage()+" "+statusCode);
                    startActivity(intent);
                }
            }
        });
        }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_scan) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("Escanee el codigo QR de la invitacion");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(false);
            integrator.setOrientationLocked(true);
            integrator.initiateScan();
        } else if (id == R.id.nav_id) {

            LayoutInflater factory = LayoutInflater.from(this);

            final View textEntryView = factory.inflate(R.layout.content_alert_id, null);

            final EditText input1 = (EditText) textEntryView.findViewById(R.id.txt_guest_id);


            input1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
            input1.setText(documento_manual);
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setIcon(R.drawable.ic_menu_id).setTitle("Documento del invitado").setView(textEntryView).setPositiveButton("Guardar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            documento_manual = input1.getText().toString();
                            txt_documento.setText(documento_manual);
                            txt_nombre.setText("No disponible");
                            txt_url.setText(server+"");

                        }
                    }).setNegativeButton("Cancelar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            dialog.cancel();
                        }
                    });
            alert.show();

        }else if (id == R.id.nav_graduate) {
            LayoutInflater factory = LayoutInflater.from(this);

            final View textEntryView = factory.inflate(R.layout.content_alert_graduate, null);

            final EditText input1 = (EditText) textEntryView.findViewById(R.id.txt_graduate_name);


            input1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);

            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setIcon(R.drawable.ic_menu_graduate).setTitle("Nombre del graduando").setView(textEntryView).setPositiveButton("Guardar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {

                            search(input1.getText().toString());

                        }
                    }).setNegativeButton("Cancelar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            dialog.cancel();
                        }
                    });
            alert.show();

        } else if (id == R.id.nav_configuration) {

            LayoutInflater factory = LayoutInflater.from(this);

            final View textEntryView = factory.inflate(R.layout.content_alert_configuration, null);

            final EditText input1 = (EditText) textEntryView.findViewById(R.id.txt_address);
            final EditText input2 = (EditText) textEntryView.findViewById(R.id.txt_pass);


            input1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
            input1.setText(server, TextView.BufferType.EDITABLE);
            input2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input2.setText(pass, TextView.BufferType.EDITABLE);

            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setIcon(R.drawable.ic_menu_configuration).setTitle("Configuración del servidor").setView(textEntryView).setPositiveButton("Guardar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {

                            server = input1.getText().toString();
                            SharedPreferences preferences=getSharedPreferences("SRCG_DATA",Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor=preferences.edit();
                            editor.putString("server", server);
                            editor.commit();
                            txt_url.setText(server+"");

                            pass = input2.getText().toString();
                            editor.putString("pass", pass);
                            editor.commit();

                        }
                    }).setNegativeButton("Cancelar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            dialog.cancel();
                        }
                    });
            alert.show();


        } else if (id == R.id.nav_sync) {

            Intent intent = null;
            String link = String.valueOf(txt_url.getText())+"";
            if(link.equals("")){
                intent = new Intent(MainActivity.this, ErrorActivity.class);
                intent.putExtra("MESSAGE", "Por favor ingrese el la direccion IP del servidor.");
                startActivity(intent);
            }
            else if(pass.equals("")){
                intent = new Intent(MainActivity.this, ErrorActivity.class);
                intent.putExtra("MESSAGE", "Por favor ingrese la contraseña.");
                startActivity(intent);
            }else{

                if (!link.contains("http://")) {
                    link = "http://" + link;
                }


                link = link+"/sync";
            intent = new Intent(MainActivity.this, ListActivity.class);
            intent.putExtra("LINK", link);
            intent.putExtra("PASS", pass);
            startActivity(intent);
            }

        }else if (id == R.id.nav_validate) {

            validate();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
