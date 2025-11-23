package com.example.socialmymap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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
import com.naver.maps.geometry.Tm128;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PolylineOverlay;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final int REQUEST_CODE_FAVORITES = 1001;
    private static final double MIN_ZOOM_FOR_BUS_STOPS = 15.0;

    private static final String NAVER_SEARCH_CLIENT_ID = "A71elrpOWwaPKzRwR5NH";
    private static final String NAVER_SEARCH_CLIENT_SECRET = "EZxU2oT_JG";
    private static final String TMAP_APP_KEY = "MRXykLttAL6XbvFba0XGcan8Pu2WM9au6AN0LBNh";

    private ActivityMainBinding binding;
    private MapView mapView;
    private NaverMap naverMap;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationOverlay locationOverlay;
    private boolean isFirstLocationUpdate = true;

    private String serviceKey = "ffM27vy9DGkDka9x8liDumAwewOhqFwXxQTsywa37yJnj5sC1gba%2FgxZhCjct2Ht27OR3uN6WO2To439x55fIA%3D%3D";

    private List<Marker> busStopMarkers = new ArrayList<>();
    private List<Marker> placeMarkers = new ArrayList<>();
    private List<Marker> longClickMarkers = new ArrayList<>();
    private OverlayImage busStopIcon;
    private Marker geocodedMarker;
    private PolylineOverlay currentRouteOverlay;

    // UI Components
    private LinearLayout searchBar;
    private BottomSheetBehavior<View> busArrivalSheetBehavior;
    private TextView tvBusStopName, tvBusStopInfo, tvSoonArrival;
    private LinearLayout llBusArrivalList;

    private BottomSheetBehavior<View> placeInfoSheetBehavior;
    private TextView tvPlaceName, tvPlaceCategory, tvPlaceAddress;
    private Button btnStart, btnArrive;

    private BottomSheetBehavior<View> mainMenuSheetBehavior;

    private FloatingActionButton fabCurrentLocation;
    private EditText etAddress;
    private Button btnGeocode, btnCategoryConvenience, btnCategoryCafe, btnCategorySeowon, btnCategoryBank;
    private Geocoder geocoder;

    private String startLocationFromSheet = null;

    // Route Info Panel Components
    private LinearLayout routeInfoPanel;
    private ImageView ivTransportIcon;
    private TextView tvTotalTime;
    private TextView tvTotalDistance;
    private ImageView btnCancelRoute;


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

        // Search Bar
        searchBar = findViewById(R.id.search_bar);
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

        // Category Buttons
        btnCategoryConvenience = findViewById(R.id.btn_category_convenience);
        btnCategoryCafe = findViewById(R.id.btn_category_cafe);
        btnCategorySeowon = findViewById(R.id.btn_category_seowon);
        btnCategoryBank = findViewById(R.id.btn_category_bank);
        btnCategoryConvenience.setOnClickListener(v -> searchNearbyPlaces("편의점"));
        btnCategoryCafe.setOnClickListener(v -> searchNearbyPlaces("카페"));
        btnCategorySeowon.setOnClickListener(v -> searchNearbyPlaces("병원"));
        btnCategoryBank.setOnClickListener(v -> {
            if (mainMenuSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                mainMenuSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                mainMenuSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });


        // FAB
        fabCurrentLocation = findViewById(R.id.fab_current_location);
        fabCurrentLocation.setOnClickListener(v -> moveToCurrentLocation());

        // Bus Arrival Bottom Sheet
        View busBottomSheet = findViewById(R.id.bottom_sheet_bus_arrival);
        busArrivalSheetBehavior = BottomSheetBehavior.from(busBottomSheet);
        busArrivalSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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
        });
        tvBusStopName = busBottomSheet.findViewById(R.id.tv_bus_stop_name);
        tvBusStopInfo = busBottomSheet.findViewById(R.id.tv_bus_stop_info);
        tvSoonArrival = busBottomSheet.findViewById(R.id.tv_soon_arrival);
        llBusArrivalList = busBottomSheet.findViewById(R.id.ll_bus_arrival_list);
        busArrivalSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // Place Info Bottom Sheet
        View placeBottomSheet = findViewById(R.id.bottom_sheet_place_info);
        placeInfoSheetBehavior = BottomSheetBehavior.from(placeBottomSheet);
        placeInfoSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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
        });
        tvPlaceName = placeBottomSheet.findViewById(R.id.tv_place_name);
        tvPlaceCategory = placeBottomSheet.findViewById(R.id.tv_place_category);
        tvPlaceAddress = placeBottomSheet.findViewById(R.id.tv_place_address);
        btnStart = placeBottomSheet.findViewById(R.id.btn_start);
        btnArrive = placeBottomSheet.findViewById(R.id.btn_arrive);
        placeInfoSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // Main Menu Bottom Sheet
        View mainMenuBottomSheet = findViewById(R.id.bottom_sheet_main_menu);
        mainMenuSheetBehavior = BottomSheetBehavior.from(mainMenuBottomSheet);
        mainMenuSheetBehavior.setPeekHeight(100);
        mainMenuSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mainMenuSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    fabCurrentLocation.show();
                } else {
                    fabCurrentLocation.hide();
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        LinearLayout btnNav = mainMenuBottomSheet.findViewById(R.id.btn_feature_1);
        LinearLayout btnSearch = mainMenuBottomSheet.findViewById(R.id.btn_feature_2);
        LinearLayout btnFav = mainMenuBottomSheet.findViewById(R.id.btn_feature_3);
        LinearLayout btnBus = mainMenuBottomSheet.findViewById(R.id.btn_feature_4);
        LinearLayout btnMyPage = mainMenuBottomSheet.findViewById(R.id.btn_feature_5);
        LinearLayout btnSettings = mainMenuBottomSheet.findViewById(R.id.btn_feature_6);
        Button btnLogout = mainMenuBottomSheet.findViewById(R.id.logout_button);

        btnNav.setOnClickListener(v -> showDirectionsDialog());
        btnSearch.setOnClickListener(v -> {
            searchBar.setVisibility(View.GONE);
            mainMenuSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            // 경로 정보 패널의 마진을 동적으로 변경
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) routeInfoPanel.getLayoutParams();
            params.topMargin = (int) (16 * getResources().getDisplayMetrics().density); // 16dp
            routeInfoPanel.setLayoutParams(params);

            Toast.makeText(this, "지도 집중 모드", Toast.LENGTH_SHORT).show();
        });
        btnFav.setOnClickListener(v -> {
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivityForResult(intent, REQUEST_CODE_FAVORITES);
        });
        btnBus.setOnClickListener(v -> Toast.makeText(this, "커뮤니티", Toast.LENGTH_SHORT).show());
        btnMyPage.setOnClickListener(v -> startActivity(new Intent(this, MyPageActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Route Info Panel
        routeInfoPanel = findViewById(R.id.route_info_panel);
        ivTransportIcon = routeInfoPanel.findViewById(R.id.iv_transport_icon);
        tvTotalTime = routeInfoPanel.findViewById(R.id.tv_total_time);
        tvTotalDistance = routeInfoPanel.findViewById(R.id.tv_total_distance);
        btnCancelRoute = routeInfoPanel.findViewById(R.id.btn_cancel_route);
        btnCancelRoute.setOnClickListener(v -> clearRouteAndPanel());


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int halfScreenHeight = displayMetrics.heightPixels / 2;
        busArrivalSheetBehavior.setPeekHeight(halfScreenHeight);
        placeInfoSheetBehavior.setPeekHeight(halfScreenHeight);
    }

    private void showDirectionsDialog() {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_directions, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        EditText etStart = dialogView.findViewById(R.id.et_start_location);
        EditText etEnd = dialogView.findViewById(R.id.et_end_location);
        Button btnSearchStart = dialogView.findViewById(R.id.btn_search_start);
        Button btnSearchEnd = dialogView.findViewById(R.id.btn_search_end);
        Button btnFindRoute = dialogView.findViewById(R.id.btn_find_route);
        RadioGroup rgTransportMode = dialogView.findViewById(R.id.rg_transport_mode);

        LinearLayout carOptionsLayout = dialogView.findViewById(R.id.car_options_layout);
        CheckBox cbTrafast = dialogView.findViewById(R.id.cb_option_trafast);
        CheckBox cbTraoptimal = dialogView.findViewById(R.id.cb_option_traoptimal);
        CheckBox cbTraavoid = dialogView.findViewById(R.id.cb_option_traavoid);

        rgTransportMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_car) {
                carOptionsLayout.setVisibility(View.VISIBLE);
                cbTraoptimal.setChecked(true); // 최적경로 기본 체크
            } else {
                carOptionsLayout.setVisibility(View.GONE);
                // 다른 모드 선택 시 체크박스 초기화 (선택 사항)
                cbTrafast.setChecked(false);
                cbTraoptimal.setChecked(false);
                cbTraavoid.setChecked(false);
            }
        });

        if (startLocationFromSheet != null) {
            etStart.setText(startLocationFromSheet);
            startLocationFromSheet = null;
        }

        btnSearchStart.setOnClickListener(v -> showSearchResultsDialog(etStart.getText().toString(), etStart));
        btnSearchEnd.setOnClickListener(v -> showSearchResultsDialog(etEnd.getText().toString(), etEnd));

        btnFindRoute.setOnClickListener(view -> {
            String startLocationName = etStart.getText().toString();
            String endLocationName = etEnd.getText().toString();

            if (startLocationName.isEmpty() || endLocationName.isEmpty()) {
                Toast.makeText(this, "출발지와 목적지를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = rgTransportMode.getCheckedRadioButtonId();

            new Thread(() -> {
                try {
                    LatLng startPoint;
                    if (startLocationName.equals("현재 위치") && locationOverlay.getPosition() != null) {
                        startPoint = locationOverlay.getPosition();
                    } else {
                        List<Address> startAddresses = geocoder.getFromLocationName(startLocationName, 1);
                        if (startAddresses == null || startAddresses.isEmpty()) {
                            runOnUiThread(() -> Toast.makeText(this, "출발지 주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show());
                            return;
                        }
                        startPoint = new LatLng(startAddresses.get(0).getLatitude(), startAddresses.get(0).getLongitude());
                    }

                    List<Address> endAddresses = geocoder.getFromLocationName(endLocationName, 1);
                    if (endAddresses == null || endAddresses.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(this, "도착지 주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    LatLng endPoint = new LatLng(endAddresses.get(0).getLatitude(), endAddresses.get(0).getLongitude());

                    if (selectedId == R.id.rb_walk) {
                        requestTmapPedestrianRoute(startPoint, endPoint);
                    } else if (selectedId == R.id.rb_car) {
                        List<String> options = new ArrayList<>();
                        if (cbTrafast.isChecked()) options.add("0");
                        if (cbTraoptimal.isChecked()) options.add("4");
                        if (cbTraavoid.isChecked()) options.add("10");
                        String searchOption = String.join(",", options);
                        requestTmapCarRoute(startPoint, endPoint, searchOption);
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Geocoding failed for directions", e);
                    runOnUiThread(() -> Toast.makeText(this, "주소 변환 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error during route finding", e);
                    runOnUiThread(() -> Toast.makeText(this, "경로 탐색 중 알 수 없는 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
                }
            }).start();

            alertDialog.dismiss();
        });
    }

    private void showSearchResultsDialog(String query, final EditText targetEditText) {
        if (query.isEmpty()) {
            Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(query, 5);

                runOnUiThread(() -> {
                    if (addresses == null || addresses.isEmpty()) {
                        Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final View dialogView = getLayoutInflater().inflate(R.layout.dialog_search_results, null);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setView(dialogView);
                    final AlertDialog searchDialog = builder.create();
                    searchDialog.show();

                    RecyclerView rv = dialogView.findViewById(R.id.rv_search_results);
                    SearchResultsAdapter adapter = new SearchResultsAdapter(addresses, address -> {
                        targetEditText.setText(address.getAddressLine(0));
                        searchDialog.dismiss();
                    });
                    rv.setAdapter(adapter);
                    rv.setLayoutManager(new LinearLayoutManager(this));
                });
            } catch (IOException e) {
                Log.e(TAG, "Geocoding search failed", e);
                runOnUiThread(() -> Toast.makeText(this, "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
            }
        }).start();
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
                return;
            }
            if (placeInfoSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                placeInfoSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return;
            }

            if (!longClickMarkers.isEmpty()) {
                for(Marker marker : longClickMarkers) {
                    marker.setMap(null);
                }
                longClickMarkers.clear();
            }
            if (geocodedMarker != null) {
                geocodedMarker.setMap(null);
            }
            clearPlaceMarkers();
            // routeInfoPanel.setVisibility(View.GONE); // 이 부분 제거
        });

        naverMap.setOnMapDoubleTapListener((point, coord) -> {
            // 지도 집중 모드일 때만 해제 기능 수행
            if (searchBar.getVisibility() == View.GONE) {
                searchBar.setVisibility(View.VISIBLE);
                mainMenuSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                // 경로 정보 패널의 마진을 원래대로 복원
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) routeInfoPanel.getLayoutParams();
                params.topMargin = (int) (130 * getResources().getDisplayMetrics().density); // 130dp
                routeInfoPanel.setLayoutParams(params);
            }
            return true;
        });

        this.naverMap.setOnMapLongClickListener((point, coord) -> {
            if (longClickMarkers.size() >= 3) {
                Marker oldestMarker = longClickMarkers.remove(0);
                oldestMarker.setMap(null);
            }

            Marker newMarker = new Marker();
            newMarker.setPosition(coord);
            newMarker.setIcon(OverlayImage.fromResource(com.naver.maps.map.R.drawable.navermap_default_marker_icon_blue));
            newMarker.setMap(naverMap);
            longClickMarkers.add(newMarker);

            new Thread(() -> {
                try {
                    List<Address> addresses = geocoder.getFromLocation(coord.latitude, coord.longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        newMarker.setTag(address);

                        runOnUiThread(() -> {
                            newMarker.setOnClickListener(overlay -> {
                                Address clickedAddress = (Address) overlay.getTag();
                                String placeName = clickedAddress.getFeatureName() != null ? clickedAddress.getFeatureName() : "이름 없는 장소";
                                showPlaceInfo(placeName, clickedAddress.getAddressLine(0), "선택한 위치", coord);
                                return true;
                            });
                        });
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Reverse geocoding failed", e);
                }
            }).start();
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
                        geocodedMarker.setMap(naverMap);

                        geocodedMarker.setOnClickListener(overlay -> {
                            showPlaceInfo(addressString, address.getAddressLine(0), "검색 결과", point);
                            return true;
                        });

                        naverMap.moveCamera(CameraUpdate.scrollTo(point));
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed", e);
                runOnUiThread(() -> Toast.makeText(this, "지오코딩 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showPlaceInfo(String name, String address, String category, LatLng destination) {
        busArrivalSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        tvPlaceName.setText(name);
        tvPlaceAddress.setText(address);
        tvPlaceCategory.setText(category);

        btnStart.setOnClickListener(v -> {
            startLocationFromSheet = name;
            Toast.makeText(this, name + " 출발지로 설정되었습니다.", Toast.LENGTH_SHORT).show();
            placeInfoSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });

        btnArrive.setOnClickListener(v -> {
            if (locationOverlay.getPosition() != null) {
                requestTmapPedestrianRoute(locationOverlay.getPosition(), destination);
            } else {
                Toast.makeText(this, "현재 위치를 알 수 없어 경로를 요청할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
            placeInfoSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });

        placeInfoSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }


    private void searchNearbyPlaces(String category) {
        if (naverMap == null) return;
        LatLng center = naverMap.getCameraPosition().target;

        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(center.latitude, center.longitude, 1);
                String query;
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    query = (address.getLocality() != null ? address.getLocality() + " " : "")
                            + (address.getSubLocality() != null ? address.getSubLocality() + " " : "")
                            + category;
                } else {
                    query = category;
                }

                String text = URLEncoder.encode(query, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/search/local.json?query=" + text + "&display=10&start=1&sort=random";

                Log.d(TAG, "Request URL: " + apiURL);

                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("X-Naver-Client-Id", NAVER_SEARCH_CLIENT_ID);
                con.setRequestProperty("X-Naver-Client-Secret", NAVER_SEARCH_CLIENT_SECRET);

                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) {
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }

                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();

                Log.d(TAG, "Response Code: " + responseCode);
                Log.d(TAG, "Response: " + response.toString());

                if (responseCode == 200) {
                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray items = jsonObject.getJSONArray("items");

                    runOnUiThread(() -> {
                        clearPlaceMarkers();
                        if (items.length() == 0) {
                            Toast.makeText(this, "주변에서 '" + category + "' 검색 결과를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (int i = 0; i < items.length(); i++) {
                            try {
                                JSONObject item = items.getJSONObject(i);
                                String title = item.getString("title").replaceAll("<[^>]*>", "");
                                String address = item.getString("address");
                                String itemCategory = item.getString("category");
                                double mapx = Double.parseDouble(item.getString("mapx"));
                                double mapy = Double.parseDouble(item.getString("mapy"));

                                double longitude = mapx / 10000000.0;
                                double latitude = mapy / 10000000.0;
                                LatLng latLng = new LatLng(latitude, longitude);

                                Marker marker = new Marker();
                                marker.setPosition(latLng);
                                marker.setCaptionText(title);
                                marker.setIcon(OverlayImage.fromResource(com.naver.maps.map.R.drawable.navermap_default_marker_icon_yellow));
                                marker.setMap(naverMap);

                                marker.setOnClickListener(overlay -> {
                                    showPlaceInfo(title, address, itemCategory, latLng);
                                    return true;
                                });
                                placeMarkers.add(marker);

                            } catch (Exception e) {
                                Log.e(TAG, "JSON parsing error", e);
                            }
                        }
                        // 줌 레벨 변경 없이 현재 상태 유지
                    });
                } else {
                    Log.e(TAG, "Naver Search API Error: " + response.toString());
                    runOnUiThread(() -> Toast.makeText(this, "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                Log.e(TAG, "searchNearbyPlaces error", e);
                runOnUiThread(() -> Toast.makeText(this, "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void requestTmapPedestrianRoute(LatLng start, LatLng end) {
        new Thread(() -> {
            try {
                String url = "https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&format=json&callback=result";
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("appKey", TMAP_APP_KEY);
                con.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("startX", String.valueOf(start.longitude));
                payload.put("startY", String.valueOf(start.latitude));
                payload.put("endX", String.valueOf(end.longitude));
                payload.put("endY", String.valueOf(end.latitude));
                payload.put("reqCoordType", "WGS84GEO");
                payload.put("resCoordType", "WGS84GEO");
                payload.put("startName", "출발지");
                payload.put("endName", "도착지");

                OutputStream os = con.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) {
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }

                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();

                Log.d(TAG, "TMAP Response: " + response.toString());

                if (responseCode == 200) {
                    List<LatLng> pathPoints = new ArrayList<>();
                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray features = jsonObject.getJSONArray("features");

                    int totalTime = 0;
                    double totalDistance = 0.0;

                    // 경로 요약 정보는 첫 번째 feature에서 가져옴
                    if (features.length() > 0) {
                        JSONObject firstFeature = features.getJSONObject(0);
                        if (firstFeature.has("properties")) {
                            JSONObject properties = firstFeature.getJSONObject("properties");
                            totalTime = properties.optInt("totalTime", 0);
                            totalDistance = properties.optDouble("totalDistance", 0.0);
                        }
                    }

                    for (int i = 0; i < features.length(); i++) {
                        JSONObject feature = features.getJSONObject(i);
                        JSONObject geometry = feature.getJSONObject("geometry");
                        String type = geometry.getString("type");
                        JSONArray coordinates = geometry.getJSONArray("coordinates");

                        if (type.equals("LineString")) {
                            for (int j = 0; j < coordinates.length(); j++) {
                                JSONArray coord = coordinates.getJSONArray(j);
                                pathPoints.add(new LatLng(coord.getDouble(1), coord.getDouble(0)));
                            }
                        }
                    }

                    int finalTotalTime = totalTime;
                    double finalTotalDistance = totalDistance;
                    runOnUiThread(() -> {
                        if (currentRouteOverlay != null) {
                            currentRouteOverlay.setMap(null);
                        }
                        if (!pathPoints.isEmpty()) {
                            currentRouteOverlay = new PolylineOverlay();
                            currentRouteOverlay.setCoords(pathPoints);
                            currentRouteOverlay.setWidth(10);
                            currentRouteOverlay.setColor(0xFF0000FF); // Blue color
                            currentRouteOverlay.setMap(naverMap);
                            naverMap.moveCamera(CameraUpdate.scrollTo(start));

                            // 경로 정보 패널 업데이트 및 표시
                            updateRouteInfoPanel(finalTotalTime, finalTotalDistance, R.drawable.ic_walk_icon);
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "TMAP 경로 탐색 실패: " + responseCode, Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                Log.e(TAG, "TMAP API Error", e);
                runOnUiThread(() -> Toast.makeText(this, "TMAP 경로 탐색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void requestTmapCarRoute(LatLng start, LatLng end, String searchOption) {
        new Thread(() -> {
            try {
                String url = "https://apis.openapi.sk.com/tmap/routes?version=1&format=json&callback=result";
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("appKey", TMAP_APP_KEY);
                con.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("startX", String.valueOf(start.longitude));
                payload.put("startY", String.valueOf(start.latitude));
                payload.put("endX", String.valueOf(end.longitude));
                payload.put("endY", String.valueOf(end.latitude));
                payload.put("reqCoordType", "WGS84GEO");
                payload.put("resCoordType", "WGS84GEO");
                payload.put("startName", "출발지");
                payload.put("endName", "도착지");
                if (searchOption != null && !searchOption.isEmpty()) {
                    payload.put("searchOption", searchOption);
                }

                OutputStream os = con.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) {
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }

                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();

                Log.d(TAG, "TMAP Car Route Response: " + response.toString());

                if (responseCode == 200) {
                    List<LatLng> pathPoints = new ArrayList<>();
                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray features = jsonObject.getJSONArray("features");

                    int totalTime = 0;
                    double totalDistance = 0.0;

                    // 경로 요약 정보는 첫 번째 feature에서 가져옴
                    if (features.length() > 0) {
                        JSONObject firstFeature = features.getJSONObject(0);
                        if (firstFeature.has("properties")) {
                            JSONObject properties = firstFeature.getJSONObject("properties");
                            totalTime = properties.optInt("totalTime", 0);
                            totalDistance = properties.optDouble("totalDistance", 0.0);
                        }
                    }

                    for (int i = 0; i < features.length(); i++) {
                        JSONObject feature = features.getJSONObject(i);
                        JSONObject geometry = feature.getJSONObject("geometry");
                        String type = geometry.getString("type");
                        JSONArray coordinates = geometry.getJSONArray("coordinates");

                        if (type.equals("LineString")) {
                            for (int j = 0; j < coordinates.length(); j++) {
                                JSONArray coord = coordinates.getJSONArray(j);
                                pathPoints.add(new LatLng(coord.getDouble(1), coord.getDouble(0)));
                            }
                        }
                    }

                    int finalTotalTime = totalTime;
                    double finalTotalDistance = totalDistance;
                    runOnUiThread(() -> {
                        if (currentRouteOverlay != null) {
                            currentRouteOverlay.setMap(null);
                        }
                        if (!pathPoints.isEmpty()) {
                            currentRouteOverlay = new PolylineOverlay();
                            currentRouteOverlay.setCoords(pathPoints);
                            currentRouteOverlay.setWidth(10);
                            currentRouteOverlay.setColor(0xFF0000FF);
                            currentRouteOverlay.setMap(naverMap);
                            naverMap.moveCamera(CameraUpdate.scrollTo(start));

                            // 경로 정보 패널 업데이트 및 표시
                            updateRouteInfoPanel(finalTotalTime, finalTotalDistance, R.drawable.ic_car_icon);
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "TMAP 자동차 경로 탐색 실패: " + responseCode, Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                Log.e(TAG, "TMAP Car API Error", e);
                runOnUiThread(() -> Toast.makeText(this, "TMAP 자동차 경로 탐색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // 경로 정보 패널 업데이트 및 표시 메서드
    private void updateRouteInfoPanel(int totalTimeSeconds, double totalDistanceMeters, int transportIconResId) {
        // 시간 포맷 (분 단위)
        int minutes = totalTimeSeconds / 60;
        tvTotalTime.setText(minutes + "분");

        // 거리 포맷 (KM 단위)
        String distanceKm = String.format(Locale.getDefault(), "%.1fkm", totalDistanceMeters / 1000);
        tvTotalDistance.setText(distanceKm);

        // 아이콘 설정
        ivTransportIcon.setImageResource(transportIconResId);

        // 패널 표시
        routeInfoPanel.setVisibility(View.VISIBLE);
    }

    // 경로 및 패널 숨기기 메서드
    private void clearRouteAndPanel() {
        if (currentRouteOverlay != null) {
            currentRouteOverlay.setMap(null);
            currentRouteOverlay = null;
        }
        routeInfoPanel.setVisibility(View.GONE);
    }


    private void clearPlaceMarkers() {
        for (Marker marker : placeMarkers) {
            marker.setMap(null);
        }
        placeMarkers.clear();
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

        placeInfoSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FAVORITES && resultCode == RESULT_OK && data != null) {
            String placeName = data.getStringExtra("placeName");
            if (placeName != null) {
                performGeocoding(placeName);
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