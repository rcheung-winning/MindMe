package cs446.mindme.DataRequest;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cs446.mindme.DataHolders.EventDataHolder;
import cs446.mindme.DataHolders.ReminderDataHolder;

public class EventRequest extends AsyncTask<String, Void, ArrayList<EventDataHolder>> {

    // Refer to https://github.com/uWaterloo/api-documentation
    private final static String apiKey = "?key=435c0cf289fbfebc934d29e8c924b323";
    private final static String apiLink = "https://api.uwaterloo.ca/v2/";
    private final static String strEvent = "events";
    private final static String strJSON = ".json";

    // Keys for the JSON
    private final static String keyData = "data";
    private final static String keyEventID = "id";
    private final static String keySite = "site";
    private final static String keySiteName = "site_name";
    private final static String keyTitle = "title";
    private final static String keyTimes = "times";
    private final static String keyType = "type";
    private final static String keyLink = "link";
    private final static String keyUpdated = "updated";

    // Builds the address for JSON request
    public static String buildAddress(String site, String eventID) {
        // Example:
        // https://api.uwaterloo.ca/v2/events.json?key=435c0cf289fbfebc934d29e8c924b323
        // https://api.uwaterloo.ca/v2/events/engineering/1701.json?key=435c0cf289fbfebc934d29e8c924b323
        String jsonAddress = apiLink;
        if (site != null && eventID != null){
            jsonAddress += strEvent + "/" + site + "/" + eventID + strJSON+ apiKey;
        } else {
            jsonAddress += strEvent+ strJSON + apiKey;
        }
        System.out.println(jsonAddress);
        return jsonAddress;
    }

    @Override
    protected ArrayList<EventDataHolder> doInBackground(String ... params) {
        URL url;
        HttpURLConnection urlConnection = null;
        String responseString = "";
        ArrayList<EventDataHolder> eventList = new ArrayList<EventDataHolder>();
        try {
            url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                responseString = readStream(urlConnection.getInputStream());
                System.out.println(responseString);
                JSONObject responseObject = new JSONObject(responseString);
                JSONArray dataArray = responseObject.getJSONArray(keyData);

                if (dataArray.length() == 0)  {
                    System.out.println("dataArray length 0");
                } else {
                    System.out.println("dataArray length " + dataArray.length());
                }

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject eventObject = dataArray.getJSONObject(i);
                    System.out.println(eventObject.toString());

                    // get the times
                    // JSONArray timesArray = eventObject.getJSONArray(keyTimes);
                    JSONArray timesArray = eventObject.optJSONArray(keyTimes);
                    List<String> times = new ArrayList<String>();
                    for (int j = 0; j < timesArray.length(); i++) {
                        times.add(timesArray.getString(i));
                    }

                    // get the types
                    JSONArray typesArray = eventObject.optJSONArray(keyType);
                    List<String> types = new ArrayList<String>();
                    for (int j = 0; j < typesArray.length(); j++) {
                        types.add(typesArray.getString(i));
                    }

                    String description = "";

                    Date rDate = null;
                    System.out.println(eventObject.getString(keyTitle));
                    EventDataHolder eventHolder = new EventDataHolder(
                            Integer.parseInt(eventObject.getString(keyEventID)),
                            eventObject.getString(keyTitle),
                            eventObject.getString(keySite),
                            rDate,
                            times,
                            description,
                            eventObject.getString(keyLink),
                            eventObject.getString(keySiteName),
                            types
                    );
                    eventList.add(eventHolder);
                }
            } else {
                Log.v("EventRequest", "Response code:" + responseCode);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return eventList;
    }


    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}
