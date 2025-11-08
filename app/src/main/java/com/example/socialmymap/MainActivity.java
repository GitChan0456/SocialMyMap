package com.example.socialmymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmymap.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final double MIN_ZOOM_FOR_BUS_STOPS = 15.0;

    private ActivityMainBinding binding;
    private MapView mapView;
    private NaverMap naverMap;

    private FusedLocationProviderClient fusedLocationClient;
    private String serviceKey = "ffM27vy9DGkDka9x8liDumAwewOhqFwXxQTsywa37yJnj5sC1gba%2FgxZhCjct2Ht27OR3uN6WO2To439x55fIA%3D%3D";

    private List<Marker> busStopMarkers = new ArrayList<>();
    private OverlayImage busStopIcon;

    private BottomSheetBehavior<View> busArrivalSheetBehavior;
    private TextView tvBusStopName;
    private LinearLayout llBusArrivalList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        View bottomSheet = findViewById(R.id.bottom_sheet_bus_arrival);
        busArrivalSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        tvBusStopName = bottomSheet.findViewById(R.id.tv_bus_stop_name);
        llBusArrivalList = bottomSheet.findViewById(R.id.ll_bus_arrival_list);
        busArrivalSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        Log.d(TAG, "Naver Map is ready!");

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
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(currentLocation, 15.0));
                    } else {
                        Toast.makeText(this, "Failed to get current location.", Toast.LENGTH_SHORT).show();
                    }
                });
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
                                } else if (tag.equals("citycode")) { // 도시코드 파싱 추가
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
                runOnUiThread(()-> Toast.makeText(MainActivity.this, "Error fetching bus stops.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showBusArrivalInfo(BusStop busStop) {
        if (busStop == null || busStop.getCityCode() == null) return;

        tvBusStopName.setText(busStop.getName());
        llBusArrivalList.removeAllViews();
        TextView loadingView = new TextView(this);
        loadingView.setText("도착 정보를 불러오는 중...");
        llBusArrivalList.addView(loadingView);
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
            urlBuilder.append("&cityCode=").append(cityCode); // 파라미터로 받은 cityCode 사용
            urlBuilder.append("&nodeId=").append(nodeId);
            urlBuilder.append("&numOfRows=").append("10");
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
                            } else if (tag.equals("arrtime")) {
                                xpp.next();
                                currentBusArrival.setArrTime(Integer.parseInt(xpp.getText()));
                            } else if (tag.equals("arrprevstationcnt")) {
                                xpp.next();
                                currentBusArrival.setArrPrevStationCnt(Integer.parseInt(xpp.getText()));
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xpp.getName();
                        if (tag.equals("item") && currentBusArrival != null) {
                            resultList.add(currentBusArrival);
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
            TextView emptyView = new TextView(this);
            emptyView.setText("도착 예정인 버스가 없습니다.");
            llBusArrivalList.addView(emptyView);
            return;
        }

        for (BusArrival info : arrivalList) {
            TextView busView = new TextView(this);
            String arrivalText = (info.getArrTime() / 60) + "분 후 도착";
            String fullText = "🚌 " + info.getRouteNo() + "번 (" + info.getArrPrevStationCnt() + " 정거장 전)\n- " + arrivalText;

            busView.setText(fullText);
            busView.setTextSize(16);
            busView.setPadding(0, 8, 0, 24);
            llBusArrivalList.addView(busView);
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
                onMapReady(this.naverMap);
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override
    protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override
    protected void onPause() { super.onPause(); mapView.onPause(); }
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
        private String name, nodeId, cityCode; // cityCode 추가
        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getLon() { return lon; }
        public void setLon(double lon) { this.lon = lon; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getNodeId() { return nodeId; }
        public void setNodeId(String nodeId) { this.nodeId = nodeId; }
        public String getCityCode() { return cityCode; } // cityCode getter/setter 추가
        public void setCityCode(String cityCode) { this.cityCode = cityCode; }
    }

    class BusArrival {
        private String routeNo;
        private int arrTime, arrPrevStationCnt;
        public String getRouteNo() { return routeNo; }
        public void setRouteNo(String routeNo) { this.routeNo = routeNo; }
        public int getArrTime() { return arrTime; }
        public void setArrTime(int arrTime) { this.arrTime = arrTime; }
        public int getArrPrevStationCnt() { return arrPrevStationCnt; }
        public void setArrPrevStationCnt(int arrPrevStationCnt) { this.arrPrevStationCnt = arrPrevStationCnt; }
    }
}