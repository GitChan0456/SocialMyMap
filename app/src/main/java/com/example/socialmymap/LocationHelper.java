package com.example.socialmymap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * 위치 서비스 유틸리티
 * - GPS 위치 획득
 * - 역지오코딩 (Android Geocoder 사용)
 * - 거리 계산
 */
public class LocationHelper {
    private static final String TAG = "LocationHelper";
    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final Geocoder geocoder;

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.geocoder = new Geocoder(context, Locale.KOREA);
    }

    /**
     * 현재 GPS 위치 획득
     */
    public Task<Location> getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return fusedLocationClient.getLastLocation();
    }

    /**
     * Android Geocoder를 사용한 역지오코딩
     * 좌표 -> 주소
     */
    public String reverseGeocode(double latitude, double longitude) {
        try {
            Log.d(TAG, "Reverse geocoding: " + latitude + ", " + longitude);

            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // 시/도 + 구/군 추출
                String adminArea = address.getAdminArea(); // 시/도 (예: 서울특별시, 경기도)
                String locality = address.getLocality(); // 시/군/구 (예: 성남시)
                String subLocality = address.getSubLocality(); // 구/읍/면 (예: 분당구)

                Log.d(TAG, "AdminArea: " + adminArea);
                Log.d(TAG, "Locality: " + locality);
                Log.d(TAG, "SubLocality: " + subLocality);

                String region = extractRegionFromAddress(adminArea, locality, subLocality);
                Log.d(TAG, "Extracted region: " + region);

                return region;
            } else {
                Log.e(TAG, "No addresses found");
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder IOException", e);
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Exception in reverseGeocode", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Address 정보에서 "시 구" 형태로 추출
     */
    private String extractRegionFromAddress(String adminArea, String locality, String subLocality) {
        try {
            // adminArea 정리 (특별시, 광역시 등 제거)
            String city = "";
            if (adminArea != null) {
                city = adminArea.replace("특별시", "").replace("광역시", "")
                        .replace("특별자치시", "").replace("특별자치도", "").replace("도", "").trim();
            }

            // 구/군 정보
            String district = "";
            if (subLocality != null) {
                district = subLocality; // 예: 분당구
            } else if (locality != null && !locality.equals(city)) {
                district = locality; // 예: 성남시
            }

            if (!city.isEmpty() && !district.isEmpty()) {
                return city + " " + district;
            } else if (!city.isEmpty()) {
                return city;
            } else if (!district.isEmpty()) {
                return district;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in extractRegionFromAddress", e);
        }
        return "미분류";
    }

    /**
     * 주소에서 지역 추출
     * 예: "서울특별시 강남구 테헤란로" -> "서울 강남구"
     */
    public String extractRegion(String fullAddress) {
        if (fullAddress == null || fullAddress.isEmpty()) {
            return "미분류";
        }

        try {
            String[] parts = fullAddress.split(" ");
            if (parts.length >= 2) {
                String city = parts[0].replace("특별시", "").replace("광역시", "")
                        .replace("특별자치시", "").replace("특별자치도", "");
                String district = parts[1];
                return city + " " + district;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "미분류";
    }

    /**
     * 두 지점 간 거리 계산 (미터)
     */
    public static float calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0];
    }

    /**
     * 위치 -> 지역명 변환
     */
    public String getRegionFromLocation(double latitude, double longitude) {
        String region = reverseGeocode(latitude, longitude);
        if (region == null || region.isEmpty()) {
            return "미분류";
        }
        return region;
    }
}
