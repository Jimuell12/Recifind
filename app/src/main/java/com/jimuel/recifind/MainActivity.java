package com.jimuel.recifind;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jimuel.recifind.fragments.BookmarkFragment;
import com.jimuel.recifind.fragments.BotFragment;
import com.jimuel.recifind.fragments.HomeFragment;
import com.jimuel.recifind.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Handle navigation item selection here
            if (item.getItemId() == R.id.menu_home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (item.getItemId() == R.id.menu_bookmark) {
                replaceFragment(new BookmarkFragment());
                return true;
            } else if (item.getItemId() == R.id.menu_profile) {
                replaceFragment(new ProfileFragment());
                return true;
            } else if (item.getItemId() == R.id.menu_bot){
                replaceFragment(new BotFragment());
                return true;
            }
            return false;
        });
        replaceFragment(new HomeFragment());
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.framelayout1, fragment);
        transaction.commit();
    }
}
