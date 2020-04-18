//James Paton S1111175

package com.jamespaton.MPDCoursework;

import android.util.Log;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


//SearchRSS: A few static methods for refining a list of RSS objects.

class SearchRSS {

    static List<RSS> SearchQuery(List<RSS> rssItemsComplete, String search, String dateText) {
        Log.e("Search", "Searching: " + search);

        //Initialize list to return.
        List<RSS> rssItemsRefined = new ArrayList<>();

        //If both search and date are empty, return all rss items.
        if (search.length() <= 0 && dateText.length() <= 0) {
            rssItemsRefined.addAll(rssItemsComplete);
            Log.e("Search", "Nothing to search, returning all results.");
            return rssItemsRefined;
        }

        //Get dateText as Calendar.
        Calendar calenderSearch = Calendar.getInstance();
        if (dateText.length() > 0) {
            SimpleDateFormat sdfSearch = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
            Date date = sdfSearch.parse(dateText, new ParsePosition(0));

            //Prevent crash from invalid date
            if (date != null)
                calenderSearch.setTime(date);
        }

        //Text search
        //Check for items containing search and bold whatever is found, also check by date.
        search = search.toLowerCase();
        for (int i = 0; i < rssItemsComplete.size(); i++) {
            boolean foundSearch = false;
            boolean foundDate = false;
            RSS rss = new RSS(rssItemsComplete.get(i));

            //Skip if no date to search.
            if (dateText.length() > 0) {
                foundDate = rss.startDate.compareTo(calenderSearch) * calenderSearch.compareTo(rss.endDate) >= 0;
            } else
                foundDate = true;

            //Skip if no text to search or was already disregarded by date.
            if (search.length() > 0 && foundDate) {
                String title = SearchForSubstring(rss.title, search, true, false);
                if (title.length() > 0) {
                    foundSearch = true;
                    rss.title = title;
                }

                String description = SearchForSubstring(rss.description, search, true, false);
                if (description.length() > 0) {
                    foundSearch = true;
                    rss.description = description;
                }
            } else
                foundSearch = true;

            //Add RSS Item to refined list if it was found by both the text search and the date search.
            if (foundSearch && foundDate) {
                rssItemsRefined.add(rss);
            }
        }

        return rssItemsRefined;
    }

    static List<RSS> SearchRoads(List<RSS> rssItemsComplete, List<String> directionsRoadNames) {
        Log.e("Roads", "Searching roads: " + directionsRoadNames.toString());

        //Initialize list to return.
        List<RSS> rssItemsRefined = new ArrayList<>();

        for (int i = 0; i < rssItemsComplete.size(); i++) {
            RSS rss = new RSS(rssItemsComplete.get(i));

            for (int j = 0; j < directionsRoadNames.size(); j++) {
                String roadName = directionsRoadNames.get(j).toLowerCase();

                boolean roadFound = false;

                String title = SearchRSS.SearchForSubstring(rss.title, roadName, true, true);
                if (title.length() > 0) {
                    roadFound = true;
                    rss.title = title;
                }

                String description = SearchRSS.SearchForSubstring(rss.description, roadName, true, true);
                if (description.length() > 0) {
                    roadFound = true;
                    rss.description = description;
                }

                if (roadFound)
                    rssItemsRefined.add(rss);
            }
        }

        return rssItemsRefined;
    }

    //Search for a substring and return the string / modified string, if found. Returns empty string if not found.
    private static String SearchForSubstring(String string, String search, boolean addBold, boolean ensureAtEnd) {
        boolean found = false;

        for (int index = string.toLowerCase().indexOf(search); index >= 0; index = string.toLowerCase().indexOf(search, index + 1)) {
            boolean endCharPass = true;
            if (ensureAtEnd && index + search.length() < string.length()) {
                char endChar = string.charAt(index + search.length());
                if (!(endChar == ' ' || endChar == ',' || endChar == '.'))
                    endCharPass = false;
            }
            if (endCharPass) {
                found = true;
                if (addBold)
                    string = string.substring(0, index) + "<b>" + string.substring(index, index + search.length()) + "</b>" + string.substring(index + search.length());
                index += 7;
            }
        }
        if (found)
            return string;
        else
            return  "";
    }
}
