package com.e.c.a.h.firebasecontactbook;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.e.c.a.h.firebasecontactbook.adapter.ContactArrayAdapter;
import com.e.c.a.h.firebasecontactbook.data.Contact;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "HACE:";
    FirebaseDatabase db;
    DatabaseReference dbRef;

    ListView listView;

    ContactArrayAdapter contacts;

    private static Contact lastDeletedContact;
    private static int indexLastDeletedContact;

    public static File folder;

    int permissionCheckReadExternalStorage = PackageManager.PERMISSION_DENIED;
    int permissionCheckWriteExternalStorage = PackageManager.PERMISSION_DENIED;

    private final int ADD_CONTACT_RC = 111;
    private final int ACCESS_EXTERNAL_STORAGE_PERMISSION_ID = 222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference("contact_book");

        contacts = new ContactArrayAdapter(this, R.layout.contact_list_view_item, new ArrayList<Contact>());

        listView = (ListView) findViewById(R.id.mainListView);
        listView.setAdapter(contacts);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToContactForm(contacts.getItem(position));
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final int pos = position;
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete this contact?")
                        .setMessage("Are you sure you want to delete this contact... this action can actually be undone ;)")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //dbRef.child(contacts.getItem(position).getUid()).removeValue();
                                Contact c = contacts.getItem(pos);
                                c.setDeleteDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                                indexLastDeletedContact = pos;
                                lastDeletedContact = c;
                                dbRef.child(c.getUid()).setValue(c);

                                Snackbar
                                        .make(findViewById(R.id.mainListView), "Contact deleted...", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Undo!", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                lastDeletedContact.setDeleteDate(null);
                                                dbRef.child(lastDeletedContact.getUid()).setValue(lastDeletedContact);
                                                Toast.makeText(MainActivity.this, "Ok... contact restored...", Toast.LENGTH_LONG).show();
                                            }
                                        }).show();
                            }
                        }).show();
                return true;
            }
        });

        folder = new File(Environment.getExternalStorageDirectory() + "/android/data/com.e.c.a.h.firebasecontactbook/pics");

        requestStoragePermissions();
        checkFolderExistence();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToContactForm(null);
            }
        });

        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Contact c = dataSnapshot.getValue(Contact.class);
                if(c.getDeleteDate() == null) {
                    contacts.add(c);
                    contacts.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Contact c = dataSnapshot.getValue(Contact.class);
                int index = contacts.getPosition(c);

                contacts.remove(c);

                if(c.getDeleteDate() == null) {
                    contacts.insert(c, index >= 0 ? index : indexLastDeletedContact);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(findViewById(R.id.mainListView), "An error has occurred when trying to perform de DB operation..." + databaseError.getMessage(), Snackbar.LENGTH_INDEFINITE).show();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
        });
    }

    private void requestStoragePermissions() {
        permissionCheckWriteExternalStorage = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionCheckReadExternalStorage = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

        if(permissionCheckWriteExternalStorage != PackageManager.PERMISSION_GRANTED
                || permissionCheckReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Please provide permission to access the external storage... I need to save the contacts photos", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this, new String []{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, ACCESS_EXTERNAL_STORAGE_PERMISSION_ID);
        }
    }

    private void checkFolderExistence() {
        if(!folder.exists()) {
            Log.d(LOG_TAG, "Folder does not exists:" + folder);
            if(!folder.mkdir()) {
                Snackbar.make(this.findViewById(R.id.mainListView), "Couldn't create or get app folder... app will not work", Snackbar.LENGTH_INDEFINITE).show();
                return;
            }
        }
        Log.d(LOG_TAG, "Folder ready... app can work as expected...");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_contact) {
            goToContactForm(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToContactForm(Contact c) {
        Intent i = new Intent(this, ContactActivity.class);

        i.putExtra(ContactActivity.EDIT_CONTACT, (Parcelable) c);

        startActivityForResult(i, ADD_CONTACT_RC);
    }

    @Override
    protected void onPostResume() {
        Log.d(LOG_TAG, "onPostResume()");
        super.onPostResume();
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.d(LOG_TAG, "onRestart()");
        super.onRestart();
    }
}
