package com.app.robot.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import android.os.Bundle;

import com.app.robot.R;
import com.app.robot.adapters.PageViewAdapter;
import com.app.robot.dialogs.HitDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity implements ValueEventListener
        , TabLayout.OnTabSelectedListener {
    /**
     * Class Fields
     */
    private DatabaseReference laser_hit = FirebaseDatabase.getInstance()
            .getReference("Sensors").child("Laser_Detect");
    private HitDialog hitDialog;
    private ViewPager2 viewPager2;

    /**
     * When App launched, the function initializes the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //Sets the layout of the main activity.
        // Initialize the tabs
        viewPager2 = findViewById(R.id.viewpager2);
        viewPager2.setAdapter(new PageViewAdapter(this));
        final TabLayout tabLayout = findViewById(R.id.tabLayout);
        //Adding event listener which operate when there is a change of tabs
        tabLayout.addOnTabSelectedListener(this);
        // An event that happens when the tab is changed by swiping
        // It will change the selection of the tab according to the position of the selected tab
        viewPager2.registerOnPageChangeCallback(new OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.selectTab(tabLayout.getTabAt(position));

            }
        });
        hitDialog = new HitDialog(); // Creating new dialog object
        laser_hit.addValueEventListener(this);
        laser_hit.setValue(false);
     }

    /**
     *  An event that happens when the value on the laser hit field is changed on the database
     * @param snapshot
     */
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        if(Boolean.parseBoolean(snapshot.getValue().toString()))//If the robot was hit
        {
            openDialog();
        }
    }
    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    /**
     *     The function shows a dialog that tells the user that he got hit
     */
    private void openDialog()
    {
        hitDialog.show(getSupportFragmentManager(), "Hit");
        laser_hit.setValue(false);

    }

    /**
     * An event that happens when the user change the tab
     * @param tab
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager2.setCurrentItem(tab.getPosition());

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}