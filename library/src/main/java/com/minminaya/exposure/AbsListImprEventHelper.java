package com.minminaya.exposure;

import android.text.TextUtils;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.minminaya.exposure.pagestate.IPageLifeCycleObserver;
import com.minminaya.exposure.pagestate.IPageStateLifecycleOwner;
import com.minminaya.exposure.pagestate.PageLifeCycleHolder;
import com.minminaya.exposure.pagestate.PageState;
import com.minminaya.exposure.utils.ThreadHelper;
import com.minminaya.exposure.utils.UIHelper;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kotlin.Triple;


/**
 * 列表曝光帮助类基类
 * <p>
 * 支持 Item 局部曝光
 * <p>
 * Author: minminaya  😊😊😊
 * Email: minminaya@gmail.com
 * Date: 2022/3/16 20:40
 */
public abstract class AbsListImprEventHelper<L extends RecyclerView.Adapter<?>, T extends IEntityForImpr>
        implements
        IListImpEventHelper,
        IPageLifeCycleObserver, RecyclerView.OnChildAttachStateChangeListener {

    /**
     * 是否已经添加过ChildAttachStateChangeListener
     */
    private boolean isAddOnChildAttachStateChangeListener = false;

    private final Map<String, Triple<T, Integer, Integer>> mPostEventDataHashMap = new LinkedHashMap<>();

    protected final RecyclerView mRecyclerView;

    /**
     * 发送事件的Runnable
     */
    private final Runnable mPostEventRunnable = this::postEvent;

    private Class<L> mRecyclerViewAdapterClass = null;

    /**
     * 上报事件停留时间
     */
    private static final long POST_EVENT_DEBOUNCE = 600L;


    protected AbsListImprEventHelper(@NonNull RecyclerView recyclerView,
                                     @NonNull ComponentActivity componentActivity) {
        this.mRecyclerView = recyclerView;
        PageLifeCycleHolder pageLifeCycleHolder;
        if (componentActivity instanceof IPageStateLifecycleOwner) {
            IPageStateLifecycleOwner pageStateLifecycleOwner = (IPageStateLifecycleOwner) componentActivity;
            pageLifeCycleHolder = pageStateLifecycleOwner.getPageStateLifecycle();
        } else {
            throw new RuntimeException(
                    "please add below classpath to build.gradle at project root.\n" +
                            "\"com.didiglobal.booster:booster-gradle-plugin:{booster-gradle-plugin-version}\"\n" +
                            ",\"com.minminaya:exposure-plugin:{exposure-plugin-version}\"");
        }
        init(pageLifeCycleHolder);
    }

    protected AbsListImprEventHelper(
            @NonNull RecyclerView recyclerView,
            @NonNull Fragment fragment) {
        this.mRecyclerView = recyclerView;
        PageLifeCycleHolder pageLifeCycleHolder;
        if (fragment instanceof IPageStateLifecycleOwner) {
            IPageStateLifecycleOwner pageStateLifecycleOwner = (IPageStateLifecycleOwner) fragment;
            pageLifeCycleHolder = pageStateLifecycleOwner.getPageStateLifecycle();
        } else {
            throw new RuntimeException(
                    "please add below classpath to build.gradle at project root.\n" +
                            "\"com.didiglobal.booster:booster-gradle-plugin:{booster-gradle-plugin-version}\"\n" +
                            ",\"com.minminaya:exposure-plugin:{exposure-plugin-version}\"");
        }
        init(pageLifeCycleHolder);
    }

    private void init(@NonNull PageLifeCycleHolder pageLifeCycleHolder) {
        pageLifeCycleHolder.addPageObserver(this);
        if (pageLifeCycleHolder.getPageState() == PageState.VISIBLE) {
            onPageStart();
            checkAndPostEvent(mRecyclerView);
        }
    }

    @NonNull
    public RecyclerView getRecyclerView() {
        return this.mRecyclerView;
    }

    public void checkAndPostEvent(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        int newFirstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        int newLastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();

        if (newFirstVisibleItemPosition == -1 || newLastVisibleItemPosition == -1) {
            return;
        }

        for (int i = newFirstVisibleItemPosition; i <= newLastVisibleItemPosition; i++) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
            if (viewHolder == null) {
                continue;
            }
            int bindingAdapterPosition = viewHolder.getBindingAdapterPosition() - getHeaderPositionCount();
            int absoluteAdapterPosition = viewHolder.getAbsoluteAdapterPosition() - getHeaderPositionCount();
            if (bindingAdapterPosition < 0 || absoluteAdapterPosition < 0 || !isBindingAdapter(viewHolder)) {
                continue;
            }
            T entity = getAdapterEntityForPosition(bindingAdapterPosition, viewHolder);
            if (entity != null && needPostEvent(entity)) {
                putEntity(entity, absoluteAdapterPosition, bindingAdapterPosition);
            }
        }
    }

    /**
     * @param entity                  entity
     * @param absoluteAdapterPosition 相对RecycleView的位置
     */
    private void putEntity(@NonNull T entity, int absoluteAdapterPosition, int bindingAdapterPosition) {
        String id = entity.getIdForImpr();
//        Log.d(TAG, "putEntity--id:" + id + ", bindingAdapterPosition:" + bindingAdapterPosition);
        if (TextUtils.isEmpty(id)) {
            return;
        }
        mPostEventDataHashMap.put(id, new Triple<>(entity, absoluteAdapterPosition, bindingAdapterPosition));
        UIHelper.removeCallback(mPostEventRunnable);
        UIHelper.runOnUiThreadDelay(mPostEventRunnable, POST_EVENT_DEBOUNCE);
    }

    /**
     * @param entity entity
     */
    private void removeEntity(T entity) {
        mPostEventDataHashMap.remove(entity.getIdForImpr());
        UIHelper.removeCallback(mPostEventRunnable);
        UIHelper.runOnUiThreadDelay(mPostEventRunnable, POST_EVENT_DEBOUNCE);
    }

    @Override
    public void onDestroy() {
        UIHelper.removeCallback(mPostEventRunnable);
    }

    private void postEvent() {
        Map<String, Triple<T, Integer, Integer>> backupMap = new LinkedHashMap<>(mPostEventDataHashMap);
        mPostEventDataHashMap.clear();
        ThreadHelper.executeExposureSingleTask(() -> {
            List<Triple<T, Integer, Integer>> tripleList = new ArrayList<>();

            for (Map.Entry<String, Triple<T, Integer, Integer>> stringPairEntry : backupMap.entrySet()) {
                Triple<T, Integer, Integer> value = stringPairEntry.getValue();
                T entity = value.getFirst();
                if (entity == null) {
                    continue;
                }
                //单个曝光
                onItemExposure(entity, value.getSecond(), value.getThird());
                tripleList.add(value);
            }
            //批量曝光
            onBatchItemExposure(tripleList);
        });
    }

    private void onPageStop() {
        if (mRecyclerView != null) {
            isAddOnChildAttachStateChangeListener = false;
            mRecyclerView.removeOnChildAttachStateChangeListener(this);
        }
    }

    private void onPageStart() {
        if (mRecyclerView != null && !isAddOnChildAttachStateChangeListener) {
            isAddOnChildAttachStateChangeListener = true;
            mRecyclerView.addOnChildAttachStateChangeListener(this);
        }
    }

    @Override
    public void onPageVisible() {
        onPageStart();
        checkAndPostEvent(mRecyclerView);
    }

    @Override
    public void onPageInvisible() {
        onPageStop();
    }

    /**
     * 是否需要统计曝光事件
     *
     * @param entity entity
     */
    public abstract boolean needPostEvent(@NonNull T entity);

    /**
     * @return 提供待统计的目标Sub Adapter class类型
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public Class<?> getRecyclerViewSubAdapterClazz() {
        if (mRecyclerViewAdapterClass != null) {
            return mRecyclerViewAdapterClass;
        }
        Type type = getClass().getGenericSuperclass();
        try {
            Type[] parameter = ((ParameterizedType) type).getActualTypeArguments();
            mRecyclerViewAdapterClass = (Class<L>) parameter[0];
            return mRecyclerViewAdapterClass;
        } catch (Exception exception) {
            exception.printStackTrace();
            return Object.class;
        }
    }

    /**
     * 抽象提供 Adapter 中数据集合对象
     *
     * @param bindingAdapterPosition sub Adapter中的位置
     * @param viewHolder             viewHolder
     */
    @Nullable
    public abstract T getAdapterEntityForPosition(int bindingAdapterPosition, @NonNull RecyclerView.ViewHolder viewHolder);

    private boolean isBindingAdapter(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder == null) {
            return false;
        }
        return viewHolder.getBindingAdapter() != null
                && viewHolder.getBindingAdapter().getClass() == getRecyclerViewSubAdapterClazz();
    }

    /**
     * 当bindingAdapterPosition项曝光的时候回调
     *
     * @param entity                  entity
     * @param absoluteAdapterPosition 相对 RecyclerView 的 item position
     * @param bindingAdapterPosition  相对子 Adapter 级别 item position
     */
    @WorkerThread
    public abstract void onItemExposure(@NonNull T entity, int absoluteAdapterPosition, int bindingAdapterPosition);

    /**
     * 可见项批量曝光回调
     *
     * @param tripleList 包含entity absoluteAdapterPosition bindingAdapterPosition的数据类
     * @apiNote entity                  entity
     * @apiNote absoluteAdapterPosition 相对 RecyclerView 的 item position
     * @apiNote bindingAdapterPosition  相对子 Adapter级别 item position
     */
    @WorkerThread
    public void onBatchItemExposure(@NonNull List<Triple<T, Integer, Integer>> tripleList) {

    }


    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
        if (mRecyclerView == null) {
            return;
        }
        RecyclerView.ViewHolder viewHolder = mRecyclerView.findContainingViewHolder(view);
        if (viewHolder == null) {
            return;
        }
        int bindingAdapterPosition = -1;

        try {
            bindingAdapterPosition = viewHolder.getBindingAdapterPosition() - getHeaderPositionCount();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        int absoluteAdapterPosition = viewHolder.getAbsoluteAdapterPosition() - getHeaderPositionCount();
        if (bindingAdapterPosition < 0 || absoluteAdapterPosition < 0) {
            return;
        }

        if (isBindingAdapter(viewHolder)) {
            T entity = getAdapterEntityForPosition(bindingAdapterPosition, viewHolder);
            if (entity == null) {
                return;
            }
            if (needPostEvent(entity)) {
                putEntity(entity, absoluteAdapterPosition, bindingAdapterPosition);
            }
        }
    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
        if (mRecyclerView == null) {
            return;
        }
        RecyclerView.ViewHolder viewHolder = mRecyclerView.findContainingViewHolder(view);
        if (viewHolder == null) {
            return;
        }
        int bindingAdapterPosition = viewHolder.getBindingAdapterPosition() - getHeaderPositionCount();
        int absoluteAdapterPosition = viewHolder.getAbsoluteAdapterPosition() - getHeaderPositionCount();
        if (bindingAdapterPosition < 0 || absoluteAdapterPosition < 0) {
            return;
        }

        if (isBindingAdapter(viewHolder)) {
            T entity = getAdapterEntityForPosition(bindingAdapterPosition, viewHolder);
            if (entity == null) {
                return;
            }
            removeEntity(entity);
        }
    }

    @Override
    public int getHeaderPositionCount() {
        return 0;
    }
}
