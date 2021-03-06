/*
 * Copyright (C) 2015 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hippo.ehviewer.Constants;
import com.hippo.ehviewer.R;
import com.hippo.ehviewer.util.Config;
import com.hippo.rippleold.RippleSalon;
import com.hippo.util.FrescoUtils;
import com.hippo.util.Messenger;
import com.hippo.widget.DrawerListView;
import com.hippo.widget.FitPaddingImpl;

public class DrawerLeftPanel extends LinearLayout implements FitPaddingImpl {

    private static final String STATE_KEY_SUPER = "super";
    private static final String STATE_KEY_DRAWER_LIST_VIEW = "drawer_list_view";

    private static final int[] MAX_ATTRS = {android.R.attr.maxWidth};

    private static Uri DEFAULT_AVATAR_URI = FrescoUtils.getResourcesDrawableUri(R.drawable.default_avatar);

    private int mMaxWidth = -1;

    private ViewGroup mSuperUserPanel;
    private View mKKStatusBarBg;
    private ViewGroup mUserPanel;
    private SimpleDraweeView mAvatar;
    private TextView mUsename;
    private TextView mAction;
    private DrawerListView mDrawerListView;

    private int mFitPaddingTop;
    private int mFitPaddingBottom;

    private int mSuperUserPanelHeight;
    private int mUserPanelOriginalPaddingTop;
    private int mDrawerListViewOriginalPaddingBottom;

    private boolean mSignIn;

    private Helper mHelper;

    private Messenger.Receiver mMessengerReceiver = new Messenger.Receiver() {
        @Override
        public void onReceive(int id, Object obj) {
            if (id == Constants.MESSENGER_ID_SIGN_IN_OR_OUT) {
                if (obj == null) {
                    // Sign out
                    mSignIn = false;
                    mAvatar.setImageURI(DEFAULT_AVATAR_URI);
                    mUsename.setText("");
                    mAction.setText(getContext().getString(R.string.signin));
                } else if (obj instanceof String) {
                    // Sign in
                    mSignIn = true;
                    mUsename.setText((String) obj);
                    mAction.setText(getContext().getString(R.string.signout));
                }
            } else if (id == Constants.MESSENGER_ID_USER_AVATAR) {
                if (mSignIn && obj instanceof Uri) {
                    mAvatar.setImageURI((Uri) obj);
                }
            }
        }
    };

    public DrawerLeftPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DrawerLeftPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, MAX_ATTRS);
        mMaxWidth = a.getDimensionPixelSize(0, -1);
        a.recycle();

        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(context).inflate(R.layout.widget_drawer_left_panel, this);

        mSuperUserPanel = (ViewGroup) getChildAt(0);
        mKKStatusBarBg = mSuperUserPanel.getChildAt(1);
        mUserPanel = (ViewGroup) mSuperUserPanel.getChildAt(2);
        mAvatar = (SimpleDraweeView) mUserPanel.getChildAt(0);
        mUsename = (TextView) mUserPanel.getChildAt(1);
        mAction = (TextView) mUserPanel.getChildAt(2);
        mDrawerListView = (DrawerListView) getChildAt(1);

        RippleSalon.addRipple(mAction, false);
        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHelper != null) {
                    if (mSignIn) {
                        mHelper.onClickSignOut();
                    } else {
                        mHelper.onClickSignIn();
                    }
                }
            }
        });

        mSuperUserPanelHeight = mSuperUserPanel.getLayoutParams().height;
        mUserPanelOriginalPaddingTop = mUserPanel.getPaddingTop();
        mDrawerListViewOriginalPaddingBottom = mDrawerListView.getPaddingBottom();

        mSignIn = Config.getSignIn();
        if (mSignIn) {
            // TODO
            mAvatar.setImageURI(DEFAULT_AVATAR_URI);
            mUsename.setText(Config.getDisplayName());
            mAction.setText(getContext().getString(R.string.signout));
        } else {
            mAvatar.setImageURI(DEFAULT_AVATAR_URI);
            mUsename.setText("");
            mAction.setText(getContext().getString(R.string.signin));
        }
    }

    public DrawerListView getDrawerListView() {
        return mDrawerListView;
    }

    public void setHelper(Helper helper) {
        mHelper = helper;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        Messenger.getInstance().register(Constants.MESSENGER_ID_SIGN_IN_OR_OUT, mMessengerReceiver);
        Messenger.getInstance().register(Constants.MESSENGER_ID_USER_AVATAR, mMessengerReceiver);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        Messenger.getInstance().unregister(Constants.MESSENGER_ID_SIGN_IN_OR_OUT, mMessengerReceiver);
        Messenger.getInstance().unregister(Constants.MESSENGER_ID_USER_AVATAR, mMessengerReceiver);
    }



    @Override
    public Parcelable onSaveInstanceState() {
        final Bundle state = new Bundle();
        state.putParcelable(STATE_KEY_SUPER, super.onSaveInstanceState());
        state.putParcelable(STATE_KEY_DRAWER_LIST_VIEW, mDrawerListView.onSaveInstanceState());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle savedState = (Bundle) state;
            super.onRestoreInstanceState(savedState.getParcelable(STATE_KEY_SUPER));
            mDrawerListView.onRestoreInstanceState(savedState.getParcelable(STATE_KEY_DRAWER_LIST_VIEW));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        if (mMaxWidth > 0) {
            widthSpecSize = Math.min(mMaxWidth, widthSpecSize);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSpecSize,
                    MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onFitPadding(int left, int top, int right, int bottom) {
        if (mFitPaddingTop != top) {
            mFitPaddingTop = top;

            ViewGroup.LayoutParams lp;

            lp = mKKStatusBarBg.getLayoutParams();
            lp.height = top;
            mKKStatusBarBg.setLayoutParams(lp);

            lp = mSuperUserPanel.getLayoutParams();
            lp.height = mSuperUserPanelHeight + top;
            mSuperUserPanel.setLayoutParams(lp);

            mUserPanel.setPadding(mUserPanel.getPaddingLeft(),
                    mUserPanelOriginalPaddingTop + top,
                    mUserPanel.getPaddingRight(),
                    mUserPanel.getPaddingBottom());
        }

        if (mFitPaddingBottom != bottom) {
            mFitPaddingBottom = bottom;

            mDrawerListView.setPadding(mDrawerListView.getPaddingLeft(),
                    mDrawerListView.getPaddingTop(),
                    mDrawerListView.getPaddingRight(),
                    mDrawerListViewOriginalPaddingBottom + bottom);
        }
    }

    public interface Helper {
        void onClickSignIn();
        void onClickSignOut();
    }
}
