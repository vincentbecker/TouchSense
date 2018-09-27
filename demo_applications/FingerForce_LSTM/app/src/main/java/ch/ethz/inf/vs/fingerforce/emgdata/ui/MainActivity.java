/* This file is part of EmgVisualizer.

    EmgVisualizer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EmgVisualizer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EmgVisualizer.  If not, see <http://www.gnu.org/licenses/>.
*/
package ch.ethz.inf.vs.fingerforce.emgdata.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.squareup.otto.Subscribe;

import ch.ethz.inf.vs.fingerforce.R;
import ch.ethz.inf.vs.fingerforce.emgdata.model.EventBusProvider;
import ch.ethz.inf.vs.fingerforce.emgdata.ui.fragments.ControlFragment;
import ch.ethz.inf.vs.fingerforce.emgdata.ui.fragments.FingerForceFragment;
import ch.ethz.inf.vs.fingerforce.emgdata.ui.fragments.GraphFragment;
import ch.ethz.inf.vs.fingerforce.emgdata.ui.fragments.HomeFragment;
import ch.ethz.inf.vs.fingerforce.emgdata.ui.fragments.BicycleMapFragment;
import ch.ethz.inf.vs.fingerforce.emgdata.ui.fragments.MyoListFragment;
import ch.ethz.inf.vs.fingerforce.emgdata.ui.fragments.StylusFragment;
import ch.ethz.inf.vs.fingerforce.emgdata.ui.fragments.TextFragment;

/**
 * Main activity for handling main drawer menu with fragment changes
 * @author Nicola
 */
public class MainActivity extends AppCompatActivity {

    /** TAG for debugging purpose */
    private static final String TAG = "MainActivity";

    /** Arrays of menu title strings */
    private String TITLES[] = {
            "Home",
            "Thalmic Myo",
            "Search for Myo",
            "Control panel",
            "Graph viewer",
            "Finger Force",
            "Bicycle map",
            "Text marking",
            "Stylus drawing"};

    /** Array of icons reference ID, -1 if a divider */
    private int ICONS[] = {
            R.drawable.ic_home_grey600_24dp,
            -1,
            R.drawable.ic_magnify_grey600_24dp,
            R.drawable.ic_myo_grey600_24dp,
            R.drawable.ic_action,
            R.drawable.ic_session,
            R.drawable.ic_session,
            R.drawable.ic_session,
            R.drawable.ic_session};

    /** Constant for Home menu position */
    private static final int POSIT_HOME = 1;
    /** Constant for Search menu position */
    private static final int POSIT_SEARCH = 3;
    /** Constant for Control menu position */
    private static final int POSIT_CONTROL = 4;
    /** Constant for Graph menu position */
    private static final int POSIT_GRAPH = 5;
    /** Constant for Graph menu position */
    private static final int POSIT_FINGER_FORCE = 6;
    private static final int POSIT_MAP = 7;
    private static final int POSIT_TEXT = 8;
    private static final int POSIT_STYLUS = 9;


    /** App Toolbar */
    private Toolbar toolbar;
    /** Recycler view reference */
    private RecyclerView mRecyclerView;
    /** Menu adapter */
    private MenuAdapter mAdapter;
    /** Layout manager for Recycler view */
    private RecyclerView.LayoutManager mLayoutManager;
    /** Drawer Layout */
    private DrawerLayout mDrawerLayout;
    /** Drawer toogle */
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.main_tool_bar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.main_left_menu);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new MenuAdapter(TITLES, ICONS);

        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        changeFragment(new HomeFragment(), POSIT_HOME);
        EventBusProvider.register(this);
    }

    /**
     * Private method for triggering a fragment switch at runtime
     * @param fragment Fragment involved
     * @param position Position of the menu to be selected
     */
    private void changeFragment(Fragment fragment, int position) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        setTitle(TITLES[position - 1]);
        mAdapter.updateSelectedItem(position);
        mDrawerLayout.closeDrawers();
    }

    @Subscribe
    public void changeFragment(PositionEvent evt) {

        int position = evt.getPosition();

        // Myo alert message if not setted
        MySensorManager mngr = MySensorManager.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.no_myo));
        builder.setMessage(getString(R.string.you_must_perform_a_scan));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Fragment fragment = new MyoListFragment();
                changeFragment(fragment, POSIT_SEARCH);
                mDrawerLayout.closeDrawers();
                dialogInterface.cancel();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDrawerLayout.closeDrawers();
                dialogInterface.cancel();
            }
        });
        AlertDialog alert = builder.create();

        Fragment fragment = null;
        // Menu selector
        switch (position) {
            case POSIT_HOME:
                fragment = new HomeFragment();
                break;
            case POSIT_SEARCH:
                fragment = new MyoListFragment();
                break;
            case POSIT_CONTROL:
                if (!mngr.isMyoFound()) {
                    fragment = null;
                    alert.show();
                } else {
                    fragment = new ControlFragment();
                }
                break;
            case POSIT_GRAPH:
                if (!mngr.isMyoFound()) {
                    fragment = null;
                    alert.show();
                } else {
                    fragment = new GraphFragment();
                }
                break;
            case POSIT_FINGER_FORCE:
                if (!mngr.isMyoFound()) {
                    fragment = null;
                    alert.show();
                } else {
                    fragment = new FingerForceFragment();
                }
                break;
            case POSIT_MAP:
                if (!mngr.isMyoFound()) {
                    fragment = null;
                    alert.show();
                } else {
                    fragment = new BicycleMapFragment();
                }
                break;
            case POSIT_TEXT:
                if (!mngr.isMyoFound()) {
                    fragment = null;
                    alert.show();
                } else {
                    fragment = new TextFragment();
                }
                break;
            case POSIT_STYLUS:
                if (!mngr.isMyoFound()) {
                    fragment = null;
                    alert.show();
                } else {
                    fragment = new StylusFragment();
                }
                break;
        }
        if (fragment != null) {
            changeFragment(fragment, position);
        }
    }


    /**
     * Public method for opening Myo control windows
     */
    public void changeFragmentMyoControl() {
        changeFragment(new ControlFragment(), POSIT_CONTROL);
    }
}
