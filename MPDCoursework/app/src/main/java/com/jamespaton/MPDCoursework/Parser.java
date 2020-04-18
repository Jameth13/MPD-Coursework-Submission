//James Paton S1111175

package com.jamespaton.MPDCoursework;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


//Parser: Handles the retrieval and parsing of RSS Feeds, as well as a callback function.


interface ParserCallback {
    void parserCallback(List<RSS> rssItemsTemp);
}


class Parser {

    //This class implements Runnable so it can be started on a new thread.
    //It calls the RSS Parser and performs a parse to look for date information.
    public static class ParseURLTask implements Runnable
    {
        private static EnumMap<MainActivity.URL, String> urls = new EnumMap<>(MainActivity.URL.class);

        private ParserCallback parserCallback;
        private List<RSS> rssItemsTemp = new ArrayList<>();
        private MainActivity.URL urlSelection;

        //Static constructor
        static {
            //Traffic Scotland RSS URLs
            urls.put(MainActivity.URL.Incidents, "https://trafficscotland.org/rss/feeds/currentincidents.aspx");
            urls.put(MainActivity.URL.Roadworks, "https://trafficscotland.org/rss/feeds/roadworks.aspx");
            urls.put(MainActivity.URL.PlannedRoadworks, "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx");
            urls.put(MainActivity.URL.TestData, "https://jamespaton.com/s/MPD_TrafficScotland_TestData.aspx");
        }

        ParseURLTask(ParserCallback parserCallback, MainActivity.URL urlSelection) {
            this.parserCallback = parserCallback;
            this.urlSelection = urlSelection;
        }

        @Override
        public void run() {


            if (urlSelection == MainActivity.URL.PlanAJourney) {
                //Plan a Journey references all feeds.
                rssItemsTemp.addAll(ParseRSS(urls.get(MainActivity.URL.Incidents)));
                rssItemsTemp.addAll(ParseRSS(urls.get(MainActivity.URL.Roadworks)));
                rssItemsTemp.addAll(ParseRSS(urls.get(MainActivity.URL.PlannedRoadworks)));
            }
            else
                //Otherwise, reference only the selected feed.
                rssItemsTemp = ParseRSS(urls.get(urlSelection));


            for (int i = 0; i < rssItemsTemp.size(); i++) {
                RSS rss = rssItemsTemp.get(i);

                //Update startDate, endDate, and lengthInDays if RSS feed contains date information.
                if (urlSelection == MainActivity.URL.PlannedRoadworks || urlSelection == MainActivity.URL.Roadworks || urlSelection == MainActivity.URL.PlanAJourney || urlSelection == MainActivity.URL.TestData) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH);
                    String findStartDate = "Start Date: ";
                    String findEndDate = "End Date: ";

                    String description = rss.description;
                    int startDateIndex = description.indexOf(findStartDate) + findStartDate.length();
                    int endDateIndex = description.indexOf(findEndDate) + findEndDate.length();

                    if (startDateIndex >= findStartDate.length()) {
                        String startDate = description.substring(startDateIndex, endDateIndex);
                        String endDate = description.substring(endDateIndex);

                        rss.startDate = Calendar.getInstance();
                        rss.startDate.setTime(sdf.parse(startDate, new ParsePosition(0)));
                        rss.endDate = Calendar.getInstance();
                        rss.endDate.setTime(sdf.parse(endDate, new ParsePosition(0)));

                        long start = rss.startDate.getTimeInMillis();
                        long end = rss.endDate.getTimeInMillis();
                        rss.lengthInDays = (int) TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
                    }
                    else {
                        rss.startDate = Calendar.getInstance();
                        rss.endDate = Calendar.getInstance();
                        rss.lengthInDays = -1;
                    }
                }

                //Calculate duration in days.
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
                Calendar pubTime = Calendar.getInstance();
                pubTime.setTime(sdf.parse(rss.pubDate, new ParsePosition(0)));
                if (pubTime.before(calendar)) {
                    long startTime = calendar.getTimeInMillis();
                    long endTime = pubTime.getTimeInMillis();
                    long timeAgo = TimeUnit.MILLISECONDS.toSeconds(Math.abs(endTime - startTime));
                    rss.timeAgo = timeAgo;
                    String timeAgoString = "";

                    //TODO: Is there a neater way to do this?
                    if (timeAgo < 60)
                        if (timeAgo == 1)
                            timeAgoString = "1 second";
                        else
                            timeAgoString = Long.toString(timeAgo) + " seconds";
                    else if (timeAgo / 60 < 60)
                        if (timeAgo / 60 == 1)
                            timeAgoString = "1 minute";
                        else
                            timeAgoString = Long.toString(timeAgo / 60) + " minutes";
                    else if (timeAgo / (60 * 60) < 24)
                        if (timeAgo / (60 * 60) == 1)
                            timeAgoString = "1 hour";
                        else
                            timeAgoString = Long.toString(timeAgo / 60 / 60) + " hours";
                    else if (timeAgo / (60 * 60 * 24) < 7)
                        if (timeAgo / (60 * 60 * 24) == 1)
                            timeAgoString = "1 day";
                        else
                            timeAgoString = Long.toString(timeAgo / 60 / 60 / 24) + " days";
                    else if (timeAgo / (60 * 60 * 24 * 7) < 4)
                        if (timeAgo / (60 * 60 * 24 * 7) == 1)
                            timeAgoString = "1 week";
                        else
                            timeAgoString = Long.toString(timeAgo / 60 / 60 / 24 / 7) + " weeks";
                    else if (timeAgo / 60 / 60 / 24 / 7 / 4 == 1)
                        timeAgoString = "1 month";
                    else
                        timeAgoString = Long.toString(timeAgo / 60 / 60 / 24 / 7 / 4) + " months";

                    rss.timeAgoString = "Updated " + timeAgoString + " ago.";
                }

                //Calculate geo points;
                String geoPoints = rss.geoPoints;
                int separator = geoPoints.indexOf(" ");  //Geo points are separated by a space.
                String strLat = geoPoints.substring(0, separator);
                String strLng = geoPoints.substring(separator + 1);

                rss.lat = Float.parseFloat(strLat);
                rss.lng = Float.parseFloat(strLng);
            }

            this.parserCallback.parserCallback(rssItemsTemp);
        }
    }


    //Code adapted from: https://www.vogella.com/tutorials/AndroidXML/article.html
    private static List<RSS> ParseRSS(String rssFeed) {
        List<RSS> list = new ArrayList<>();
        XmlPullParser parser = Xml.newPullParser();
        InputStream stream = null;

        try {
            stream = new URL(rssFeed).openConnection().getInputStream();

            //Help! Do I have to convert it to a String first?
            //"The parsing MUST be done in the mobile app on the downloaded data and not on the stream."
            Scanner scanner = new Scanner(stream);
            StringBuffer stringBuffer = new StringBuffer();
            while(scanner.hasNext())
                stringBuffer.append(scanner.nextLine());
            String string = stringBuffer.toString();

            //I could just set this to the 'stream' rather than the string.
            parser.setInput(new StringReader(string));
            int eventType = parser.getEventType();
            boolean done = false;
            RSS rss = null;

            while (eventType != XmlPullParser.END_DOCUMENT && !done) {
                String name = null;

                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("item"))
                            rss = new RSS();
                        else if (rss != null) {
                            if (name.equalsIgnoreCase("title"))
                                rss.title = parser.nextText().trim();
                            else if (name.equalsIgnoreCase("description"))
                                rss.description = parser.nextText().trim();
                            else if (name.equalsIgnoreCase("pubDate"))
                                rss.pubDate = parser.nextText().trim();
                            else if (name.equalsIgnoreCase("point"))
                                rss.geoPoints = parser.nextText().trim();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("item") && rss != null) {
                            //Add any missing data.
                            if (rss.title.length() <= 0)
                                rss.title = "[No title]";
                            if (rss.description.length() <= 0)
                                rss.description = "[No description]";
                            //Add item to list.
                            list.add(rss);
                        }
                        else if (name.equalsIgnoreCase("channel"))
                            done = true;
                        break;
                }

                eventType = parser.next();
            }

        } catch (Exception e) {
            Log.e("RSSParser", e.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e("RSSParser", e.toString());
                }
            }
        }
        return list;
    }
}
