package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ListWidgetRemoteViewsService extends RemoteViewsService {
    public final static String LOG_TAG = ListWidgetRemoteViewsService.class.getSimpleName();

    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID
    };

    private static final int INDEX_TIME_COL = 0;
    private static final int INDEX_HOME_COL = 1;
    private static final int INDEX_AWAY_COL = 2;
    private static final int INDEX_HOME_GOALS_COL = 3;
    private static final int INDEX_AWAY_GOALS_COL = 4;
    private static final int INDEX_MATCH_ID = 5;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                // As our ContentProvider is not exported we need to clear the calling identity
                final long identityToken = Binder.clearCallingIdentity();

                Uri scoresByDateUri = DatabaseContract.scores_table.buildScoreWithDate();

                Date fragmentdate = new Date(System.currentTimeMillis());
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");

                data = getContentResolver().query(
                        scoresByDateUri,
                        SCORES_COLUMNS,
                        //DatabaseContract.scores_table.DATE_COL + "= ?",
                        null,
                        new String[]{mformat.format(fragmentdate)},
                        null
                );

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews remoteViews = new RemoteViews(
                        getPackageName(), R.layout.widget_list_item
                );
                remoteViews.setTextViewText(R.id.home_name, data.getString(INDEX_HOME_COL));
                remoteViews.setTextViewText(R.id.away_name, data.getString(INDEX_AWAY_COL));
                remoteViews.setTextViewText(R.id.data_textview, data.getString(INDEX_TIME_COL));
                remoteViews.setTextViewText(
                        R.id.score_textview,
                        Utilies.getScores(
                                data.getInt(INDEX_HOME_GOALS_COL), data.getInt(INDEX_AWAY_GOALS_COL)
                        )
                );
                remoteViews.setImageViewResource(
                        R.id.home_crest,
                        Utilies.getTeamCrestByTeamName(data.getString(INDEX_HOME_COL))
                );
                remoteViews.setImageViewResource(
                        R.id.away_crest,
                        Utilies.getTeamCrestByTeamName(data.getString(INDEX_AWAY_COL))
                );

                Intent intent = new Intent(ListWidgetRemoteViewsService.this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        ListWidgetRemoteViewsService.this,0 ,intent, 0
                );
                remoteViews.setOnClickPendingIntent(R.id.widget_list_item, pendingIntent);

                //final Intent fillIntent = new Intent();
                remoteViews.setOnClickFillInIntent(R.id.widget_list_item, intent);

                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) return data.getLong(INDEX_MATCH_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
