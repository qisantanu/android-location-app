# Project: Location Tracker App

### Tech stack
Kotlin

### Compatibilty
With Android 10 should be working
The app will be built locally and the apk will be transfered to mobile, installed for use.

### Requirement 1 (Done)
1. Since we have the Show info page, I think we can remove the details from the first or home page. We will get more space then in home page.

### Requirement 2
The response of the get_latest_info has been changed a bit. But it should not affect the current implementation. A new attribute has been added in the response body of the API.
1. route_info has been added. The value of the route_info is a JSON.
e.g {
    "chakdah": { distance: 10 },
    "Baharampore": { distance: 150 },
    "Malda": { distance: 300 },
    "Dalkhola": { distance: 450 },
    "Siliguri": { distance: 500 }
}

2. Prepare a tabular view based on this JSON in the same screen after the current implementation.

| Place | Distance in meter |
-----------------------------
| Chakdah | 10 |
| Baharampore | 150 |
....
3. The route_info can be blank JSON as well {}, make sure the screen is not showing errors.
4. Adjust the color or theme of the view accordingly.



### Archived
1. Under the Location: in activity_main.xml, line 104, I want to show another field and value, "Remaining distance:" 
2. In the same API, it will come in the response, remaining_distance: 23465
3. I want a smooth transition of the remaining distance, like suppose from API remaining_distance: 23465
next time comes remaining_distance: 23123
So the number will be transitioned from 23465 to 23123 smoothly
4. We can use valueAnimator like
```
val start = 160
val end = 120

val animator = ValueAnimator.ofInt(start, end)
animator.duration = 500 // milliseconds

animator.addUpdateListener { animation ->
    val value = animation.animatedValue as Int
    textView.text = value.toString()
}

animator.start()
```


1. **Controls:** - Add a "Start Tracking" button (Enabled when not tracking).
   - Add a "Stop Tracking" button (Enabled only when tracking is active).
2. **Live Log Terminal:**
   - Add a scrollable text area or `LazyColumn` at the bottom of the screen.
   - **Log Format:** `[Timestamp] - [Status/Error] - [Message]`
   - Purpose: To debug location fixes, permission denials, application errors, or provider status (GPS off/on).

3. Make sure the application tracking locations when it is in background as well or minimize. Unless we stop tracking it should continue with tracking locations.

4. ### UI enhacement

Color Scheme:
    - Primary color: #4A90E2 (soft blue)
    - Accent color: #FF6B6B (coral)
    - Background color: #F7F9FC (light gray)
    - Text color: #333333 (dark gray)
    - Error color: #D32F2F (red)
Button Design:
    - Use Material Design buttons with primary color #4A90E2
    - Add icons to buttons (e.g., play icon for "Start Tracking")
Typography:
    - Font family: Roboto
    - Use TextAppearance.Material.Headline6 for headings
    - Use TextAppearance.Material.Body1 for body text
Status Log:
    - Use a RecyclerView with alternate row colors (e.g., #F2F2F2 and #FFFFFF)
    - Highlight errors with a soft red background #FFEBEE and text color #D32F2F
Spacing:
    - Use 16dp padding and 12dp margins for a breathy feel
Icons:
    - Use Material Design icons (e.g., ic_play_arrow for "Start Tracking")
Header:
    - Use a Toolbar with primary color #4A90E2 and white text

5. When I stop tracking it should send all the unsynced locations to backend via API
Or may be it is calling but there is an error Network error during sync: CLEATEXT communication to 192.168.... not permit by network security policy
6. Make the threashold of 10 locations batch to 2

7. Maintain a version of the app. So that, when I release a new build, I do not have to uninstall and install new build. I can update. And the existing data stays there.
8. I need more logs to be added. Right now I can see only 
Location tracking started
LocationService: onStartCommand called
Location service created
LocationService:onCreate called

When I stop tracking the other logs are coming. It is not live logging.

1. The locations are sent to backend , update from 2 to 10 for the batch size
1. In the base URL, where user input the URL, can we make it readonly after saving. Otherwise, accidentally, the URL could get updated. To prevent that add some restriction. But user should able to update the URL when he wants.

2. Add a header in the backend API call. header information: X-API-KEY: suxmahdixumotherfukers
otherwise the API will return 401
3. Increase the version with this fix.
2. a. If the backend API is not working or down, will the app still tracking and keeping the locations locations locally? 
b. If yes, upto how many locations it can store locally?
c. What will be the size of that unsynced locations data? 
d. Is it still storing the synced locations data?

## New UI / Features/ Todos
Create a screen which can fetch some info from a get API and serve to the user.
Also the screen will have a refresh button to refetch

Lets assume the response will be in JSON.
Key and value of the JSON will be label and value

e.g {
    "Last location": "Baguiati, Kolkata 700159",
    "Average speed": "23km",
    "Speed in last 60 min": "34km" 
}

so that even the key got changed in future, the page should not throw any error. It can have upto 10 keys as of now.
### Bugs
