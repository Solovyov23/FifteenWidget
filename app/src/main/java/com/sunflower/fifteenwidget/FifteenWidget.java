package com.sunflower.fifteenwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.util.HashMap;

/**
 * Implementation of App Widget functionality.
 */
public class FifteenWidget extends AppWidgetProvider
{
    final static String ACTION_CLICK = "click";
    final static String ACTION_RESTART_CLICK = "restart_click";
    final static String CELL_NUM = "item_position";
    private static final String LOG_TAG = "Fifteen Game Widget";

    // keeps status of every game
    private static HashMap<Integer, FifteenGame> gameStats = new HashMap<>();

    private static final int[] button_ids = new int[]{
            R.id.first, R.id.second, R.id.third, R.id.fourth,
            R.id.fifth, R.id.sixth, R.id.seventh, R.id.eight,
            R.id.ninth, R.id.tenth, R.id.eleventh, R.id.twelfth,
            R.id.thirteenth, R.id.fourteenth, R.id.fifteenth, R.id.sixteenth};

    static void updateAppWidget(final Context context, AppWidgetManager appWidgetManager, final int appWidgetId)
    {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fifteen_widget);

        // the game itself
        final FifteenGame game = new FifteenGame();
        game.addListener(new FifteenGame.FifteenGameListener()
        {
            @Override
            public void cellsSwapped(FifteenGame source, SuperPoint first, SuperPoint second)
            {
                // redraw swapped cells
                drawCell(first.x, first.y, false);
                drawCell(second.x, second.y, false);
            }

            @Override
            public void fieldUpdated(FifteenGame source)
            {
                drawGameField();
            }

            @Override
            public void gameStateChanged(FifteenGame source, FifteenGame.GameState newState)
            {
                if(newState == FifteenGame.GameState.InTheProcess)
                {
                    showHideTextView(false, "");
                    showHideRestartView(true);
                    // refresh status bar
                   // updateStatusViewText(context.getResources().getString(R.string.ticTacToe) + ": " + game.getTurn() );
                    drawGameField();
                }
                else if(newState == FifteenGame.GameState.Ended)
                {
                   // updateStatusViewText(context.getResources().getString(R.string.ticTacToe));
                    showHideRestartView(false);
                    showHideTextView(true, "You are the winner!");
                    Log.d(LOG_TAG, "Game ended!!!");
                }
                else if(newState == FifteenGame.GameState.ForceEnd)
                {
                    // updateStatusViewText(context.getResources().getString(R.string.ticTacToe));
                    showHideRestartView(false);
                    showHideTextView(true, "Game ended!");
                    Log.d(LOG_TAG, "Force end!");
                }
            }

            void showHideTextView(boolean show, String text)
            {
                views.setViewVisibility(R.id.mainTextView, show? View.VISIBLE: View.GONE);

                if(show) views.setTextViewText(R.id.mainTextView, text);

                // update views
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

            void showHideRestartView(boolean show)
            {
                views.setViewVisibility(R.id.restart, show? View.VISIBLE: View.INVISIBLE);

                // update views
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

            void drawGameField()
            {
                GameBoard gameField = game.getGameField();
                int fieldWidth = game.getFieldWidth();
                int fieldHeight = game.getFieldHeight();

                // redraw the board
                for(int x = 0; x < fieldWidth; x++)
                {
                    for(int y = 0; y < fieldHeight; y++)
                    {
                        drawCell(x, y, true);
                    }
                }

                // update views
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

            void drawCell(int x, int y, boolean deferredDraw)
            {
                int pos = x + game.getFieldWidth() * y;
                int number = game.getGameField().get(x, y);

                if (number != 0)
                {
                    String text =  game.getGameField().get(x, y) + "";
                    views.setTextViewText(button_ids[pos], text);
                    views.setViewVisibility(button_ids[pos], View.VISIBLE);
                }
                else
                {
                    views.setTextViewText(button_ids[pos], "");
                    views.setViewVisibility(button_ids[pos], View.INVISIBLE);
                }

                if(!deferredDraw)
                {
                    // update views
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
        });

        gameStats.put(appWidgetId, game);

        // loop through the buttons and set click intents
        for(int i = 0; i < game.getFieldWidth() * game.getFieldHeight(); i++)
        {
            int id = button_ids[i];

            Intent clickIntent = new Intent(context, FifteenWidget.class);

            clickIntent.setAction(id+"");
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            clickIntent.putExtra(CELL_NUM, i);

            PendingIntent pIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, 0);
            views.setOnClickPendingIntent(id, pIntent);
        }

        // --- start click-------------
        Intent clickIntent = new Intent(context, FifteenWidget.class);

        clickIntent.setAction(ACTION_CLICK);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        PendingIntent pIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, 0);
        views.setOnClickPendingIntent(R.id.mainTextView, pIntent);

        // ----restart click------------
        Intent restartIntent = new Intent(context, FifteenWidget.class);

        restartIntent.setAction(ACTION_RESTART_CLICK);
        restartIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        pIntent = PendingIntent.getBroadcast(context, appWidgetId, restartIntent, 0);
        views.setOnClickPendingIntent(R.id.restart, pIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds)
        {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context)
    {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context)
    {
        // Enter relevant functionality for when the last widget is disabled
    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        // remove game stats from the list
        for(int id: appWidgetIds)
        {
            gameStats.remove(id);
            Log.d(LOG_TAG, id + " Deleted!");
        }
    }

    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);

        int cellId = -1;
        for(int id: button_ids)
        {
            if (intent.getAction().equalsIgnoreCase(id+""))
            {
                cellId = id;
            }
        }

        // a button has been clicked
        if(cellId != -1)
        {
            Bundle extras = intent.getExtras();

            if (extras == null) return;

            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            int cellPos = extras.getInt(CELL_NUM, -1);

            FifteenGame game = gameStats.get(appWidgetId);

            int x = cellPos % game.getFieldWidth();
            int y = cellPos / game.getFieldWidth();

            //Log.e(LOG_TAG, "X: " + x + ", Y: " + y);

            // make a move
            game.tryToMoveBlankTo(new SuperPoint(x, y));
        }
        else if(intent.getAction().equalsIgnoreCase(ACTION_CLICK))
        {
            Bundle extras = intent.getExtras();
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            FifteenGame game = gameStats.get(appWidgetId);
            game.startTheGame();

            //Log.e(LOG_TAG, "Text Click");
        }
        else if(intent.getAction().equalsIgnoreCase(ACTION_RESTART_CLICK))
        {
            Bundle extras = intent.getExtras();
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            FifteenGame game = gameStats.get(appWidgetId);
            game.reset();
            game.startTheGame();
        }
    }
}

