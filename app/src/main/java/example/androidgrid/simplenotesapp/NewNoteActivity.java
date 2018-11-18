package example.androidgrid.simplenotesapp;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class NewNoteActivity extends AppCompatActivity {

    private Button btnCreate;
    private EditText etTitle, etContent, etEmailFriends;
    private LinearLayout linearFriends;
    private String friKey;
    private FirebaseAuth fAuth;
    private DatabaseReference fNotesDatabase;
    private DatabaseReference firebaseDatabase;
    private DatabaseReference notesDatabase;
    private Button btnFriendsAdd;
    private String noteID;
    String arkadasMail;
    private boolean isExist;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.new_note_menu, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        try {
            noteID = getIntent().getStringExtra("noteId");

            //Toast.makeText(this, noteID, Toast.LENGTH_SHORT).show();

            if (!noteID.trim().equals("")) {
                isExist = true;
            } else {
                isExist = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        linearFriends = findViewById(R.id.linearFriends);
        etEmailFriends = findViewById(R.id.etEmailFriends);
        btnCreate = findViewById(R.id.new_note_btn);
        etTitle = findViewById(R.id.new_note_title);
        etContent = findViewById(R.id.new_note_content);
        btnFriendsAdd = findViewById(R.id.btnFriendsAdd);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        fAuth = FirebaseAuth.getInstance();
        fNotesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());

        firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String title = etTitle.getText().toString().trim();
                String content = etContent.getText().toString().trim();

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)) {
                    createNote(title, content);
                } else {
                    Snackbar.make(view, "Boş Alanları Doldurunuz", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        btnFriendsAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                arkadasDeneme(1);

            }

        });

        putData();
    }

    private void arkadasDeneme(int code) {
        Map updateMap = new HashMap();
        final boolean[] de = {false};
        if (code == 1) {
            updateMap.put("email", etEmailFriends.getText().toString().trim());
        }

        firebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    for (DataSnapshot datas : data.getChildren()) {
                        arkadasMail = datas.child("mail").getValue().toString();
                        if (arkadasMail.equals(etEmailFriends.getText().toString())) {
                            friKey = data.getKey();
                            Toast.makeText(NewNoteActivity.this, "Kullanıcı Bulundu", Toast.LENGTH_SHORT).show();
                            de[0] = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (de[0]){

            notesDatabase.child(friKey).updateChildren(updateMap);
        }
    }

    private void putData() {
        arkadasDeneme(0);

        if (isExist) {
            fNotesDatabase.child(noteID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("content")) {
                        String title = dataSnapshot.child("title").getValue().toString();
                        String content = dataSnapshot.child("content").getValue().toString();
                        etTitle.setText(title);
                        etContent.setText(content);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        if (friKey != null) {
            notesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(friKey);
            notesDatabase.child(friKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("content")) {
                        String title = dataSnapshot.child("title").getValue().toString();
                        String content = dataSnapshot.child("content").getValue().toString();
                        etTitle.setText(title);
                        etContent.setText(content);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void createNote(String title, String content) {
        if (fAuth.getCurrentUser() != null) {

            if (isExist) {
                // UPDATE A NOTE
                Map updateMap = new HashMap();
                updateMap.put("title", etTitle.getText().toString().trim());
                updateMap.put("content", etContent.getText().toString().trim());
                updateMap.put("timestamp", ServerValue.TIMESTAMP);
                updateMap.put("email", arkadasMail);
                fNotesDatabase.child(noteID).updateChildren(updateMap);

                Toast.makeText(this, "Not Güncellendi", Toast.LENGTH_SHORT).show();
            } else {
                // CREATE A NEW NOTE
                final DatabaseReference newNoteRef = fNotesDatabase.push();

                final Map noteMap = new HashMap();
                noteMap.put("title", title);
                noteMap.put("content", content);
                noteMap.put("email", arkadasMail);
                noteMap.put("timestamp", ServerValue.TIMESTAMP);

                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        newNoteRef.setValue(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(NewNoteActivity.this, "Not Veri Tabanına Eklendi", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(NewNoteActivity.this, "Hata: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                });
                mainThread.start();
            }


        } else {
            Toast.makeText(this, "Kullanıcı Girişi Başarısız", Toast.LENGTH_SHORT).show();
        }
        arkadasDeneme(0);
        if (friKey != null) {
            if (fAuth.getCurrentUser() != null) {

                if (isExist) {
                    // UPDATE A NOTE
                    Map updateMap = new HashMap();
                    updateMap.put("title", etTitle.getText().toString().trim());
                    updateMap.put("content", etContent.getText().toString().trim());
                    updateMap.put("email", fAuth.getCurrentUser().getEmail().toString());
                    updateMap.put("timestamp", ServerValue.TIMESTAMP);
                    notesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(friKey);
                    notesDatabase.child(friKey).updateChildren(updateMap);

                    Toast.makeText(this, "Not Güncellendi", Toast.LENGTH_SHORT).show();
                } else {
                    // CREATE A NEW NOTE
                    final DatabaseReference newNoteRef = notesDatabase.push();

                    final Map noteMap = new HashMap();
                    noteMap.put("title", title);
                    noteMap.put("content", content);
                    noteMap.put("email", fAuth.getCurrentUser().getEmail().toString());
                    noteMap.put("timestamp", ServerValue.TIMESTAMP);

                    Thread mainThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            newNoteRef.setValue(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(NewNoteActivity.this, "Not Veri Tabanına Eklendi", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(NewNoteActivity.this, "Hata: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                    });
                    mainThread.start();
                }


            } else {
                Toast.makeText(this, "Kullanıcı Girişi Başarısız", Toast.LENGTH_SHORT).show();
            }
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.new_note_delete_btn:
                if (isExist) {
                    deleteNote();
                } else {
                    Toast.makeText(this, "Silinecek Not Yok", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.account_creat:
                linearFriends.setVisibility(View.VISIBLE);

                break;


        }

        return true;
    }

    private void deleteNote() {

        fNotesDatabase.child(noteID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(NewNoteActivity.this, "Not Silindi", Toast.LENGTH_SHORT).show();
                    noteID = "no";
                    finish();
                } else {
                    Log.e("NewNoteActivity", task.getException().toString());
                    Toast.makeText(NewNoteActivity.this, "Hata: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        if (friKey != null) {
            notesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(friKey);
            notesDatabase.child(friKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(NewNoteActivity.this, "Not Silindi", Toast.LENGTH_SHORT).show();
                        friKey = null;
                        finish();
                    } else {
                        Log.e("NewNoteActivity", task.getException().toString());
                        Toast.makeText(NewNoteActivity.this, "Hata: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }

}
