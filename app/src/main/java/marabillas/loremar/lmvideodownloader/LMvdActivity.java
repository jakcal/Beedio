/*
 *     LM videodownloader is a browser app for android, made to easily
 *     download videos.
 *     Copyright (C) 2018 Loremar Marabillas
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.lmvideodownloader;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import marabillas.loremar.lmvideodownloader.download_feature.DownloadManager;
import marabillas.loremar.lmvideodownloader.download_feature.Downloads;

public class LMvdActivity extends Activity implements TextView.OnEditorActionListener, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {
    private EditText webBox;
    private BrowserManager browserManager;
    private Uri appLinkData;
    private DrawerLayout layout;
    private Intent downloadService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        webBox = findViewById(R.id.web);
        webBox.setOnEditorActionListener(this);

        ImageButton go = findViewById(R.id.go);
        go.setOnClickListener(this);

        if (getFragmentManager().findFragmentByTag("BM") == null) {
            getFragmentManager().beginTransaction().add(browserManager = new BrowserManager(),
                    "BM").commit();
        }
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        //String appLinkAction = appLinkIntent.getAction();
        appLinkData = appLinkIntent.getData();

        layout = findViewById(R.id.drawer);
        ImageView menu = findViewById(R.id.menuButton);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.openDrawer(Gravity.START);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            NavigationView navigationView = findViewById(R.id.menu);
            navigationView.setNavigationItemSelectedListener(this);
        } else {
            ListView listView = findViewById(R.id.menu);
            String[] menuItems = new String[]{"Home", "Browser", "Downloads", "Bookmarks",
                    "History"};
            ArrayAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout
                    .simple_list_item_1, menuItems) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView textView = view.findViewById(android.R.id.text1);
                    textView.setTextColor(Color.WHITE);
                    return view;
                }
            };
            listView.setAdapter(listAdapter);
            listView.setOnItemClickListener(this);
        }

        downloadService = new Intent(this, DownloadManager.class);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        System.out.println("opening webview");
        new WebConnect(webBox, this).connect();
        return false;
    }

    OnBackPressedListener onBackPressedListener;

    @Override
    public void onBackPressed() {
        Fragment sourcePage = getFragmentManager().findFragmentByTag("updateSourcePage");
        if (sourcePage != null) {
            getFragmentManager().beginTransaction().remove(sourcePage).commit();
        } else if (layout.isDrawerVisible(Gravity.START)) {
            layout.closeDrawer(Gravity.START);
        } else if (onBackPressedListener != null) {
            onBackPressedListener.onBackpressed();
        } else super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if (getCurrentFocus() != null) {
            Utils.hideSoftKeyboard(this, getCurrentFocus().getWindowToken());
            System.out.println("opening webview");
            new WebConnect(webBox, this).connect();
        }
    }

    private void homeClicked() {
        Fragment fragment;
        browserManager.hideCurrentWindow();
        fragment = getFragmentManager().findFragmentByTag("Downloads");
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
        setOnBackPressedListener(null);
    }

    private void browserClicked() {
        Fragment fragment;
        browserManager.unhideCurrentWindow();
        fragment = getFragmentManager().findFragmentByTag("Downloads");
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    private void downloadsClicked() {
        if (getFragmentManager().findFragmentByTag("Downloads") == null) {
            browserManager.hideCurrentWindow();
            getFragmentManager().beginTransaction().add(R.id.main, new Downloads(),
                    "Downloads").commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        layout.closeDrawers();
        switch (item.getTitle().toString()) {
            case "Home":
                homeClicked();
                break;
            case "Browser":
                browserClicked();
                break;
            case "Downloads":
                downloadsClicked();
                break;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        layout.closeDrawers();
        switch (position) {
            case 0:
                homeClicked();
                break;
            case 1:
                browserClicked();
                break;
            case 2:
                downloadsClicked();
                break;
        }
    }

    public interface OnBackPressedListener {
        void onBackpressed();
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    public BrowserManager getBrowserManager() {
        return browserManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (appLinkData != null) {
            browserManager.newWindow(appLinkData.toString());
        }
    }

    public Intent getDownloadService() {
        return downloadService;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onRequestPermissionsResultCallback.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
    }

    ActivityCompat.OnRequestPermissionsResultCallback onRequestPermissionsResultCallback;

    void setOnRequestPermissionsResultListener(ActivityCompat.OnRequestPermissionsResultCallback
                                                       onRequestPermissionsResultCallback) {
        this.onRequestPermissionsResultCallback = onRequestPermissionsResultCallback;
    }

}