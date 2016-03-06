package it.jaschke.alexandria;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import it.jaschke.alexandria.services.BookService;

public class Utility {
    /**
     * Returns true if the network is available of about to become available.
     *
     * @param context Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    static public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @SuppressWarnings("ResourceType")
    static public @BookService.BookFoundStatus int getBookFoundStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(
                context.getString(R.string.pref_book_found_status_key),
                BookService.BOOK_FOUND_STATUS_UNKNOWN
        );
    }
}
