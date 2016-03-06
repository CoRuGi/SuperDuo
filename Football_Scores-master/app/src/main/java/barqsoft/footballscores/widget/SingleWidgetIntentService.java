package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

public class SingleWidgetIntentService extends IntentService {
    private final static String LOG_TAG = SingleWidgetIntentService.class.getSimpleName();

    private static final String[] SCORES_COLUMNS = {
        DatabaseContract.scores_table.TIME_COL,
        DatabaseContract.scores_table.HOME_COL,
        DatabaseContract.scores_table.AWAY_COL,
        DatabaseContract.scores_table.HOME_GOALS_COL,
        DatabaseContract.scores_table.AWAY_GOALS_COL
    };

    private static final int INDEX_TIME_COL = 0;
    private static final int INDEX_HOME_COL = 1;
    private static final int INDEX_AWAY_COL = 2;
    private static final int INDEX_HOME_GOALS_COL = 3;
    private static final int INDEX_AWAY_GOALS_COL = 4;

    public SingleWidgetIntentService() {
        super("SingleWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "SingleWidgetIntentService has been called!");

        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                SingleWidgetProvider.class));

        Uri scoresByDateUri = DatabaseContract.scores_table.buildScoreWithDate();

        Date fragmentdate = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");

        Cursor data = getContentResolver().query(
                scoresByDateUri,
                SCORES_COLUMNS,
                //DatabaseContract.scores_table.DATE_COL + "= ?",
                null,
                new String[] {mformat.format(fragmentdate)},
                null
        );
        if (data == null) {
            Log.d(LOG_TAG, "Cursor returned null!");
            return;
        }
        if (!data.moveToFirst()) {
            Log.d(LOG_TAG, "Cursor was empty!");
            data.close();
            return;
        }

        String time = data.getString(INDEX_TIME_COL);
        String homeTeam = data.getString(INDEX_HOME_COL);
        int homeGoals = data.getInt(INDEX_HOME_GOALS_COL);
        String awayTeam = data.getString(INDEX_AWAY_COL);
        int awayGoals = data.getInt(INDEX_AWAY_GOALS_COL);

        Log.d(LOG_TAG, "Found: " + homeTeam + homeGoals + awayTeam + awayGoals);

        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_single);
            remoteViews.setTextViewText(R.id.home_name, homeTeam);
            remoteViews.setTextViewText(R.id.away_name, awayTeam);
            remoteViews.setTextViewText(R.id.data_textview, time);
            remoteViews.setTextViewText(
                    R.id.score_textview,
                    Utilies.getScores(homeGoals, awayGoals)
            );
//            remoteViews.setImageViewResource(
//                    R.id.home_crest,
//                    Utilies.getTeamCrestByTeamName(homeTeam)
//            );
//            remoteViews.setImageViewResource(
//                    R.id.away_crest,
//                    Utilies.getTeamCrestByTeamName(awayTeam)
//            );

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
