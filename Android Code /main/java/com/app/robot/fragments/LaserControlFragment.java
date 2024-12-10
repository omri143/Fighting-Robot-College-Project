 package com.app.robot.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.app.robot.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LaserControlFragment extends Fragment implements View.OnTouchListener, Switch.OnCheckedChangeListener
                                                            , ValueEventListener {

    private DatabaseReference laser = FirebaseDatabase.getInstance().getReference("Laser");
    private DatabaseReference laser_ctrl = laser.child("Override_ctrl");
    private ToggleButton toggleButton;
    private TextView textView;
    private TextView textViewDirection;
    private TextView xDisplay, yDisplay;
    private int x_angle , y_angle;
    public LaserControlFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_laser_control, container, false);
        //Initialize Widgets
        final Button xPos = root.findViewById(R.id.xpos);
        final Button xNeg = root.findViewById(R.id.xneg);
        final Button zPos = root.findViewById(R.id.ypos);
        final Button zNeg = root.findViewById(R.id.yneg);
        final Button centerServos = root.findViewById(R.id.buttonCenter);
        final Switch override_switch = root.findViewById(R.id.switchOverride);
        textView = root.findViewById(R.id.laserstateTextView);
        textViewDirection = root.findViewById(R.id.textViewDirection);
        yDisplay = root.findViewById(R.id.ytextView);
        xDisplay = root.findViewById(R.id.xtextView);
        toggleButton = root.findViewById(R.id.toggleButton);
        //Adding listeners
        override_switch.setOnCheckedChangeListener(this);
        toggleButton.setOnCheckedChangeListener(this);
        xNeg.setOnTouchListener(this);
        xPos.setOnTouchListener(this);
        zNeg.setOnTouchListener(this);
        zPos.setOnTouchListener(this);
        centerServos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.buttonCenter)
                {
                    // Initialize the position of the servos
                    initServos(120, 0);
                }
            }
        });
        laser.addValueEventListener(this);
        return root;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                switch (v.getId())
                {
                    case R.id.xpos:
                        textViewDirection.setText(R.string.negX);
                        rotateXServo(false, v);
                        break;
                    case R.id.xneg:
                        textViewDirection.setText(R.string.posX);
                        rotateXServo(true, v);
                        break;
                    case  R.id.ypos:
                        textViewDirection.setText(R.string.negY);
                        rotateYServo(false, v);
                        break;
                    case R.id.yneg:
                        textViewDirection.setText(R.string.posY);
                        rotateYServo(true, v);

                }
                updateDisplay(x_angle, y_angle);
                break;
            case MotionEvent.ACTION_UP:
                textViewDirection.setText(R.string.empty);
                break;
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId())
        {
            case R.id.toggleButton:
                laser_ctrl.child("LASER_ON").setValue(isChecked);
                break;
            case R.id.switchOverride:
                laser_ctrl.child("Enabled").setValue(isChecked);
                if(isChecked)
                {
                    toggleButton.setVisibility(View.VISIBLE); // Making the togglebutton visible
                    textView.setVisibility(View.VISIBLE);
                }
                else
                {
                    toggleButton.setVisibility(View.INVISIBLE);// Making the togglebutton invisible
                    textView.setVisibility(View.INVISIBLE);
                    toggleButton.setChecked(false);
                }
                break;
        }
    }
    //Reading from the database the current position of the servos
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        for (DataSnapshot data : snapshot.getChildren())
        {
            switch (data.getKey())
            {
                case "X_axis": // Reading the value from "X_axis" node
                    x_angle = Integer.parseInt(data.getValue().toString());
                    break;
                case "Y_axis": // Reading the value from "Y_axis" node
                    y_angle = Integer.parseInt(data.getValue().toString());
                    break;
                default:
                    break;
            }
        }
        updateDisplay(x_angle, y_angle);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Snackbar.make(this.getView() , "ERROR: " + error.getMessage(), Snackbar.LENGTH_LONG).show();
    }


    //The function initializes the state of the servos
    private void initServos(int x_angle , int y_angle)
    {
        laser.child("X_axis").setValue(x_angle);
        laser.child("Y_axis").setValue(y_angle);
        this.x_angle = x_angle;
        this.y_angle = y_angle;
        updateDisplay(x_angle, y_angle);
    }
    // The function updates the text views
    private void updateDisplay(int x_angle, int y_angle)
    {
        xDisplay.setText(x_angle+"°");
        yDisplay.setText(y_angle +"°");
    }

    /**
     * The function rotates the servo on the X axis according to the direction
     * @param rotateLeft
     * @param view
     */
    private void rotateXServo(boolean rotateLeft , View view)
    {
        if (rotateLeft) {
            if (x_angle + 10 <= 180) //If the angle is lower than 180 degrees (maximum angle of the servo)
            {
                x_angle += 10;
            } else {
                Snackbar.make(view.getRootView(), "The servo on the X-axis is on his maximum position\n",
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            if (x_angle - 10 >= 0) //If the angle is higher than 0 degrees (minimum angle of the servo)
            {
                x_angle -= 10;
            } else {
                Snackbar.make(view.getRootView(), "The servo on the X-axis is on his minimum position",
                        Snackbar.LENGTH_LONG).show();
            }
        }
        laser.child("X_axis").setValue(x_angle);
    }

    /**
     * The function rotates the servo on the Y axis according to the buttons
     *
     * @param rotateUp
     * @param view
     */
    private void rotateYServo(boolean rotateUp , View view)
    {
        if (rotateUp) {
            if (y_angle + 10 <= 120) //If the angle is lower than 120 degrees (maximum angle of the servo)
            {
                y_angle += 10;
            } else {
                Snackbar.make(view.getRootView(), "The servo on the Y-axis is on his maximum position",
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            if (y_angle - 10 >= 0) //If the angle is higher than 0 degree (minimum angle of the servo)
            {
                y_angle -= 10;
            } else {
                Snackbar.make(view.getRootView(), "The servo on the Y-axis is in his minimum position",
                        Snackbar.LENGTH_LONG).show();
            }
        }
        laser.child("Y_axis").setValue(y_angle);
    }
}