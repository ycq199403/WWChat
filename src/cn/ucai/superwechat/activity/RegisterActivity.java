/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.listener.OnSetAvatarListener;

import com.easemob.exceptions.EaseMobException;

/**
 * 注册页
 * 
 */
public class RegisterActivity extends BaseActivity {
    private final static String TAG = RegisterActivity.class.getName();
    Activity mContext;
    EditText userNickEditText;
    EditText userNameEditText;
    EditText passwordEditText;
    EditText confirmPwdEditText;
     ImageView mIVAvatar;
    OnSetAvatarListener mOnSetAvatarListener;
    String avatarUserName;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(cn.ucai.superwechat.R.layout.activity_register);
        mContext = this;
        initView();
        setListener();
    }

    private void setListener() {
        onSetRegisterListener();
        onLoginClickListener();
        onSetAvatarListener();
    }

    private void onSetAvatarListener() {
        findViewById(R.id.layout_user_avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnSetAvatarListener = new OnSetAvatarListener(mContext, R.id.layout_register, getAvatarName(), I.AVATAR_TYPE_USER_PATH);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mOnSetAvatarListener.setAvatar(requestCode, data, mIVAvatar);
        }
    }

    private String getAvatarName() {
        avatarUserName = System.currentTimeMillis() + "";
        return avatarUserName;
    }

    private void onLoginClickListener() {
        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
		userNameEditText = (EditText) findViewById(cn.ucai.superwechat.R.id.username);
		passwordEditText = (EditText) findViewById(cn.ucai.superwechat.R.id.password);
        userNickEditText = (EditText) findViewById(R.id.etNick);
		confirmPwdEditText = (EditText) findViewById(cn.ucai.superwechat.R.id.confirm_password);
        mIVAvatar = (ImageView) findViewById(R.id.iv_avatar);
    }

	/**
	 * 注册
	 * 
	 * @param
	 */
	public void onSetRegisterListener() {
        findViewById(R.id.btnRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = userNameEditText.getText().toString().trim();
                final String pwd = passwordEditText.getText().toString().trim();
                String confirm_pwd = confirmPwdEditText.getText().toString().trim();
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(mContext, getResources().getString(cn.ucai.superwechat.R.string.User_name_cannot_be_empty), Toast.LENGTH_SHORT).show();
                    userNameEditText.requestFocus();
                    return;
                } else if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(mContext, getResources().getString(cn.ucai.superwechat.R.string.Password_cannot_be_empty), Toast.LENGTH_SHORT).show();
                    passwordEditText.requestFocus();
                    return;
                } else if (TextUtils.isEmpty(confirm_pwd)) {
                    Toast.makeText(mContext, getResources().getString(cn.ucai.superwechat.R.string.Confirm_password_cannot_be_empty), Toast.LENGTH_SHORT).show();
                    confirmPwdEditText.requestFocus();
                    return;
                } else if (!pwd.equals(confirm_pwd)) {
                    Toast.makeText(mContext, getResources().getString(cn.ucai.superwechat.R.string.Two_input_password), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
                    final ProgressDialog pd = new ProgressDialog(mContext);
                    pd.setMessage(getResources().getString(cn.ucai.superwechat.R.string.Is_the_registered));
                    pd.show();

                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                // 调用sdk注册方法
                                EMChatManager.getInstance().createAccountOnServer(username, pwd);
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (!RegisterActivity.this.isFinishing())
                                            pd.dismiss();
                                        // 保存用户名
                                        SuperWeChatApplication.getInstance().setUserName(username);
                                        Toast.makeText(getApplicationContext(), getResources().getString(cn.ucai.superwechat.R.string.Registered_successfully), Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            } catch (final EaseMobException e) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (!RegisterActivity.this.isFinishing())
                                            pd.dismiss();
                                        int errorCode=e.getErrorCode();
                                        if(errorCode==EMError.NONETWORK_ERROR){
                                            Toast.makeText(getApplicationContext(), getResources().getString(cn.ucai.superwechat.R.string.network_anomalies), Toast.LENGTH_SHORT).show();
                                        }else if(errorCode == EMError.USER_ALREADY_EXISTS){
                                            Toast.makeText(getApplicationContext(), getResources().getString(cn.ucai.superwechat.R.string.User_already_exists), Toast.LENGTH_SHORT).show();
                                        }else if(errorCode == EMError.UNAUTHORIZED){
                                            Toast.makeText(getApplicationContext(), getResources().getString(cn.ucai.superwechat.R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
                                        }else if(errorCode == EMError.ILLEGAL_USER_NAME){
                                            Toast.makeText(getApplicationContext(), getResources().getString(cn.ucai.superwechat.R.string.illegal_user_name),Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(getApplicationContext(), getResources().getString(cn.ucai.superwechat.R.string.Registration_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }).start();

                }

            }
        });

	}

	public void back(View view) {
		finish();
	}

}
