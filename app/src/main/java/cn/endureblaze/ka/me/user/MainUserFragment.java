package cn.endureblaze.ka.me.user;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import cn.endureblaze.ka.Kirby;
import cn.endureblaze.ka.base.BaseFragment;
import cn.endureblaze.ka.helper.LayoutAnimationHelper;
import cn.endureblaze.ka.main.MainActivity;
import cn.endureblaze.ka.me.login.MainLoginFragment;
import cn.endureblaze.ka.me.user.userhead.HeadActivity;
import cn.endureblaze.ka.utils.EmailUtil;
import cn.endureblaze.ka.utils.GlideUtil;
import cn.endureblaze.ka.utils.PlayAnimUtil;
import cn.endureblaze.ka.utils.UserUtil;
import cn.endureblaze.ka.R;

public class MainUserFragment extends BaseFragment {
	private boolean CHANGE_HEAD=false;
	private ChangeUserHeadLocalReceiver localReceiver;
    private LocalBroadcastManager localBroadcastManager;
	private View view;
	private MainActivity m;
	private ImageView userHead;
	private String name;
	private String email;
	private String id;
	private CardView card;
	private Button edit_email;
	private Button edit_password;
	private IntentFilter intentFilter;

	private Button user_logout;

	private ImageView mo_userHead;

	private RelativeLayout changeUserHead;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.main_user, container, false);
		m = (MainActivity)getActivity();
		initUser(view);
		return view;
	}

	private void initUser(View view) {
		localBroadcastManager = localBroadcastManager.getInstance(getActivity());
		intentFilter = new IntentFilter();
		intentFilter.addAction("com.kirby.download.CHANGE_USERHEAD");
		localReceiver = new ChangeUserHeadLocalReceiver();
        //注册本地广播监听器
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
		name = UserUtil.getCurrentUser().getUsername();
		email = UserUtil.getCurrentUser().getEmail();
		id = UserUtil.getCurrentUser().getObjectId();
		TextView userName=(TextView)view.findViewById(R.id.user_name);
		TextView userId=(TextView)view.findViewById(R.id.user_id);
		TextView userTime=(TextView)view.findViewById(R.id.user_data);
		TextView userEmail=(TextView)view.findViewById(R.id.user_email);
		card = (CardView)view.findViewById(R.id.cardview);
		edit_email = (Button)view.findViewById(R.id.edit_email);
		edit_password = (Button)view.findViewById(R.id.edit_password);	
		user_logout = (Button)view.findViewById(R.id.user_logout);	
		edit_email.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1) {
					userEditEmail();
				}			
			});
		edit_password.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1) {
					userEditPassword();
				}
			});
		user_logout.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1) {
					UserUtil.getCurrentUser().logOut();
					MobclickAgent.onProfileSignOff();
					m.replaceFragment(new MainLoginFragment());
				}
			});
		userHead = (ImageView)view.findViewById(R.id.user_head);
		mo_userHead = (ImageView)view.findViewById(R.id.mo_user_head);
		try {
			if (UserUtil.getCurrentUser().getUserHead().getFileUrl() != null) {
				Glide
					.with(getActivity())
					.load(UserUtil.getCurrentUser().getUserHead().getFileUrl())
					.apply(Kirby.getGlideRequestOptions())
					.into(userHead);
				GlideUtil.setBlurImageViaGlideCache(getActivity(),mo_userHead,UserUtil.getCurrentUser().getUserHead().getFileUrl(),"5");
			}
		} catch (Exception e) {}
		userName.setText(UserUtil.getCurrentUser().getUsername());
		userId.setText("id:" + UserUtil.getCurrentUser().getObjectId());
		userTime.setText(getActivity().getResources().getString(R.string.register_time) + ":" + UserUtil.getCurrentUser().getCreatedAt());
		userEmail.setText(getActivity().getResources().getString(R.string.user_email) + ":" + UserUtil.getCurrentUser().getEmail());
		changeUserHead = (RelativeLayout)view.findViewById(R.id.change_userhead);
		changeUserHead.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1) {
					Pair<View, String> userHeadPair=new Pair<View,String>(userHead, "userHead");
					Pair<View, String> cardPair= new Pair<View,String>(card, "card");
					Pair<View, String> editPassPair= new Pair<View,String>(user_logout, "pass");
					Intent intent = new Intent(getActivity(), HeadActivity.class);
					ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), userHeadPair, cardPair, editPassPair);
					startActivityForResult(intent, 3, options.toBundle());
				}
			});

		LayoutAnimationController controller = LayoutAnimationHelper.makeLayoutAnimationController();
		ViewGroup viewGroup = (ViewGroup)view.findViewById(R.id.root_view);
		viewGroup.setLayoutAnimation(controller);
		viewGroup.scheduleLayoutAnimation();
		PlayAnimUtil.playLayoutAnimation(LayoutAnimationHelper.getAnimationSetFromBottom(), false);
	}
	private void userEditEmail() {
		LayoutInflater lay_1 =getActivity().getLayoutInflater();
		final View modification_email_layout = lay_1.inflate(R.layout.dialog_modification_email, null);
		new AlertDialog.Builder(getActivity())
			.setTitle(R.string.modification_email)
			.setView(modification_email_layout) 
			.setPositiveButton(R.string.dia_yes, new
			DialogInterface.OnClickListener()
			{

				private ProgressDialog modificationEmailProgress;
				@Override
				public void onClick(DialogInterface dialog, int which) {
					modificationEmailProgress = new ProgressDialog(getActivity());
					modificationEmailProgress.setCanceledOnTouchOutside(false);
					modificationEmailProgress.setMessage(getResources().getString(R.string.modification_email));
					modificationEmailProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					modificationEmailProgress.show();
					EditText modification_email_old=(EditText)modification_email_layout.findViewById(R.id.modification_email_old);
					EditText modification_email_new=(EditText)modification_email_layout.findViewById(R.id.modification_email_new);
					String str_modification_email_old=modification_email_old.getText().toString();
					String str_modification_email_new=modification_email_new.getText().toString();
					if (str_modification_email_old.isEmpty() || str_modification_email_new.isEmpty()) {
						modificationEmailProgress.dismiss();
						Toast.makeText(getActivity(), R.string.is_null, Toast.LENGTH_SHORT).show();
					} else {
						if (EmailUtil.checkEmail(str_modification_email_old) == false || EmailUtil.checkEmail(str_modification_email_new) == false) {
							modificationEmailProgress.dismiss();
							Toast.makeText(getActivity(), R.string.email_fail, Toast.LENGTH_SHORT).show();
						} else {
							if (email.equals(str_modification_email_old)) {
								BmobUser modification_email=new BmobUser();
								modification_email.setEmail(str_modification_email_new);
								modification_email.update(id, new UpdateListener() {

										@Override
										public void done(BmobException e) {
											if (e == null) {
												modificationEmailProgress.dismiss();
												Toast.makeText(getActivity(), R.string.edit_true, Toast.LENGTH_SHORT).show();
												UserUtil.getCurrentUser().logOut();
												//finish();
												m.open();
											} else {
												modificationEmailProgress.dismiss();
												Toast.makeText(getActivity(), R.string.edit_false + e.getMessage(), Toast.LENGTH_SHORT).show();
											}
										}

									});
							} else {
								Toast.makeText(getActivity(), R.string.modification_email_false, Toast.LENGTH_SHORT).show();
							}
						}
					}
				}
			}
		)					
			.setNegativeButton(R.string.dia_cancel, null)
			.show();
	}
	private void userEditPassword() {
		LayoutInflater lay_2 =getActivity().getLayoutInflater();
		final View modification_password_layout = lay_2.inflate(R.layout.dialog_modification_password, null);
		new AlertDialog.Builder(getActivity())
			.setTitle(R.string.modification_password)
			.setView(modification_password_layout) 
			.setPositiveButton(R.string.dia_yes, new
			DialogInterface.OnClickListener()
			{

				private int text;

				private ProgressDialog changepasswordProgress;
				@Override
				public void onClick(DialogInterface dialog, int which) {
					changepasswordProgress = new ProgressDialog(getActivity());
					changepasswordProgress.setCanceledOnTouchOutside(false);
					changepasswordProgress.setMessage(getResources().getString(R.string.modification_password));
					changepasswordProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					changepasswordProgress.show();
					EditText modification_password_old=(EditText)modification_password_layout.findViewById(R.id.modification_password_old);
					EditText modification_password_new=(EditText)modification_password_layout.findViewById(R.id.modification_password_new);
					EditText modification_password_new_again=(EditText)modification_password_layout.findViewById(R.id.modification_password_new_again);
					String str_modification_password_old=modification_password_old.getText().toString();
					String str_modification_password_new=modification_password_new.getText().toString();
					String str_modification_password_new_again=modification_password_new_again.getText().toString();
					if (str_modification_password_old.isEmpty() || str_modification_password_new.isEmpty() || str_modification_password_new_again.isEmpty()) {
						changepasswordProgress.dismiss();
						Toast.makeText(getActivity(), R.string.is_null, Toast.LENGTH_SHORT).show();
					} else {
						if (str_modification_password_new.equals(str_modification_password_new_again)) {
							final BmobUser pas = new BmobUser();
							pas.updateCurrentUserPassword(str_modification_password_old, str_modification_password_new, new UpdateListener(){
									@Override
									public void done(BmobException e) {
										if (e == null) {
											changepasswordProgress.dismiss();
											Toast.makeText(getActivity(), R.string.edit_true, Toast.LENGTH_SHORT).show();
											UserUtil.getCurrentUser().logOut();
											//finish();
											m.open();
										} else {
											changepasswordProgress.dismiss();
											Toast.makeText(getActivity(), R.string.edit_false + e.getMessage(), Toast.LENGTH_SHORT).show();
										}
									}
								});
						} else {
							Toast.makeText(getActivity(), R.string.modification_password_false, Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		)					
			.setNegativeButton(R.string.dia_cancel, null)
			.show();
	}
	private class ChangeUserHeadLocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
			CHANGE_HEAD=true;
        }
    }
	@Override
	public void onResume() {
		if(CHANGE_HEAD){
			m.replaceFragment(new MainUserFragment());
		}
		super.onResume();
	}
}
