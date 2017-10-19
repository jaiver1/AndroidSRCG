package software.univalle.srcg;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    View root_view;
    String link="http://127.0.0.1/srcg",pass="1234";
    DBController controller = new DBController(this);
    // Progress Dialog Object
    ProgressDialog prgDialog;
    HashMap<String, String> queryValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try{
            prgDialog = new ProgressDialog(this);
            prgDialog.setMessage("Comunicándose con el  servidor.\n" +
                    "Por favor espera...");
            prgDialog.setCancelable(false);
            root_view =  (RelativeLayout) findViewById(R.id.content_list);

            link = getIntent().getStringExtra("LINK");
            pass = getIntent().getStringExtra("PASS");
            List<Item> items = new ArrayList<Item>();
            ArrayList<HashMap<String, String>> graduatesList = controller.getAllGraduates();
            for(int i= 0; i < graduatesList.size(); i++ ){

                HashMap graduate = graduatesList.get(i);
            items.add(new ListHeader(graduate.get("codigo")+") "
                    +graduate.get("nombre").toString()+" "+graduate.get("apellido").toString()+"\n"
                    +graduate.get("programa").toString()));

                ArrayList<HashMap<String, String>> guestList = controller.searchGuests(graduate.get("codigo").toString());

                for(int j= 0; j < guestList.size(); j++ ){
                    HashMap guest = guestList.get(j);
                    items.add(new ListItem(guest.get("documento").toString(),
                            guest.get("nombre").toString()+" "+guest.get("apellido").toString()));
                }
            }

           load(items);


        } catch (Exception e) {
            Snackbar.make(root_view,  e.getMessage(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sync();
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

    public void load(List<Item> list) {
        TwoTextArrayAdapter adapter = new TwoTextArrayAdapter(this, list);
        ListView myList = (ListView) findViewById(android.R.id.list);
        myList.setAdapter(adapter);
    }

    public void sync() {

        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();

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
                Intent intent = new Intent(ListActivity.this, ExceptionActivity.class);
                intent.putExtra("MESSAGE", throwable.getMessage()+" "+statusCode);
                startActivity(intent);
            }


            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {

                    String result = new String(responseBody, "UTF-8");

                    switch (result){

                        case "0":
                            Intent intent = new Intent(ListActivity.this, ErrorActivity.class);
                            intent.putExtra("MESSAGE", "Contraseña incorrecta.");
                            startActivity(intent);
                            break;

                        case "1":
                            intent = new Intent(ListActivity.this, FailActivity.class);
                            intent.putExtra("MESSAGE", "No hay datos.");
                            startActivity(intent);
                            break;

                        default:
                            updateSQLite(result);
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
                    intent = new Intent(ListActivity.this, ExceptionActivity.class);
                    intent.putExtra("MESSAGE", "Pagina no encontrada");
                    startActivity(intent);
                }else if(statusCode == 403){
                    intent.putExtra("MESSAGE", "No tiene permiso para acceder");
                    startActivity(intent);
                }else if(statusCode == 500){
                    intent = new Intent(ListActivity.this, ExceptionActivity.class);
                    intent.putExtra("MESSAGE", "Hubo un error en el servidor");
                    startActivity(intent);
                }else{
                    intent = new Intent(ListActivity.this, ExceptionActivity.class);
                    intent.putExtra("MESSAGE", error.getMessage()+" "+statusCode);
                    startActivity(intent);
                }
            }
        });
    }

    public void updateSQLite(String response){


        try {
            Snackbar.make(root_view,  response, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

    Gson gson = new GsonBuilder().create();
    // Extract JSON array from the response
    JSONArray arr = new JSONArray(response);
    controller.update();
    // If no of array elements is not zero
    if(arr.length() != 0){
        // Loop through each array element, get JSON object which has userid and username
        for (int i = 0; i < arr.length(); i++) {
            // Get JSON object
            JSONArray data = new JSONArray(arr.get(i).toString());
            JSONObject graduate = (JSONObject) data.get(0);
            JSONArray guests = new JSONArray(data.get(1).toString());

            Toast.makeText(getBaseContext(), graduate.get("codigo").toString(),
                    Toast.LENGTH_LONG).show();

            queryValues = new HashMap<String, String>();
            queryValues.put("codigo", graduate.get("codigo").toString());
            queryValues.put("nombre", graduate.get("nombre").toString());
            queryValues.put("apellido", graduate.get("apellido").toString());
            queryValues.put("programa", graduate.get("programa").toString());
            controller.insertGraduate(queryValues);

            for (int j = 0; j < guests.length(); j++) {
                JSONObject guest = (JSONObject) guests.get(j);
            queryValues = new HashMap<String, String>();

            queryValues.put("documento", guest.get("documento").toString());
            queryValues.put("graduando", graduate.get("codigo").toString());
            queryValues.put("nombre", guest.get("nombre").toString());
            queryValues.put("apellido", guest.get("apellido").toString());
            queryValues.put("asistencia", guest.get("asistencia").toString());
            // Insert User into SQLite DB
            controller.insertGuest(queryValues);

        }
        }
        reloadActivity();
    }
} catch (Exception e) {
            Snackbar.make(root_view,  e.getMessage(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
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
        getMenuInflater().inflate(R.menu.list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_guest) {
            LayoutInflater factory = LayoutInflater.from(this);

            final View textEntryView = factory.inflate(R.layout.content_alert_graduate, null);

            final EditText input1 = (EditText) textEntryView.findViewById(R.id.txt_graduate_name);


            input1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);

            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setIcon(R.drawable.ic_menu_graduate).setTitle("Nombre del invitado").setView(textEntryView).setPositiveButton("Guardar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {



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



                        }
                    }).setNegativeButton("Cancelar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            dialog.cancel();
                        }
                    });
            alert.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void reloadActivity() {
        finish();
        startActivity(getIntent());
    }
}
