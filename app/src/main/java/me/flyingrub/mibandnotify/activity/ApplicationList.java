/*
 * Copyright (C) 2015 - Holy Lobster
 *
 * Nuntius is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Nuntius is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nuntius. If not, see <http://www.gnu.org/licenses/>.
 */

package me.flyingrub.mibandnotify.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import me.flyingrub.mibandnotify.R;
import me.flyingrub.mibandnotify.adapter.AppAdapter;
import me.flyingrub.mibandnotify.data.WhitelistedApp;


public class ApplicationList extends ActionBarActivity {
    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Toolbar toolbar;

    private WhitelistedApp whitelistedApp;
    private PackageManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        setTitle(getResources().getString(R.string.app_name));

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        pm = getPackageManager();

        whitelistedApp = new WhitelistedApp(this); // init the class with this context.
        adapter = new AppAdapter(this, whitelistedApp.getBlacklistedAppList(), true);

        recyclerView.setAdapter(adapter);
        adapter.SetOnItemClickListener(new AppAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                itemSelected(position);
            }
        });
    }

    public void itemSelected(int i) {
        ApplicationInfo oldApp = whitelistedApp.getBlacklistedAppList().get(i);
        whitelistedApp.remove(i);
        adapter.refresh(whitelistedApp.getBlacklistedAppList());
        showInfo(oldApp);
        checkIfEmpty();
    }

    public void showInfo(final ApplicationInfo app) {
        SnackbarManager.show(
                Snackbar.with(getApplicationContext())
                        .actionColor(getResources().getColor(R.color.main))
                        .actionLabel(getString(R.string.undo))
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                whitelistedApp.add(app);
                                adapter.refresh(whitelistedApp.getBlacklistedAppList());
                                checkIfEmpty();
                            }
                        }) // action button's ActionClickListener
                        .text(getString(R.string.app_removed, pm.getApplicationLabel(app))), this);
    }

    @Override
    public void onResume(){
        super.onResume();

        whitelistedApp.getFromPref();
        adapter.refresh(whitelistedApp.getBlacklistedAppList());

        checkIfEmpty();
    }

    public void checkIfEmpty() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.centerLayout);
        if (whitelistedApp.getBlacklistedAppList().isEmpty()) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
    }

    public void onFabClick(View v){
        Intent intent = new Intent(this, AddApplication.class);
        startActivity(intent);
    }


}

