package com.eje_c.vrmultiview.controller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.eje_c.vrmultiview.common.GearVRDeviceInfo;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@EBean
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {
    private List<GearVRDeviceInfo> list = new CopyOnWriteArrayList<>();

    @RootContext
    Context context;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(DeviceListItemView_.build(context));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((DeviceListItemView) holder.itemView).bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void add(GearVRDeviceInfo gearVR) {
        list.add(gearVR);
        notifyDataSetChanged();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void remove(GearVRDeviceInfo gearVR) {
        list.remove(gearVR);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
