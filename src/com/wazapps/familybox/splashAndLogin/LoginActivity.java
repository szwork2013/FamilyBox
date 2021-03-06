package com.wazapps.familybox.splashAndLogin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.splunk.mint.Mint;
import com.wazapps.familybox.MainActivity;
import com.wazapps.familybox.R;
import com.wazapps.familybox.expandNetwork.EmailInvite;
import com.wazapps.familybox.handlers.FamilyHandler;
import com.wazapps.familybox.handlers.InputHandler;
import com.wazapps.familybox.handlers.PhotoHandler;
import com.wazapps.familybox.handlers.UserHandler;
import com.wazapps.familybox.handlers.UserHandler.FamilyMembersFetchCallback;
import com.wazapps.familybox.newsfeed.NewsItem;
import com.wazapps.familybox.profiles.UserData;
import com.wazapps.familybox.profiles.UserData.DownloadCallback;
import com.wazapps.familybox.splashAndLogin.BirthdaySignupDialogFragment.DateChooserCallback;
import com.wazapps.familybox.splashAndLogin.EmailLoginDialogueFragment.EmailLoginScreenCallback;
import com.wazapps.familybox.splashAndLogin.EmailSignupFragment.SignupScreenCallback;
import com.wazapps.familybox.splashAndLogin.FamilyQueryFragment.QueryHandlerCallback;
import com.wazapps.familybox.splashAndLogin.GenderSignupDialogFragment.GenderChooserCallback;
import com.wazapps.familybox.splashAndLogin.MemberQueryFragment.QueryAnswerHandlerCallback;
import com.wazapps.familybox.splashAndLogin.StartFragment.StartScreenCallback;
import com.wazapps.familybox.util.LogUtils;

public class LoginActivity extends FragmentActivity implements
		StartScreenCallback, DateChooserCallback, GenderChooserCallback,
		SignupScreenCallback, EmailLoginScreenCallback, QueryHandlerCallback,
		QueryAnswerHandlerCallback {
	private static final String TAG_EMAIL_FRAG = "emailLogin";
	protected static final String TAG_LOGIN_SCR = "loginScreen";
	private static final String TAG_SIGNBIRTHDAY = "birthdayDialog";
	private static final String TAG_SIGNGENDER = "genderDialog";
	private static final String TAG_SGINUP_FRAG = "signupScreen";
	private static final String TAG_FAMILYQUERY_FRAG = "familyQueryScreen";
	private static final String TAG_MEMBERQUERY_FRAG = "memberQueryScreen";
	private static final int SELECT_PICTURE = 0;

	public static final String PIN_USER_FAMILY = "UserFamily";
	public static final String PIN_USER_FAMILY_MEMBERS = "UserFamilyMembers";

	public int familyQueryIndex = 0;
	public ArrayList<ParseObject> relatedFamilies = null;
	public ParseUser currentUser = null, currentFamilyMember = null;
	public UserData currentFamilyMemberDetails = null;
	public ParseObject currentFamily = null;
	public ArrayList<ParseUser> relatedFamilyMembers = null;
	public ArrayList<UserData> relatedFamilyMemberDetails = null;

	private UserHandler userHandler = null;

	// pre-creating the callback functions used by parse to make code more
	// readable
	// all these callbacks are defined in the "setupCallbackFunctions" method
	private SignUpCallback userCreationCallback = null;
	private SaveCallback familyCreationCallback = null;
	private FindCallback<ParseObject> familiesListFetchCallback = null;
	private UserHandler.FamilyMembersFetchCallback familyMembersFetchCallback = null;
	private LogInCallback loginCallback = null;
	private GetCallback<ParseObject> loginFetchCallback = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Mint.initAndStartSession(LoginActivity.this, "ad50ec84");
		setContentView(R.layout.activity_login_screen);
		// Hide the status bar.
		getActionBar().hide();
		userHandler = new UserHandler();
		setUpCallbackFunctions();

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			// checking if loginActivity was called by a sign out action or by
			// splash screen and determine the transition animations
			// accordingly.
			if (extras.containsKey(MainActivity.LOG_OUT_ACTION))
				overridePendingTransition(R.anim.enter_reverse,
						R.anim.exit_reverse);

			else if (extras.containsKey(SplashActivity.SPLASH_ACTION))
				overridePendingTransition(R.anim.enter, R.anim.exit);

			// checking if the system should prompt user with family query or
			// with
			// the regular login screen. if family query is launched - call
			// userCreationCallback and pass control to it.
			if (extras.containsKey(SplashActivity.HANDLE_QUERY)) {
				if (extras.getBoolean(SplashActivity.HANDLE_QUERY)) {
					// maybe pass logics to splash screen and use pin to
					// local datastore for passing information
					currentUser = ParseUser.getCurrentUser();
					userCreationCallback.done(null);
					return;
				}
			}
		}

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, new StartFragment(),
						TAG_LOGIN_SCR).commit();
	}

	private void setUpCallbackFunctions() {
		// handles the sign up process after registering the user on parse
		userCreationCallback = new SignUpCallback() {
			private LoginActivity loginActivity;

			@Override
			public void done(ParseException e) {
				// if sign up succeeded - fetch all families
				// that might be related to user
				//also - add news item mentioning that user
				//has joined the network
				if (e == null) {
					String userNetwork = loginActivity.currentUser
							.getString(UserHandler.NETWORK_KEY);
					String userFirstName = loginActivity.currentUser
							.getString(UserHandler.FIRST_NAME_KEY);
					String userLastName = loginActivity.currentUser
							.getString(UserHandler.LAST_NAME_KEY);
					
					NewsItem userJoin = new NewsItem();
					userJoin.setNetworkId(userNetwork);
					userJoin.setContent("Joined the family network");
					userJoin.setUser(currentUser);
					userJoin.setUserFirstName(userFirstName);
					userJoin.setUserLastName(userLastName);
					userJoin.saveEventually();

					FamilyHandler.fetchRelatedFamilies(
							userLastName, userNetwork,
							loginActivity.familiesListFetchCallback);
				}

				// if sign up failed
				else {
					EmailSignupFragment signUpFrag = (EmailSignupFragment) getSupportFragmentManager()
							.findFragmentById(R.id.fragment_container);
					if (signUpFrag != null) {
						signUpFrag.turnOffProgress();
					}
					loginActivity.handleUserCreationError(e, false);
				}
			}

			private SignUpCallback init(LoginActivity loginActivity) {
				this.loginActivity = loginActivity;
				return this;
			}
		}.init(this);

		// handles the beginning of the families list query after
		// fetching the related families list
		familiesListFetchCallback = new FindCallback<ParseObject>() {
			private LoginActivity loginActivity;

			@Override
			public void done(List<ParseObject> fetchedFamilies, ParseException e) {

				// if fetching succeeded
				if (e == null) {
					loginActivity.relatedFamilies = new ArrayList<ParseObject>(
							fetchedFamilies);

					loginActivity.familyQueryIndex = 0;
					loginActivity.handleFamilyQuery();
				}

				// if fetching failed
				else {
					loginActivity.handleUserCreationError(e, true);
				}
			}

			private FindCallback<ParseObject> init(LoginActivity loginActivity) {
				this.loginActivity = loginActivity;
				return this;
			}
		}.init(this);

		// handles the launching process of the application
		// after creating a new family for the new user
		familyCreationCallback = new SaveCallback() {
			private LoginActivity loginActivity;

			@Override
			public void done(ParseException e) {
				// if family creation for the new user was successful
				// then user can enter the main application
				if (e == null) {
					loginActivity.enterApp();
				}

				// if family creation failed
				else {
					loginActivity.handleUserCreationError(e, true);
				}
			}

			private SaveCallback init(LoginActivity loginActivity) {
				this.loginActivity = loginActivity;
				return this;
			}
		}.init(this);

		// opens the family members query fragment after fetching
		// the family members data
		familyMembersFetchCallback = 
				new UserHandler.FamilyMembersFetchCallback() {
			private LoginActivity loginActivity;

			@Override
			public void done(ParseException e) {
				// if fetching was successful
				if (e == null) {
					loginActivity.launchFamilyQueryFragment();
				}

				else {
					// if fetching failed
					loginActivity.handleUserCreationError(e, true);
				}
			}

			private UserHandler.FamilyMembersFetchCallback init(
					LoginActivity loginActivity) {
				this.loginActivity = loginActivity;
				return this;
			}
		}.init(this);
		
		loginFetchCallback = new GetCallback<ParseObject>() {
			final StartFragment startFrag = (StartFragment) 
					getSupportFragmentManager()
					.findFragmentByTag(TAG_LOGIN_SCR);
			private LoginActivity activity;
			
			@Override
			public void done(ParseObject family, ParseException e) {
				//if query fetching has failed
				if (e != null) {
					activity.handleUserLoginError(e, "error in user login. " +
							"please log in again", true, false);
					
					if (startFrag != null) {
						startFrag.turnOffProgress();
					}
					
					return;
				}
				
				//if query fetching succeeded
				activity.currentFamily = family;
				activity.relatedFamilyMembers = new ArrayList<ParseUser>();
				activity.relatedFamilyMemberDetails = new ArrayList<UserData>();	
				activity.userHandler.fetchFamilyMembers (
						activity.relatedFamilyMembers,
						activity.relatedFamilyMemberDetails,
						activity.currentFamily,
						activity.currentUser.getObjectId(), true,
						new FamilyMembersFetchCallback() {

							@Override
							public void done(ParseException e) {
								if (e == null) {
									activity.enterApp();
									return;
								}

								else {
									activity.handleUserLoginError(
											e,"error in user login. " +
											"please log in again", true, false);
									
									if (startFrag != null) {
										startFrag.turnOffProgress();
									}
								}
							}
						});
			}
			
			private GetCallback<ParseObject> init(LoginActivity activity) {
				this.activity = activity;
				return this;
			}
		}.init(this);
		

		// Handles the process of login in the user
		// and caching relevant data
		loginCallback = new LogInCallback() {
			final StartFragment startFrag = (StartFragment) getSupportFragmentManager()
					.findFragmentByTag(TAG_LOGIN_SCR);
			LoginActivity loginActivity;

			@Override
			public void done(ParseUser user, ParseException e) {
				// if login process succeeded
				if (e == null) {
					loginActivity.currentUser = user;

					// if the user did not pass the family query yet
					// pass to family query screen.
					if (!currentUser.getBoolean(UserHandler.PASS_QUERY_KEY)) {
						loginActivity.userCreationCallback.done(null);
						return;
					}

					// else if the user has passed the family query
					// then cache related family & members data and
					// enter main app
					else {
						ParseQuery<ParseObject> familyQuery = ParseQuery
								.getQuery(FamilyHandler.FAMILY_CLASS_NAME);

						String familyId = loginActivity.currentUser
								.getString(UserHandler.FAMILY_KEY);

						// fetch the user's family for caching purposes
						familyQuery.getInBackground(familyId, loginFetchCallback);
					}
				}

				// if login process failed
				else {
					loginActivity.handleUserLoginError(e, e.getMessage(), false, false);
					if (startFrag != null) {
						startFrag.turnOffProgress();
					}
				}
			}

			private LogInCallback init(LoginActivity loginActivity) {
				this.loginActivity = loginActivity;
				return this;
			}
		}.init(this);
	}

	/**
	 * Launches the main activity, call this function when you want to pass to
	 * the application's actual main screen
	 */
	private void enterApp() {
		currentFamily.pinInBackground(PIN_USER_FAMILY, new SaveCallback() {
			private LoginActivity loginActivity;

			@Override
			public void done(ParseException e) {
				//if family pin failed
				if (e != null) {
					loginActivity.handleUserLoginError(e, "error in user "
							+ "login. please log in again", true, false);
					return;
				}
				
				// if family pin succeeded
				ParseUser.pinAllInBackground(PIN_USER_FAMILY_MEMBERS,
						loginActivity.relatedFamilyMembers, new SaveCallback() {
					
							@Override
							public void done(ParseException e) {
								if (e == null) {
									Intent intent = 
											new Intent(loginActivity, MainActivity.class);
									startActivity(intent);
									loginActivity.finish();
								}
	
								else {
									loginActivity.handleUserLoginError(e,
											"error in user login. please log in again", 
											true, true);
								}
							}
						});
			}

			private SaveCallback init(LoginActivity loginActivity) {
				this.loginActivity = loginActivity;
				return this;
			}
		}.init(this));
	}

	@Override
	public void openEmailLogin() {
		EmailLoginDialogueFragment frag = new EmailLoginDialogueFragment();
		frag.show(getSupportFragmentManager(), TAG_EMAIL_FRAG);
	}

	@Override
	public void openFacebookLogin() {
		// right now we are not going to implement this feature
		 Toast toast = Toast.makeText(this,
		 "This feature is not yet available"
		 , Toast.LENGTH_SHORT);
		 toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		 toast.show();
	}

	@Override
	public void openSignup() {
		getSupportFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.anim.enter, R.anim.exit,
						R.anim.enter_reverse, R.anim.exit_reverse)
				.replace(R.id.fragment_container, new EmailSignupFragment(),
						TAG_SGINUP_FRAG).addToBackStack(null).commit();
	}

	@Override
	public void openBirthdayInputDialog() {
		BirthdaySignupDialogFragment dialog = new BirthdaySignupDialogFragment();
		dialog.show(getSupportFragmentManager(), TAG_SIGNBIRTHDAY);
	}

	@Override
	public void openGenderInputDialog() {
		GenderSignupDialogFragment dialog = new GenderSignupDialogFragment();
		dialog.show(getSupportFragmentManager(), TAG_SIGNGENDER);
	}

	@Override
	public void setDate(String date) {
		EmailSignupFragment frag = (EmailSignupFragment) getSupportFragmentManager()
				.findFragmentByTag(TAG_SGINUP_FRAG);
		frag.setBirthday(date);
	}

	@Override
	public void setGender(String gender) {
		EmailSignupFragment frag = (EmailSignupFragment) getSupportFragmentManager()
				.findFragmentByTag(TAG_SGINUP_FRAG);
		frag.setGender(gender);
	}

	@Override
	public void openPhonePhotoBrowsing() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				SELECT_PICTURE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				Uri currImageURI = data.getData();
				File file = new File(PhotoHandler.getRealPathFromURI(this,
						currImageURI));

				if (file.exists()) {
					EmailSignupFragment frag = (EmailSignupFragment) getSupportFragmentManager()
							.findFragmentByTag(TAG_SGINUP_FRAG);
					Bitmap myBitmap = PhotoHandler.getImageBitmapFromFile(file);
					byte[] fileData = PhotoHandler
							.createDownsampledPictureData(myBitmap);
					String filename = "profilePic.JPEG";
					frag.setProfileImage(myBitmap, fileData, filename);
				}
			}
		}
	}

	@Override
	public void emailLoginAction(String email, String password) {
		final StartFragment startFrag = (StartFragment) getSupportFragmentManager()
				.findFragmentByTag(TAG_LOGIN_SCR);

		String errMsg = InputHandler.validateLoginInput(email, password);
		if (!errMsg.equals("")) {
			handleUserLoginError(null, errMsg, false, false);
			return;
		} else if (startFrag != null) {
			// add loading animation upon processing data
			startFrag.turnOnProgress();
		}

		ParseUser.logInInBackground("fb_" + email, password, loginCallback);
	}

	@Override
	public void signUp(final String firstName, final String lastName, final String email,
			final String birthday, final String gender, final String password,
			final String passwordConfirm, final byte[] profilePictureData,
			final String profilePictureName) {

		final EmailSignupFragment signUpFrag = (EmailSignupFragment) getSupportFragmentManager()
				.findFragmentByTag(TAG_SGINUP_FRAG);

		String errMsg = InputHandler.validateSignupInput(firstName, lastName,
				email, birthday, gender, password, passwordConfirm);

		if (!errMsg.equals("")) {
			Toast toast = Toast.makeText(this, errMsg, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
			return;
		} else if (signUpFrag != null) {
			signUpFrag.turnOnProgress();
		}
		
		//Check if user was invited to any family network
		ParseQuery<EmailInvite> inviteQuery = EmailInvite.getQuery();
		inviteQuery.whereEqualTo(EmailInvite.EMAIL_ADDR, email);
		inviteQuery.findInBackground(new FindCallback<EmailInvite>() {
			private LoginActivity activity;
			
			@Override
			public void done(List<EmailInvite> invites, ParseException e) {
				//if an error has happened during query evaluation
				if (e != null) {
					activity.handleUserCreationError(e, false);
					signUpFrag.turnOffProgress();
					return;
				} 
				
				//if query is evaluated successfully
				
				//if user was not invited or mistakenly invited
				//to more then one family network (should be impossible)
				//then create a new family network for the user
				if (invites.size() != 1) {
					ParseObject newNetwork = new ParseObject("FamilyNetwork");
					newNetwork.saveInBackground(new SaveCallback() {
						private ParseObject familyNetwork;
						
						@Override
						public void done(ParseException e) {
							if (e != null) {
								activity.handleUserCreationError(e, false);
								signUpFrag.turnOffProgress();
								return;
							}
							
							String networkId = familyNetwork.getObjectId();
							activity.currentUser = new ParseUser();
							userHandler.createNewUser(activity.currentUser, 
									firstName, lastName, email, gender, 
									password, birthday, profilePictureData,
									profilePictureName, networkId, userCreationCallback);		
						}
						
						private SaveCallback init(ParseObject familyNetwork) {
							this.familyNetwork = familyNetwork;
							return this;
						}
					}.init(newNetwork));
					
				} 
				
				//if user was invited to some family network - add him
				else {
					EmailInvite networkInvite = invites.get(0);
					String networkId = networkInvite.getNetworkId();
					activity.currentUser = new ParseUser();
					userHandler.createNewUser(currentUser, firstName, lastName, email,
							gender, password, birthday, profilePictureData,
							profilePictureName, networkId, userCreationCallback);					
				}
			}
			
			private FindCallback<EmailInvite> init(LoginActivity activity) {
				this.activity = activity;
				return this;
			}
		}.init(this));

	}

	@Override
	public void handleFamilyQuery() {
		// if no related families were found for the user
		// create a new family for him/her
		if (familyQueryIndex >= relatedFamilies.size()) {
			currentFamily = new ParseObject(FamilyHandler.FAMILY_CLASS_NAME);
			relatedFamilyMembers = new ArrayList<ParseUser>();
			FamilyHandler.createNewFamilyForUser(currentUser, true,
					currentFamily, familyCreationCallback);
			return;
		}

		// else - check the current related family
		// fetch its members and show to user
		relatedFamilyMembers = new ArrayList<ParseUser>();
		relatedFamilyMemberDetails = new ArrayList<UserData>();
		currentFamily = relatedFamilies.get(familyQueryIndex);

		userHandler.fetchFamilyMembers(relatedFamilyMembers,
				relatedFamilyMemberDetails, currentFamily,
				currentUser.getObjectId(), true, 
				familyMembersFetchCallback);
	}

	@Override
	public void handleMemberQuery() {
		// fetch the familyQueryFragment in case we want to start loading
		// animation
		final FamilyQueryFragment familyQueryFrag = (FamilyQueryFragment) getSupportFragmentManager()
				.findFragmentByTag(TAG_FAMILYQUERY_FRAG);

		currentFamily.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
			private LoginActivity loginActivity;

			@Override
			public void done(ParseObject object, ParseException e) {
				if (e == null) {
					loginActivity.currentFamilyMember = relatedFamilyMembers
							.get(0);
					loginActivity.currentFamilyMemberDetails = relatedFamilyMemberDetails
							.get(0);

					boolean isFatherTaken = loginActivity.currentFamily
							.has(FamilyHandler.RELATION_FATHER);
					boolean isMotherTaken = loginActivity.currentFamily
							.has(FamilyHandler.RELATION_MOTHER);
					boolean isMemberMale = loginActivity.currentFamilyMember
							.getString(UserHandler.GENDER_KEY).equals(
									UserHandler.GENDER_MALE) ? true : false;

					ArrayList<String> relationOptions = InputHandler
							.generateRelationOptions(loginActivity.currentUser,
									loginActivity.currentFamilyMemberDetails,
									isFatherTaken, isMotherTaken);

					// if only one relation option exists then the answer is
					// already known
					if (relationOptions.size() == 1) {
						familyQueryFrag.startLoadingSpinner();
						loginActivity.handleMemberQueryAnswer(relationOptions
								.get(0));
						return;
					}

					// else open the member query screen
					loginActivity.launchMemberQueryFragment(relationOptions,
							isMemberMale);
				} else {
					// TODO: handle error
				}
			}

			private GetCallback<ParseObject> init(LoginActivity loginActivity) {
				this.loginActivity = loginActivity;
				return this;
			}
		}.init(this));
	}

	@Override
	public void handleMemberQueryAnswer(String relation) {
		String userGender = currentUser.getString(UserHandler.GENDER_KEY);
		String familyMemberGender = currentFamilyMemberDetails.getGender();
		String familyMemberRole = currentFamilyMemberDetails.getRole();
		boolean isMemberUndefined = familyMemberRole
				.equals(UserData.ROLE_UNDEFINED) ? true : false;
		boolean isUserMale = userGender.equals(UserHandler.GENDER_MALE) ? true
				: false;
		boolean isMemberMale = familyMemberGender
				.equals(UserHandler.GENDER_MALE) ? true : false;

		FamilyHandler.updateUsersAndFamilyRelation(currentUser,
				currentFamilyMember, currentFamily, relation,
				isMemberUndefined, isUserMale, isMemberMale,
				true, new SaveCallback() {
					private LoginActivity loginActivity;

					@Override
					public void done(ParseException e) {
						if (e == null) {
							loginActivity.enterApp();
						} else {
							// TODO: handle error
						}
					}

					private SaveCallback init(LoginActivity loginActivity) {
						this.loginActivity = loginActivity;
						return this;
					}
				}.init(this));
	}

	public void handleUserCreationError(ParseException e, boolean wasUserCreated) {
		String errMsg = (e.getMessage().startsWith("username")) ? "Email already taken"
				: "Error in user creation, please sign up again";
		Toast toast = Toast.makeText(this, errMsg, Toast.LENGTH_LONG);
		LogUtils.logError("LoginActivity", e.getMessage());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();

		if (wasUserCreated) {
			ParseUser.logOut();
			currentUser.deleteInBackground();
		}
	}

	public void handleUserLoginError(ParseException e, String errMsg,
			boolean isUserLogged, boolean eraseDatabase) {
		Toast toast = Toast.makeText(this, errMsg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();

		if (e != null) {
			LogUtils.logError("LoginActivity", e.getMessage());
		}

		if (isUserLogged) {
			ParseUser.logOut();
		}

		if (eraseDatabase) {
			ParseUser.unpinAllInBackground();
		}
	}

	/**
	 * Launches the family query fragment, with the fetched families list
	 */
	protected void launchFamilyQueryFragment() {
		// progress to the next family in the related families list
		// in preparation for next iteration
		familyQueryIndex++;

		// pass arguments and control to family query fragment
		UserData userDetails = new UserData(currentUser,
				UserData.ROLE_UNDEFINED);
		userDetails.downloadProfilePicAsync(currentUser,
				new DownloadCallback() {
					private UserData userDetails;
					ArrayList<UserData> relatedFamilyMembers;

					@Override
					public void done(ParseException e) {
						// we can ignore error message
						Bundle args = new Bundle();
						args.putParcelable(FamilyQueryFragment.MEMBER_ITEM,
								userDetails);
						args.putParcelableArray(
								FamilyQueryFragment.QUERY_FAMILIES_LIST,
								relatedFamilyMembers
								.toArray(new UserData[relatedFamilyMemberDetails
								.size()]));
						args.putBoolean(FamilyQueryFragment.CURRENT_FAMILY, true);

						FamilyQueryFragment familyQueryFrag = new FamilyQueryFragment();
						familyQueryFrag.setArguments(args);
						// TODO: fix backstack bug
						FragmentManager fm = getSupportFragmentManager();
						FragmentTransaction ft = fm.beginTransaction();
						ft.setCustomAnimations(R.anim.enter, R.anim.exit,
								R.anim.enter_reverse, R.anim.exit_reverse);
						ft.replace(R.id.fragment_container, familyQueryFrag,
								TAG_FAMILYQUERY_FRAG).commit();
					}

					private DownloadCallback init(UserData userDetails,
							ArrayList<UserData> relatedFamilyMembersDetails) {
						this.userDetails = userDetails;
						this.relatedFamilyMembers = relatedFamilyMembersDetails;
						return this;
					}
				}.init(userDetails, relatedFamilyMemberDetails));
	}

	protected void launchMemberQueryFragment(ArrayList<String> relationOptions,
			boolean isMemberMale) {
		Bundle args = new Bundle();
		args.putParcelable(MemberQueryFragment.FAMILY_MEMBER_ITEM,
				currentFamilyMemberDetails);
		args.putStringArrayList(
				MemberQueryFragment.FAMILY_MEMBER_RELATION_OPTIONS,
				relationOptions);
		args.putBoolean(MemberQueryFragment.FAMILY_MEMBER_GENDER, isMemberMale);

		MemberQueryFragment memberQueryFrag = new MemberQueryFragment();
		memberQueryFrag.setArguments(args);
		getSupportFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.anim.enter, R.anim.exit,
						R.anim.enter_reverse, R.anim.exit_reverse)
				.replace(R.id.fragment_container, memberQueryFrag,
						TAG_MEMBERQUERY_FRAG).commit();
	}
}
