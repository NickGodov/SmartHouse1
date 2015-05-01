package com.isosystem.smarthouse.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;

import com.isosystem.smarthouse.R;

public class SettingsActivity extends Activity implements ActionBar.TabListener {

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.settingsActivity_Pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
				
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
		
			@Override
			public void onPageSelected (int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});
		
		for (int i=0;i<mSectionsPagerAdapter.getCount();i++) {
			actionBar.addTab( 
					actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}
	
	public void setCurrentItem (int item, boolean smoothScroll) {
		mViewPager.setCurrentItem(item, smoothScroll);
	}
	
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }
    
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
	
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        
        @Override
        public Fragment getItem(int position) {
        	if (position ==0) {
        		return new GeneralSettings();
        	} else if (position == 1){
        		return new MainMenuSettingsFragment();
        	} else if (position == 2) {
        		return new FormattedScreensSettingsFragment();
        	} else if (position == 3) {
        		return new LogsFragment();
        	}
        	return null;	
        }

        @Override
        public int getCount() {
            return 4;
        }
           
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Общие";
                case 1:
                    return "Меню";
                case 2:
                    return "Окна вывода";
                case 3:
                    return "Журнал";
            }
            return null;
        }
        
    }

}
