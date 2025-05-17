package com.example.guitarprocessingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.guitarprocessingapp.ui.connection.DeviceSearchActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.guitarprocessingapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_bluetooth_connection, R.id.navigation_effects_menu, R.id.navigation_instruments_menu)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        navView.setSelectedItemId(R.id.navigation_bluetooth_connection);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            invalidateOptionsMenu();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        if (binding.navView.getSelectedItemId() == R.id.navigation_effects_menu) {
            getMenuInflater().inflate(R.menu.effects_menu, menu);
            return true;
        } else if (binding.navView.getSelectedItemId() == R.id.navigation_instruments_menu) {
            getMenuInflater().inflate(R.menu.instruments_menu, menu);
            return true;
        }
        getMenuInflater().inflate(R.menu.connection_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_search_devices) {
            Intent intent = new Intent(this, DeviceSearchActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}