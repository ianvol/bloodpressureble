package com.example.bloodpressure.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.example.bloodpressure.SelectionActivity;

public class NotificationsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getActivity(), SelectionActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
