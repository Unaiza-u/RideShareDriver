package com.example.ridesharedriver.Service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ridesharedriver.Common.Common;
import com.example.ridesharedriver.Model.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MyFirebaseIdService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKEN",s);
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        if (firebaseUser != null){
                            updateToken(token);
                        }

                    }
                });
    }

    private void updateToken(String tokenRefresh) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        Token token = new Token(tokenRefresh);
        reference.child(user.getUid()).setValue(token);
        

    }
}
