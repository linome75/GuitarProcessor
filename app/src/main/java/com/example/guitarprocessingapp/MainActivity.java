package com.example.guitarprocessingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.guitarprocessingapp.ui.connection.DeviceSearchActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        BottomNavigationView navView = binding.navView;

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_bluetooth_connection,
                R.id.navigation_effects_menu,
                R.id.navigation_instruments_menu)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navView.setSelectedItemId(R.id.navigation_bluetooth_connection);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> invalidateOptionsMenu());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        int selectedId = binding.navView.getSelectedItemId();
        if (selectedId == R.id.navigation_effects_menu) {
            getMenuInflater().inflate(R.menu.effects_menu, menu);
        } else if (selectedId == R.id.navigation_instruments_menu) {
            getMenuInflater().inflate(R.menu.instruments_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.connection_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search_devices) {
            startActivity(new Intent(this, DeviceSearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
