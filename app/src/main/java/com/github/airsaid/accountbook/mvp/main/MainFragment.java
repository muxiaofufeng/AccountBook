package com.github.airsaid.accountbook.mvp.main;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.airsaid.accountbook.R;
import com.github.airsaid.accountbook.adapter.AccountListAdapter;
import com.github.airsaid.accountbook.base.BaseFragment;
import com.github.airsaid.accountbook.constants.AppConstants;
import com.github.airsaid.accountbook.constants.MsgConstants;
import com.github.airsaid.accountbook.data.Account;
import com.github.airsaid.accountbook.data.Error;
import com.github.airsaid.accountbook.util.DateUtils;
import com.github.airsaid.accountbook.util.ToastUtils;
import com.github.airsaid.accountbook.util.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author Airsaid
 * @github https://github.com/airsaid
 * @date 2017/4/6
 * @desc 首页 Fragment
 */
public class MainFragment extends BaseFragment implements MainContract.View, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.refreshLayout)
    SwipeRefreshLayout mRefreshLayout;

    private MainContract.Presenter mPresenter;
    private String mStartDate;
    private String mEndDate;

    private AccountListAdapter mAdapter;
    private int mPage = 1;

    private View mHeadView;
    private TextView mTxtTotalCost;
    private TextView mTxtTotalIncome;

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        mPresenter = presenter;
    }

    public static MainFragment newInstance(Bundle args) {
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View getLayout(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, null);
    }

    @Override
    public void onCreateFragment(@Nullable Bundle savedInstanceState) {
        initHeadView();
        initAdapter();

        Bundle arguments = getArguments();
        if (arguments != null) {
            mStartDate = arguments.getString(AppConstants.EXTRA_POSITION);
            mEndDate = DateUtils.getDateNxtMonth(mStartDate, DateUtils.FORMAT_MAIN_TAB, 1);
            onRefresh();
        }
    }

    private void initHeadView() {
        mHeadView = LayoutInflater.from(mContext).inflate(R.layout.rlv_header_main, null);
        mTxtTotalCost = (TextView) mHeadView.findViewById(R.id.txt_total_cost);
        mTxtTotalIncome = (TextView) mHeadView.findViewById(R.id.txt_total_income);
    }

    private void initAdapter() {
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeColors(UiUtils.getColor(R.color.colorAccent));

       /* mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mContext)
                .size(DimenUtils.dp2px(10f))
                .color(R.color.transparent)
                .showLastDivider()
                .build());*/
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new AccountListAdapter(new ArrayList<Account>());
        mAdapter.setHeaderView(mHeadView);
        mAdapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        mAdapter.setEmptyView(UiUtils.getEmptyView(mContext, mRecyclerView
                , UiUtils.getString(R.string.empty_account_data)));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        super.onStart();
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                mPage = 1;
                requestData();
            }
        });
    }

    /**
     * 请求数据。
     */
    private void requestData() {
        mPresenter.queryAccount(mStartDate, mEndDate, mPage);
    }

    @Override
    public void querySuccess(List<Account> list) {
        mRefreshLayout.setRefreshing(false);
        mAdapter.setNewData(mAdapter.setItemType(list));
        mAdapter.setTotalData(mTxtTotalCost, mTxtTotalIncome);
    }

    @Override
    public void queryFail(Error e) {
        ToastUtils.show(mContext, e.getMessage());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Message msg) {
        switch (msg.what) {
            case MsgConstants.MSG_SAVE_ACCOUNT_SUCCESS:
                onRefresh();
                break;
        }
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }
}