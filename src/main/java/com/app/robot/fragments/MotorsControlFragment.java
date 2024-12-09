package com.app.robot.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.app.robot.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MotorsControlFragment extends Fragment implements View.OnTouchListener{
    /**
     * The class implements the layout fragment_motors_control.
     */
    private TextView speedView;
    private final DatabaseReference motorNode = FirebaseDatabase.getInstance().getReference("Movement");

    public MotorsControlFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_motors_control, container, false);


        //Initialize Widgets
        final Button forward = rootView.findViewById(R.id.buttonForward);
        final Button backward = rootView.findViewById(R.id.buttonBackwards);
        final Button right = rootView.findViewById(R.id.buttonRight);
        final Button left = rootView.findViewById(R.id.buttonLeft);
        final SeekBar speedBar = rootView.findViewById(R.id.seekBar);
        speedView = (TextView) rootView.findViewById(R.id.textViewSpeed);
        //Adding listeners to the widgets
        forward.setOnTouchListener(this);
        backward.setOnTouchListener(this);
        right.setOnTouchListener(this);
        left.setOnTouchListener(this);
        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Called when the value of the seek bar is changing.
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedView.setText(progress +"%");
                motorNode.child("DC").setValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        speedView.setText(speedBar.getProgress()+"%");
        motorNode.child("DC").setValue(speedBar.getProgress());
        return rootView;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN: // if button was pressed
                switch (v.getId())
                {
                    case R.id.buttonForward:
                        writeToMotors(false,false,true, false);
                        break;

                    case R.id.buttonBackwards:
                        writeToMotors(false,false,false, true);
                        break;

                    case R.id.buttonRight:
                        writeToMotors(true, false, false, false);
                        break;

                    case R.id.buttonLeft:
                        writeToMotors(false, true, false, false);
                        break;
                    default:
                        Snackbar.make(v.getRootView() , "ERROR", Snackbar.LENGTH_LONG);
                        break;
                }
               break;
            case MotionEvent.ACTION_UP:
                writeToMotors(false, false, false, false);
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * The function changes the driving direction
     * @param right turns robot right
     * @param left turns robot left
     * @param forward moves robot forward
     * @param backward moves robot backwards
     */
    private void writeToMotors(boolean right, boolean left, boolean forward, boolean backward)
    {
        motorNode.child("Forward").setValue(forward);
        motorNode.child("Backward").setValue(backward);
        motorNode.child("Right").setValue(right);
        motorNode.child("Left").setValue(left);
    }

}
