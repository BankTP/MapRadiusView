#Map Radius View
Automatically set zoom level based on selected radius that draw over Google Map.
This library included Google Map so that you have to provide Google Map api to display google map properly.

###Installation
Gradle
```
compile 'th.in.banktp.lib.mapradiusview:0.1'
```

###Usage
Add view to xml layout
```
<th.in.banktp.lib.mapradiusview.MapRadiusView
    android:id="@+id/map_radius_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

Get MapRadiusView instance and initialize view with support FragmentManager. You can grab Google Map instance by using Google Map's OnMapReadyCallback and modify map settings later.
```
MapRadiusView mapRadiusView = (MapRadiusView)findViewById(R.id.map_radius_view);

LatLng center = new LatLng(lat,lng);
int radius = 1000; // meters
mapRadiusView.init(getSupportFragmentManager(), latLngCenter, radius, onMapReadyCallback);
```

Change radius by using setRadius(radiusInMeters) method. Google Map will be automatically change zoom level based on current radius.
```
mapRadiusView.setRadius(2000);
```

### Configuration
Set animation duration.
```
mapRadiusView.setAnimateDuration(300); // milli seconds
```

<br>

#### Planed for update
- Change pin and area drawable

#### Release note
#####0.1
- Change radius and change map zoom level
