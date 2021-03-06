package com.wangdaye.mysplash.user.presenter.widget;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;

import com.wangdaye.mysplash.Mysplash;
import com.wangdaye.mysplash.R;
import com.wangdaye.mysplash._common.data.data.Collection;
import com.wangdaye.mysplash._common.data.data.User;
import com.wangdaye.mysplash._common.data.service.CollectionService;
import com.wangdaye.mysplash._common.i.model.CollectionsModel;
import com.wangdaye.mysplash._common.i.presenter.CollectionsPresenter;
import com.wangdaye.mysplash._common.i.view.CollectionsView;
import com.wangdaye.mysplash._common.ui.dialog.RateLimitDialog;
import com.wangdaye.mysplash._common.utils.NotificationUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Collections implementor.
 * */

public class CollectionsImplementor
        implements CollectionsPresenter {
    // model & view.
    private CollectionsModel model;
    private CollectionsView view;

    /** <br> life cycle. */

    public CollectionsImplementor(CollectionsModel model, CollectionsView view) {
        this.model = model;
        this.view = view;
    }

    /** <br> presenter. */

    @Override
    public void requestCollections(Context c, int page, boolean refresh) {
        if (!model.isRefreshing() && !model.isLoading()) {
            if (refresh) {
                model.setRefreshing(true);
            } else {
                model.setLoading(true);
            }
            page = refresh ? 1 : page + 1;
            model.getService()
                    .requestUserCollections(
                            (User) model.getRequestKey(),
                            page,
                            Mysplash.DEFAULT_PER_PAGE,
                            new OnRequestCollectionsListener(c, page, refresh));
        }
    }

    @Override
    public void cancelRequest() {
        model.getService().cancel();
    }

    @Override
    public void refreshNew(Context c, boolean notify) {
        if (notify) {
            view.setRefreshing(true);
        }
        requestCollections(c, model.getCollectionsPage(), true);
    }

    @Override
    public void loadMore(Context c, boolean notify) {
        if (notify) {
            view.setLoading(true);
        }
        requestCollections(c, model.getCollectionsPage(), false);
    }

    @Override
    public void initRefresh(Context c) {
        model.getService().cancel();
        model.setRefreshing(false);
        model.setLoading(false);
        refreshNew(c, false);
        view.initRefreshStart();
    }

    @Override
    public boolean canLoadMore() {
        return !model.isRefreshing() && !model.isLoading() && !model.isOver();
    }

    @Override
    public boolean isRefreshing() {
        return model.isRefreshing();
    }

    @Override
    public boolean isLoading() {
        return model.isLoading();
    }

    @Override
    public Object getRequestKey() {
        return model.getRequestKey();
    }

    @Override
    public void setRequestKey(Object k) {
        model.setRequestKey(k);
    }

    @Override
    public void setType(String key) {
        // do nothing.
    }

    @Override
    public void setActivityForAdapter(Activity a) {
        model.getAdapter().setActivity(a);
    }

    @Override
    public int getAdapterItemCount() {
        return model.getAdapter().getRealItemCount();
    }

    /** <br> interface. */

    private class OnRequestCollectionsListener implements CollectionService.OnRequestCollectionsListener {
        // data
        private Context c;
        private int page;
        private boolean refresh;

        public OnRequestCollectionsListener(Context c, int page, boolean refresh) {
            this.c = c;
            this.page = page;
            this.refresh = refresh;
        }

        @Override
        public void onRequestCollectionsSuccess(Call<List<Collection>> call, Response<List<Collection>> response) {
            model.setRefreshing(false);
            model.setLoading(false);
            if (refresh) {
                model.getAdapter().clearItem();
                model.setOver(false);
                view.setRefreshing(false);
                view.setPermitLoading(true);
            } else {
                view.setLoading(false);
            }
            if (response.isSuccessful()) {
                model.setCollectionsPage(page);
                for (int i = 0; i < response.body().size(); i ++) {
                    model.getAdapter().insertItem(response.body().get(i), 0);
                }
                if (response.body().size() < Mysplash.DEFAULT_PER_PAGE) {
                    model.setOver(true);
                    view.setPermitLoading(false);
                    if (response.body().size() == 0) {
                        NotificationUtils.showSnackbar(
                                c.getString(R.string.feedback_is_over),
                                Snackbar.LENGTH_SHORT);
                    }
                }
                view.requestCollectionsSuccess();
            } else {
                view.requestCollectionsFailed(c.getString(R.string.feedback_load_nothing_tv));
                RateLimitDialog.checkAndNotify(
                        Mysplash.getInstance().getActivityList().get(
                                Mysplash.getInstance().getActivityList().size() - 1),
                        response.headers().get("X-Ratelimit-Remaining"));
            }
        }

        @Override
        public void onRequestCollectionsFailed(Call<List<Collection>> call, Throwable t) {
            model.setRefreshing(false);
            model.setLoading(false);
            if (refresh) {
                view.setRefreshing(false);
            } else {
                view.setLoading(false);
            }
            NotificationUtils.showSnackbar(
                    c.getString(R.string.feedback_load_failed_toast) + " (" + t.getMessage() + ")",
                    Snackbar.LENGTH_SHORT);
            view.requestCollectionsFailed(c.getString(R.string.feedback_load_failed_tv));
        }
    }
}
