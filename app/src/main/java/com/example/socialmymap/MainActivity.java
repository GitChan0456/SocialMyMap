package com.example.socialmymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmymap.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final double MIN_ZOOM_FOR_BUS_STOPS = 15.0;

    private ActivityMainBinding binding;
    private MapView mapView;
    private NaverMap naverMap;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationOverlay locationOverlay;
    private boolean isFirstLocationUpdate = true;

    private String serviceKey = "ffM27vy9DGkDka9x8liDumAwewOhqFwXxQTsywa37yJnj5sC1gba%2FgxZhCjct2Ht27OR3uN6WO2To439x55fIA%3D%3D";

    private List<Marker> busStopMarkers = new ArrayList<>();
    private OverlayImage busStopIcon;
    private Marker longClickMarker;
    private Marker geocodedMarker;

    // --- UI Elements ---
    private BottomSheetBehavior<View> busArrivalSheetBehavior;
    private TextView tvBusStopName, tvBusStopInfo, tvSoonArrival;
    private LinearLayout llBusArrivalList;

    private BottomSheetBehavior<View> placeInfoSheetBehavior;
    private TextView tvPlaceName, tvPlaceCategory, tvPlaceAddress;

            private FloatingActionButton fabCurrentLocation;

        

            private EditText etAddress;

            private Button btnGeocode;

            private Geocoder geocoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                geocoder = new Geocoder(this, Locale.KOREA);

        

                etAddress = findViewById(R.id.et_address);

                btnGeocode = findViewById(R.id.btn_geocode);

                btnGeocode.setOnClickListener(v -> {

                    String address = etAddress.getText().toString();

                    if (!address.isEmpty()) {

                        performGeocoding(address);

                    } else {

                        Toast.makeText(this, "주소를 입력해주세요.", Toast.LENGTH_SHORT).show();

                    }

                });

        

                fabCurrentLocation = findViewById(R.id.fab_current_location);

                fabCurrentLocation.setOnClickListener(v -> moveToCurrentLocation());

        // --- BottomSheet Callbacks ---
        BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    fabCurrentLocation.show();
                } else {
                    fabCurrentLocation.hide();
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        };

        // 버스 도착 정보 바텀시트 초기화 및 콜백 설정
        View busBottomSheet = findViewById(R.id.bottom_sheet_bus_arrival);
        busArrivalSheetBehavior = BottomSheetBehavior.from(busBottomSheet);
        busArrivalSheetBehavior.addBottomSheetCallback(bottomSheetCallback);
        tvBusStopName = busBottomSheet.findViewById(R.id.tv_bus_stop_name);
        tvBusStopInfo = busBottomSheet.findViewById(R.id.tv_bus_stop_info);
        tvSoonArrival = busBottomSheet.findViewById(R.id.tv_soon_arrival);
        llBusArrivalList = busBottomSheet.findViewById(R.id.ll_bus_arrival_list);
        busArrivalSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // 장소 정보 바텀시트 초기화 및 콜백 설정
        View placeBottomSheet = findViewById(R.id.bottom_sheet_place_info);
        placeInfoSheetBehavior = BottomSheetBehavior.from(placeBottomSheet);
        placeInfoSheetBehavior.addBottomSheetCallback(bottomSheetCallback);
        tvPlaceName = placeBottomSheet.findViewById(R.id.tv_place_name);
        tvPlaceCategory = placeBottomSheet.findViewById(R.id.tv_place_category);
        tvPlaceAddress = placeBottomSheet.findViewById(R.id.tv_place_address);
        placeInfoSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int halfScreenHeight = displayMetrics.heightPixels / 2;
        busArrivalSheetBehavior.setPeekHeight(halfScreenHeight);
        placeInfoSheetBehavior.setPeekHeight(halfScreenHeight);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        Log.d(TAG, "Naver Map is ready!");

        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(new LatLng(37.5665, 126.9780), 15.0));

        locationOverlay = naverMap.getLocationOverlay();
        locationOverlay.setVisible(true);

        busStopIcon = OverlayImage.fromResource(R.drawable.ic_bus_with_border);

        naverMap.addOnCameraIdleListener(() -> {
            CameraPosition cameraPosition = naverMap.getCameraPosition();
            if (cameraPosition.zoom >= MIN_ZOOM_FOR_BUS_STOPS) {
                fetchNearbyBusStops(cameraPosition.target.latitude, cameraPosition.target.longitude);
            } else {
                clearBusStopMarkers();
            }
        });

        naverMap.setOnMapClickListener((point, coord) -> {
            if (busArrivalSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                busArrivalSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
            if (placeInfoSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                placeInfoSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
            if (longClickMarker != null) {
                longClickMarker.setMap(null);
            }
            if (geocodedMarker != null) {
                geocodedMarker.setMap(null);
            }
        });

        this.naverMap.setOnMapLongClickListener((point, coord) -> {
            if (longClickMarker != null) {
                longClickMarker.setMap(null);
            }
            longClickMarker = new Marker();
            longClickMarker.setPosition(coord);
            longClickMarker.setIcon(OverlayImage.fromResource(com.naver.maps.map.R.drawable.navermap_default_marker_icon_blue));
            longClickMarker.setMap(naverMap);
        });

        checkLocationPermission();
    }

    private void performGeocoding(String addressString) {
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    LatLng point = new LatLng(address.getLatitude(), address.getLongitude());

                    runOnUiThread(() -> {
                        if (geocodedMarker != null) {
                            geocodedMarker.setMap(null);
                        }
                        geocodedMarker = new Marker();
                        geocodedMarker.setPosition(point);
                        geocodedMarker.setCaptionText(addressString);
                        geocodedMarker.setMap(naverMap);

                        geocodedMarker.setOnClickListener(overlay -> {
                            showPlaceInfo(point, addressString);
                            return true;
                        });

                        naverMap.moveCamera(CameraUpdate.scrollTo(point));
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed", e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "지오코딩 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showPlaceInfo(LatLng point, String name) {
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String placeName = name;
                    String category = address.getFeatureName();
                    String fullAddress = address.getAddressLine(0);

                    runOnUiThread(() -> {
                        tvPlaceName.setText(placeName);
                        tvPlaceCategory.setText(category != null ? category : "정보 없음");
                        tvPlaceAddress.setText(fullAddress);
                        placeInfoSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Reverse Geocoding failed", e);
            }
        }).start();
    }


    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null && naverMap != null) {
                    LatLng latLng = new LatLng(locationResult.getLastLocation());
                    locationOverlay.setPosition(latLng);

                    if (isFirstLocationUpdate) {
                        isFirstLocationUpdate = false;
                        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(latLng, 15.0));
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }


    private void moveToCurrentLocation() {
        if (locationOverlay.getPosition() != null) {
            naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(locationOverlay.getPosition(), 15.0));
        } else {
            Toast.makeText(this, "Current location is not available yet.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchNearbyBusStops(double lat, double lon) {
        new Thread(() -> {
            List<BusStop> busStops = new ArrayList<>();
            try {
                StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getCrdntPrxmtSttnList");
                urlBuilder.append("?serviceKey=").append(serviceKey);
                urlBuilder.append("&pageNo=").append("1");
                urlBuilder.append("&numOfRows=").append("20");
                urlBuilder.append("&_type=").append("xml");
                urlBuilder.append("&gpsLati=").append(lat);
                urlBuilder.append("&gpsLong=").append(lon);

                URL url = new URL(urlBuilder.toString());
                InputStream is = url.openStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(is, "UTF-8"));

                String tag;
                BusStop currentBusStop = null;
                int eventType = xpp.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            tag = xpp.getName();
                            if (tag.equals("item")) {
                                currentBusStop = new BusStop();
                            } else if (currentBusStop != null) {
                                if (tag.equals("gpslati")) {
                                    xpp.next();
                                    currentBusStop.setLat(Double.parseDouble(xpp.getText()));
                                } else if (tag.equals("gpslong")) {
                                    xpp.next();
                                    currentBusStop.setLon(Double.parseDouble(xpp.getText()));
                                } else if (tag.equals("nodenm")) {
                                    xpp.next();
                                    currentBusStop.setName(xpp.getText());
                                } else if (tag.equals("nodeid")) {
                                    xpp.next();
                                    currentBusStop.setNodeId(xpp.getText());
                                } else if (tag.equals("citycode")) {
                                    xpp.next();
                                    currentBusStop.setCityCode(xpp.getText());
                                }
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            tag = xpp.getName();
                            if (tag.equals("item") && currentBusStop != null) {
                                busStops.add(currentBusStop);
                            }
                            break;
                    }
                    eventType = xpp.next();
                }

                runOnUiThread(() -> {
                    clearBusStopMarkers();
                    for (BusStop busStop : busStops) {
                        Marker marker = new Marker();
                        marker.setPosition(new LatLng(busStop.getLat(), busStop.getLon()));
                        marker.setIcon(busStopIcon);
                        marker.setWidth(40);
                        marker.setHeight(40);
                        marker.setIconPerspectiveEnabled(true);
                        marker.setTag(busStop);
                        marker.setMap(naverMap);

                        marker.setOnClickListener(overlay -> {
                            BusStop clickedBusStop = (BusStop) overlay.getTag();
                            showBusArrivalInfo(clickedBusStop);
                            return true;
                        });

                        busStopMarkers.add(marker);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error fetching bus stops", e);
            }
        }).start();
    }

    private void showBusArrivalInfo(BusStop busStop) {
        if (busStop == null || busStop.getCityCode() == null) return;

        tvBusStopName.setText(busStop.getName());
        tvBusStopInfo.setText(busStop.getNodeId() + " | 다음 정류장 방향");
        llBusArrivalList.removeAllViews();
        tvSoonArrival.setVisibility(View.GONE);

        busArrivalSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        new Thread(() -> {
            final List<BusArrival> arrivalList = fetchBusArrivalsApi(busStop.getNodeId(), busStop.getCityCode());
            runOnUiThread(() -> updateBusArrivalSheet(arrivalList));
        }).start();
    }

    private List<BusArrival> fetchBusArrivalsApi(String nodeId, String cityCode) {
        List<BusArrival> resultList = new ArrayList<>();
        try {
            StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList");
            urlBuilder.append("?serviceKey=").append(serviceKey);
            urlBuilder.append("&cityCode=").append(cityCode);
            urlBuilder.append("&nodeId=").append(nodeId);
            urlBuilder.append("&numOfRows=").append("20");
            urlBuilder.append("&pageNo=").append("1");
            urlBuilder.append("&_type=").append("xml");

            URL url = new URL(urlBuilder.toString());
            InputStream is = url.openStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8"));

            String tag;
            BusArrival currentBusArrival = null;
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();
                        if (tag.equals("item")) {
                            currentBusArrival = new BusArrival();
                        } else if (currentBusArrival != null) {
                            if (tag.equals("routeno")) {
                                xpp.next();
                                currentBusArrival.setRouteNo(xpp.getText());
                            } else if (tag.equals("routetp")) {
                                xpp.next();
                                currentBusArrival.setRouteType(xpp.getText());
                            } else if (tag.equals("arrtime")) {
                                xpp.next();
                                currentBusArrival.addArrTime(Integer.parseInt(xpp.getText()));
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xpp.getName();
                        if (tag.equals("item") && currentBusArrival != null) {
                            boolean merged = false;
                            for (BusArrival item : resultList) {
                                if (item.getRouteNo().equals(currentBusArrival.getRouteNo())) {
                                    item.addArrTime(currentBusArrival.getArrTimes().get(0));
                                    merged = true;
                                    break;
                                }
                            }
                            if (!merged) {
                                resultList.add(currentBusArrival);
                            }
                        }
                        break;
                }
                eventType = xpp.next();
            }
            return resultList;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching bus arrivals", e);
            return null;
        }
    }

    private void updateBusArrivalSheet(List<BusArrival> arrivalList) {
        llBusArrivalList.removeAllViews();

        if (arrivalList == null || arrivalList.isEmpty()) {
            tvSoonArrival.setVisibility(View.GONE);
            TextView emptyView = new TextView(this);
            emptyView.setText("도착 예정인 버스가 없습니다.");
            emptyView.setPadding(0, 40, 0, 40);
            llBusArrivalList.addView(emptyView);
            return;
        }

        List<String> soonArrivalBuses = arrivalList.stream()
                .filter(bus -> bus.getArrTimes().get(0) < 180)
                .map(BusArrival::getRouteNo)
                .collect(Collectors.toList());

        if (!soonArrivalBuses.isEmpty()) {
            tvSoonArrival.setText("곧 도착 " + String.join(", ", soonArrivalBuses));
            tvSoonArrival.setVisibility(View.VISIBLE);
        } else {
            tvSoonArrival.setVisibility(View.GONE);
        }

        LayoutInflater inflater = getLayoutInflater();
        for (BusArrival info : arrivalList) {
            View itemView = inflater.inflate(R.layout.list_item_bus_arrival, llBusArrivalList, false);

            TextView tvBusType = itemView.findViewById(R.id.tv_bus_type);
            TextView tvBusNumber = itemView.findViewById(R.id.tv_bus_number);
            TextView tvArrivalTime1 = itemView.findViewById(R.id.tv_arrival_time_1);
            TextView tvArrivalTime2 = itemView.findViewById(R.id.tv_arrival_time_2);

            tvBusType.setText(info.getRouteType());
            tvBusNumber.setText(info.getRouteNo());

            if (!info.getArrTimes().isEmpty()) {
                int firstTime = info.getArrTimes().get(0);
                tvArrivalTime1.setText((firstTime / 60) + "분 후 도착");
                if (firstTime < 180) {
                    tvArrivalTime1.setText("곧 도착");
                    tvArrivalTime1.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    tvArrivalTime1.setTextColor(getResources().getColor(android.R.color.black));
                }
            }

            if (info.getArrTimes().size() > 1) {
                int secondTime = info.getArrTimes().get(1);
                tvArrivalTime2.setText((secondTime / 60) + "분 후 도착");
                tvArrivalTime2.setVisibility(View.VISIBLE);
            } else {
                tvArrivalTime2.setVisibility(View.GONE);
            }

            llBusArrivalList.addView(itemView);
        }
    }

    private void clearBusStopMarkers() {
        for (Marker marker : busStopMarkers) {
            marker.setMap(null);
        }
        busStopMarkers.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override
    protected void onResume() { super.onResume(); if (locationCallback != null) startLocationUpdates(); }
    @Override
    protected void onPause() { super.onPause(); if (fusedLocationClient != null && locationCallback != null) fusedLocationClient.removeLocationUpdates(locationCallback); }
    @Override
    protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override
    protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override
    public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }

    class BusStop {
        private double lat, lon;
        private String name, nodeId, cityCode;
        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getLon() { return lon; }
        public void setLon(double lon) { this.lon = lon; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getNodeId() { return nodeId; }
        public void setNodeId(String nodeId) { this.nodeId = nodeId; }
        public String getCityCode() { return cityCode; }
        public void setCityCode(String cityCode) { this.cityCode = cityCode; }
    }

    class BusArrival {
        private String routeNo;
        private String routeType;
        private List<Integer> arrTimes = new ArrayList<>();

        public String getRouteNo() { return routeNo; }
        public void setRouteNo(String routeNo) { this.routeNo = routeNo; }
        public String getRouteType() { return routeType; }
        public void setRouteType(String routeType) { this.routeType = routeType; }
        public List<Integer> getArrTimes() { return arrTimes; }
        public void addArrTime(int time) { this.arrTimes.add(time); }
    }
}