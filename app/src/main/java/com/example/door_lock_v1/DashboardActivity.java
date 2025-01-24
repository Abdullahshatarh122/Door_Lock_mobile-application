package com.example.door_lock_v1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.door_lock_v1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth auth;

    private TextView doorStatusTextView;
    private Button unlockButton , logoutButton;
    private ImageButton notificationButton;
    private Switch  motionSwitch;
    private TextView recentActivityTextView;

    private ImageView lockIcon;

    private boolean isDoorLocked = true;
    private FirebaseDatabase remoteDatabase;
    private DatabaseReference doorStatusRef, recentActivityRef, smokeRef, fireRef, enMotion, motion, keyPadRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_dashboard);

        doorStatusTextView = findViewById(R.id.doorLockStatus);
        unlockButton = findViewById(R.id.unlockButton);
        notificationButton = findViewById(R.id.notificationButton);
        motionSwitch = findViewById(R.id.motionSwitch);
        recentActivityTextView = findViewById(R.id.recentActivityTextView);
        lockIcon = findViewById(R.id.doorLockIcon);
        logoutButton = findViewById(R.id.logoutButton);

        remoteDatabase = FirebaseDatabase.getInstance("https://esptestgb1-default-rtdb.firebaseio.com/");
        doorStatusRef = remoteDatabase.getReference("lock");// int lock = 1 (locked) , lock = 0 (unlocked)
        recentActivityRef = remoteDatabase.getReference("recentActivity");
        fireRef = remoteDatabase.getReference("flame");
        smokeRef = remoteDatabase.getReference("gas");
        enMotion = remoteDatabase.getReference("EnMotion");
        motion = remoteDatabase.getReference("motion");
        keyPadRef = remoteDatabase.getReference("KEYBAD");
 
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();

        loadDoorStatus();
        loadRecentActivity();
        loadEnabelMotion();

        unlockButton.setOnClickListener(v -> {
            if (isDoorLocked) {
                isDoorLocked = false;
                doorStatusRef.setValue(0);
                addRecentActivity(user.getDisplayName() +" unlocked the door at " + getCurrentTime());
            } else {
                isDoorLocked = true;
                doorStatusRef.setValue(1);
                addRecentActivity(user.getDisplayName() +" locked the door at " + getCurrentTime());
            }
            updateDoorStatusUI();
        });


        notificationButton.setOnClickListener(v -> showNotificationPanel());


        motionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            String message;
            if (isChecked) {
                message = "Motion alerts enabled";
                enMotion.setValue(1);
            }
            else{
                message = "Motion alerts disabled";
                enMotion.setValue(0);
            }

            Toast.makeText(DashboardActivity.this, message, Toast.LENGTH_SHORT).show();
        });
        fireRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int fireDetected = dataSnapshot.getValue(int.class);
                if (fireDetected == 0) {
                    sendLocalNotification("Fire Alert", "Fire detected inside the house!");
                    addRecentActivity("Fire detected at " + getCurrentTime());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        smokeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int smokeDetected = dataSnapshot.getValue(int.class);
                if (smokeDetected == 0) {
                    sendLocalNotification("Smoke Alert", "Smoke detected inside the house!");
                    addRecentActivity("Smoke detected at " + getCurrentTime());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        motion.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(motionSwitch.isChecked()){
                    int motionDetected = dataSnapshot.getValue(int.class);
                    if (motionDetected == 0) {
                        sendLocalNotification("Motion Alert", "Motion detected inside the house!");
                        addRecentActivity("Motion detected at " + getCurrentTime());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        keyPadRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int manualLock = dataSnapshot.getValue(int.class);
                if (manualLock == 1) {
                    addRecentActivity("Door locked manually at " + getCurrentTime());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signOut();

            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadDoorStatus() {
        doorStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int doorStatus = dataSnapshot.getValue(int.class);

                isDoorLocked = (doorStatus == 1);
                updateDoorStatusUI();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void loadRecentActivity() {
        recentActivityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder activities = new StringBuilder();
                List<String> activityList = new ArrayList<>();

                for (DataSnapshot activitySnapshot : dataSnapshot.getChildren()) {
                    String activity = activitySnapshot.getValue(String.class);
                    if (activity != null) {
                        activityList.add(activity);
                    }
                }
                int start = Math.max(0, activityList.size() - 5);
                List<String> lastFiveActivities = activityList.subList(start, activityList.size());
                Collections.reverse(lastFiveActivities);
                for (String activity : lastFiveActivities) {
                    activities.append(activity).append("\n");
                }
                recentActivityTextView.setText(activities.toString().trim());
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }


    private void updateDoorStatusUI() {
        if (isDoorLocked) {
            doorStatusTextView.setText("Locked");
            lockIcon.setImageResource(R.drawable.lock_24dp_000000);
            unlockButton.setText("Unlock");
        } else {
            doorStatusTextView.setText("Unlocked");
            lockIcon.setImageResource(R.drawable.lock_open_24dp_000000_fill0_wght400_grad0_opsz24);
            unlockButton.setText("Lock");
        }
    }

    private void addRecentActivity(String activity) {
        recentActivityRef.push().setValue(activity);
    }
    private void loadEnabelMotion() {
        enMotion.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int enMotionStatus = dataSnapshot.getValue(int.class);
                if(enMotionStatus == 0){
                    motionSwitch.setChecked(false);
                }
                else {
                    motionSwitch.setChecked(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
//                Toast.makeText(DashboardActivity.this, "Failed to load door status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNotificationPanel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recent activities");

        StringBuilder notificationMessage = new StringBuilder();
        notificationMessage.append("Notifications:\n");

        recentActivityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> activityList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String activity = snapshot.getValue(String.class);
                    if (activity != null) activityList.add(activity);
                }

                Collections.reverse(activityList);
                for (int i = 0; i < Math.min(65, activityList.size()); i++) {
                    notificationMessage.append("- ").append(activityList.get(i)).append("\n");
                }

                builder.setMessage(notificationMessage.toString().trim());
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                builder.show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Failed to load notifications: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String getCurrentTime() {
        return java.text.DateFormat.getTimeInstance().format(new java.util.Date());
    }
    // Method to send a local notification
    private void sendLocalNotification(String title, String message) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        String channelId = "DoorLockNotifications";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.lock_24dp_000000)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Door Lock Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, builder.build());
    }
}
