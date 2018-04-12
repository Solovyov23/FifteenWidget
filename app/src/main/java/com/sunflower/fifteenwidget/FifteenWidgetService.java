package com.sunflower.fifteenwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

/**
 * Created by SuperComputer on 3/7/2017.
 */

public class FifteenWidgetService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new gameFieldViewFactory(this.getApplicationContext(), intent);
    }
}

class gameFieldViewFactory implements RemoteViewsService.RemoteViewsFactory
{
    ArrayList<String> data = new ArrayList<>();
    Context context;
    int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public gameFieldViewFactory(Context context, Intent intent)
    {
        this.context = context;

        for(int i = 1; i <= 16; i++)
            data.add(i+"");
    }

    @Override
    public void onCreate()
    {

    }

    @Override
    public void onDataSetChanged()
    {

    }

    @Override
    public void onDestroy()
    {

    }

    @Override
    public int getCount()
    {
        return data.size();
    }

    @Override
    public RemoteViews getViewAt(int position)
    {
        // fill item with data
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.cell);
        view.setTextViewText(R.id.textView, data.get(position));

        // handler for each item
        Intent clickIntent = new Intent();
     //  clickIntent.putExtra(FifteenWidget.ITEM_POSITION, position);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        view.setOnClickFillInIntent(R.id.textView, clickIntent);

        return view;
    }

    @Override
    public RemoteViews getLoadingView()
    {
        return null;
    }

    @Override
    public int getViewTypeCount()
    {
        return 1;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public boolean hasStableIds()
    {
        return true;
    }
}
