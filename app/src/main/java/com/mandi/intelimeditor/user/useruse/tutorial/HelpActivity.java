package com.mandi.intelimeditor.user.useruse.tutorial;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.common.BaseActivity;
import com.mandi.intelimeditor.R;


public class HelpActivity extends BaseActivity {

    @Override
    public int getLayoutResId() {
        return R.layout.activity_help;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecyclerView recyclerView = findViewById(R.id.rcv_Info);
        recyclerView.setAdapter(new HelpAdapter(this));
    }
}
