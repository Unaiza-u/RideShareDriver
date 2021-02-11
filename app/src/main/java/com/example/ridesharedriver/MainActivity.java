package com.example.ridesharedriver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ridesharedriver.Common.Common;
import com.example.ridesharedriver.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private Button btnSignIn, btnRegister;
    private RelativeLayout rootLayout;

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private FirebaseUser currentUser;

    //forget password
    TextView txt_forget_pwd;

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().
                setDefaultFontPath("fonts/Arkhip_font.ttf").
                setFontAttrId(R.attr.fontPath).
                build());
        setContentView(R.layout.activity_main);

        //init paperDb
        Paper.init(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_driver_tbl);

        btnRegister = findViewById(R.id.btnRegister);
        btnSignIn = findViewById(R.id.btnSignIn);
        rootLayout = findViewById(R.id.rootLayout);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });

        txt_forget_pwd = findViewById(R.id.txt_forget_password);
        txt_forget_pwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showDialogForgetPwd();
                return false;
            }
        });

        //Auto login system
        String user = Paper.book().read(Common.user_field);
        String pwd = Paper.book().read(Common.pwd_field);

        if(user != null && pwd != null){
            if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pwd)){
                autoLogin(user,pwd);
            }
        }

//        if(Common.exitUser) {
//
//            if (getIntent() != null) {
//                String exit_user = getIntent().getStringExtra("exit_user");
//                if (!exit_user.isEmpty()) {
//                    FirebaseDatabase.getInstance().getReference(Common.driver_tbl)
//                            .child(exit_user).removeValue();
//
//                    Common.exitUser = false;
//                }
//
//
//            }
//        }

    }

    private void autoLogin(final String user, final String pwd) {

        final android.app.AlertDialog waitingDialog= new SpotsDialog.Builder()
                .setMessage("Please Wait for SIGN IN")
                .setCancelable(false)
                .setContext(MainActivity.this)
                .build();
        waitingDialog.show();

        auth.signInWithEmailAndPassword(user, pwd)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        waitingDialog.dismiss();
                        FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Common.currentUser = dataSnapshot.getValue(User.class);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                if (!pwd.equals(user.getPassword())){
                                    updateProfile(pwd);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        //write values in paper
                        Paper.book().write(Common.user_field, user);
                        Paper.book().write(Common.pwd_field, pwd);

                        startActivity(new Intent(MainActivity.this, PermissionActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(rootLayout, "Failed "+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        btnSignIn.setEnabled(true);
                    }
                });
    }

    private void showDialogForgetPwd() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("FORGET PASSWORD");
        alertDialog.setMessage("Please enter your email address");

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View forget_pwd_layout = inflater.inflate(R.layout.layout_forget_pwd, null);

        final MaterialEditText editEmail = forget_pwd_layout.findViewById(R.id.edtEmail);
        alertDialog.setView(forget_pwd_layout);

        //setButton
        alertDialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int which) {

                dialogInterface.dismiss();

                if(TextUtils.isEmpty(editEmail.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                final android.app.AlertDialog waitingDialog= new SpotsDialog.Builder()
                        .setMessage("Please Wait for Password Reset")
                        .setCancelable(false)
                        .setContext(MainActivity.this)
                        .build();
                waitingDialog.show();

                auth.sendPasswordResetEmail(editEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();

                                Snackbar.make(rootLayout, "Reset Password link has been sent", Snackbar.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialogInterface.dismiss();
                        waitingDialog.dismiss();

                        Snackbar.make(rootLayout, ""+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void showLoginDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Sign In");
        dialog.setMessage("Please use email to sign in");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login, null);

        final MaterialEditText editEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText editPassword = login_layout.findViewById(R.id.edtPassword);


        dialog.setView(login_layout);

        dialog.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                btnSignIn.setEnabled(false);

                if(TextUtils.isEmpty(editEmail.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(editPassword.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(editPassword.getText().toString().length() < 6){
                    Snackbar.make(rootLayout, "Password too short !!!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                final android.app.AlertDialog waitingDialog= new SpotsDialog.Builder()
                        .setMessage("Please Wait for SIGN IN")
                        .setCancelable(false)
                        .setContext(MainActivity.this)
                        .build();
                waitingDialog.show();

                auth.signInWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingDialog.dismiss();
                                FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                Common.currentUser = dataSnapshot.getValue(User.class);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DriversInformation").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                reference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        if (!editPassword.getText().toString().equals(user.getPassword())){
                                            updateProfile(editPassword.getText().toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                    //write values in paper
                                Paper.book().write(Common.user_field, editEmail.getText().toString());
                                Paper.book().write(Common.pwd_field, editPassword.getText().toString());

                                startActivity(new Intent(MainActivity.this, PermissionActivity.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Snackbar.make(rootLayout, "Failed "+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                btnSignIn.setEnabled(true);
                            }
                        });
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void updateProfile(String password){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("password", password);
        reference.updateChildren(hashMap);
    }

    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Register");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register, null);

        final MaterialEditText editEmail = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText editPassword = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText editName = register_layout.findViewById(R.id.edtName);
        final MaterialEditText editPhone = register_layout.findViewById(R.id.edtPhone);

        dialog.setView(register_layout);

        dialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                btnRegister.setEnabled(false);

                if(TextUtils.isEmpty(editEmail.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(editPhone.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter phone number", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(editPassword.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(editPassword.getText().toString().length() < 6){
                    Snackbar.make(rootLayout, "Password too short !!!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(editName.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter name", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                final android.app.AlertDialog waitingDialog= new SpotsDialog.Builder()
                        .setMessage("Please Wait for SIGN IN")
                        .setCancelable(false)
                        .setContext(MainActivity.this)
                        .build();
                waitingDialog.show();

                auth.createUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        User user = new User();
                        user.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        user.setEmail(editEmail.getText().toString().trim());
                        user.setName(editName.getText().toString().trim());
                        user.setPhone(editPhone.getText().toString().trim());
                        user.setPassword(editPassword.getText().toString().trim());
                        user.setRate("0.0");
                        user.setStatus("online");

                        currentUser = FirebaseAuth.getInstance().getCurrentUser();

                        users.child(currentUser.getUid())
                                .setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        waitingDialog.dismiss();
                                        Snackbar.make(rootLayout, "Register Successfully", Snackbar.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this, PermissionActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        waitingDialog.dismiss();
                                        Snackbar.make(rootLayout, "Failed "+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                        btnRegister.setEnabled(true);
                                    }
                                });
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Snackbar.make(rootLayout, "Failed "+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                btnRegister.setEnabled(true);
                            }
                        });

            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }
}
