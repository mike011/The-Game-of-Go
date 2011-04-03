package charland.games.go;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * The very first class the game encounters.
 * 
 * @author Michael
 * 
 */
public class Go extends Activity implements OnClickListener {
    
    /**
     * Used for being able to parse the output of log cat.
     */
    public static final String TAG = "Go";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Set up click listeners for all the buttons
        View continueButton = findViewById(R.id.continue_button);
        continueButton.setOnClickListener(this);
        View newButton = findViewById(R.id.new_button);
        newButton.setOnClickListener(this);
        View aboutButton = findViewById(R.id.about_button);
        aboutButton.setOnClickListener(this);
        View exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.continue_button:
            startGame();
            break;
        case R.id.about_button:
            Intent i = new Intent(this, About.class);
            startActivity(i);
            break;
        case R.id.new_button:
            openNewGameDialog();
            break;
        case R.id.exit_button:
            finish();
            break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.settings:
            startActivity(new Intent(this, Prefs.class));
            return true;
            // More items go here (if any) ...
        }
        return false;
    }

    /**
     * Get ready to start the game.
     */
    private void openNewGameDialog() {
        startGame();
    }

    /**
     * Launches the game.
     */
    private void startGame() {
        Log.d(TAG, "Starting game");
        Intent intent = new Intent(Go.this, Game.class);
        startActivity(intent);
    }
}