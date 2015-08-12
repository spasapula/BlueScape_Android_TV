package com.bluescape;

import android.content.Intent;

import com.bluescape.activity.DashboardActivity;


public class DashboardActivityTest extends
		android.test.ActivityUnitTestCase<DashboardActivity> {
	private DashboardActivity activity;

	public DashboardActivityTest() {
		super(DashboardActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// setActivityInitialTouchMode(false);
		Intent intent = new Intent(getInstrumentation().getTargetContext(),
				DashboardActivity.class);
		startActivity(intent, null, null);
		activity = getActivity();
	}

	public void testLayout() {
		int dashWebview = R.id.dashWebview;
		assertNotNull(activity.findViewById(dashWebview));
	}

}