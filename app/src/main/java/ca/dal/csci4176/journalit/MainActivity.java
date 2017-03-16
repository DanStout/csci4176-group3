package ca.dal.csci4176.journalit;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import ca.dal.csci4176.journalit.models.Entry;
import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    FloatingActionButton fab;
    List<Card> cards;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv = (RecyclerView)findViewById(R.id.cardList);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        fab.setOnClickListener(this);

        initializeData();
        RVAdapter adapter = new RVAdapter(cards);
        rv.setAdapter(adapter);


        Entry ent = new Entry();
        ent.setDate(LocalDate.now());
        ent.setText("This is the entry text");

        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(r -> r.deleteAll());
        realm.executeTransaction(r -> r.copyToRealm(ent));

        RealmResults<Entry> entries = realm
                .where(Entry.class)
                .findAll();

        Timber.d("Found %d entries", entries.size());

        for (Entry e : entries)
        {
            Timber.d("%s", e);
        }

    }

    private void initializeData(){
        cards = new ArrayList<>();
        cards.add(new Card("eat", "drink", R.mipmap.ic_launcher));
        cards.add(new Card("shop", "run", R.mipmap.ic_launcher));
        cards.add(new Card("talk", "ask", R.mipmap.ic_launcher));
        cards.add(new Card("sleep", "walk", R.mipmap.ic_launcher));
        cards.add(new Card("work", "fall", R.mipmap.ic_launcher));
        cards.add(new Card("ski", "drive", R.mipmap.ic_launcher));
    }


    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_edit:

                    break;
                case R.id.action_share:

                    break;
                case R.id.action_settings:

                    break;
            }
            return true;
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(MainActivity.this, "hi", Toast.LENGTH_SHORT).show();
    }
}
