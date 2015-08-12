package com.bluescape;

import android.content.Intent;
import android.widget.Button;

import com.bluescape.activity.LoginActivity;


public class LoginActivityTest extends
		android.test.ActivityUnitTestCase<LoginActivity> {
	private LoginActivity activity;

	public LoginActivityTest() {
		super(LoginActivity.class);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// setActivityInitialTouchMode(false);
		Intent intent = new Intent(getInstrumentation().getTargetContext(),
				LoginActivity.class);
		startActivity(intent, null, null);
		activity = getActivity();
	}

	public void testLoginB() {
		// login button test
		int loginBtn = R.id.loginBtn;
		assertNotNull(activity.findViewById(loginBtn));
		Button view = (Button) activity.findViewById(loginBtn);
		assertEquals("Incorrect label of the button", "Sign In", view.getText());
		// emailEt test
		int emailEt = R.id.emailEt;
		assertNotNull(activity.findViewById(emailEt));
		// passwordEt test
		int passwordEt = R.id.passwordEt;
		assertNotNull(activity.findViewById(passwordEt));

	}

}