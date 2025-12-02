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

            // 주변 여러 위치의 주소를 가져옴 (동 정보를 찾기 위해)
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 5);

            if (addresses != null && !addresses.isEmpty()) {
                // 첫 번째 주소 정보 (기본값으로 사용)
                Address firstAddress = addresses.get(0);
                String firstRegion = null;
                
                // 동 정보를 수집 (투표 방식)
                java.util.Map<String, Integer> dongVotes = new java.util.HashMap<>();
                
                // 여러 주소 중에서 동 정보 수집
                for (Address address : addresses) {
                    String adminArea = address.getAdminArea();
                    String locality = address.getLocality();
                    String subLocality = address.getSubLocality();
                    String thoroughfare = address.getThoroughfare();
                    String featureName = address.getFeatureName();
                    String addressLine = address.getAddressLine(0);

                    Log.d(TAG, "--- Address " + addresses.indexOf(address) + " ---");
                    Log.d(TAG, "AdminArea: " + adminArea);
                    Log.d(TAG, "Locality: " + locality);
                    Log.d(TAG, "SubLocality: " + subLocality);
                    Log.d(TAG, "Thoroughfare: " + thoroughfare);
                    Log.d(TAG, "FeatureName: " + featureName);
                    Log.d(TAG, "AddressLine: " + addressLine);

                    String region = extractRegionFromAddress(adminArea, locality, subLocality, thoroughfare, featureName, addressLine);
                    
                    // 첫 번째 주소의 region 저장
                    if (firstRegion == null) {
                        firstRegion = region;
                    }
                    
                    // 동 정보가 있으면 투표에 추가
                    if (region != null && region.contains(" ") && (region.endsWith("동") || region.endsWith("리"))) {
                        dongVotes.put(region, dongVotes.getOrDefault(region, 0) + 1);
                        Log.d(TAG, "Found dong: " + region);
                    }
                }
                
                // 가장 많이 나온 동 찾기
                if (!dongVotes.isEmpty()) {
                    String mostCommonDong = null;
                    int maxVotes = 0;
                    
                    for (java.util.Map.Entry<String, Integer> entry : dongVotes.entrySet()) {
                        Log.d(TAG, "Dong vote: " + entry.getKey() + " = " + entry.getValue());
                        if (entry.getValue() > maxVotes) {
                            maxVotes = entry.getValue();
                            mostCommonDong = entry.getKey();
                        }
                    }
                    
                    Log.d(TAG, "Most common dong: " + mostCommonDong + " (" + maxVotes + " votes)");
                    return mostCommonDong;
                }
                
                // 동 정보가 없으면 첫 번째 주소 반환
                Log.d(TAG, "No dong info found, using first address: " + firstRegion);
                return firstRegion;
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
     * Address 정보에서 "구 동" 형태로 추출
     * 형태: "서원구 모충동" 또는 "분당구 수내동"
     */
    private String extractRegionFromAddress(String adminArea, String locality, String subLocality, String thoroughfare, String featureName, String addressLine) {
        try {
            StringBuilder region = new StringBuilder();
            
            // subLocality 추가 (서원구, 분당구 등)
            if (subLocality != null && !subLocality.isEmpty()) {
                region.append(subLocality);
            }
            
            // thoroughfare 또는 featureName에서 동 정보 추출
            String dongName = null;
            
            // thoroughfare 우선 확인
            if (thoroughfare != null && !thoroughfare.isEmpty()) {
                if (thoroughfare.endsWith("동") || thoroughfare.endsWith("리")) {
                    dongName = thoroughfare;
                }
            }
            
            // thoroughfare가 없으면 featureName 확인
            if (dongName == null && featureName != null && !featureName.isEmpty()) {
                if (featureName.endsWith("동") || featureName.endsWith("리")) {
                    dongName = featureName;
                }
            }
            
            // 둘 다 없으면 addressLine에서 파싱 시도
            if (dongName == null && addressLine != null && !addressLine.isEmpty()) {
                dongName = extractDongFromAddressLine(addressLine);
            }
            
            // 동 이름 추가
            if (dongName != null) {
                if (region.length() > 0) region.append(" ");
                region.append(dongName);
            }
            
            String result = region.toString().trim();
            if (!result.isEmpty()) {
                return result;
            }
            
            // subLocality도 없으면 locality 사용
            if (locality != null && !locality.isEmpty()) {
                return locality;
            }
            
            // 최후의 수단으로 adminArea 사용
            if (adminArea != null && !adminArea.isEmpty()) {
                return adminArea;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in extractRegionFromAddress", e);
        }
        return "미분류";
    }

    /**
     * 전체 주소 문자열에서 동/리 추출
     * 예: "충청북도 청주시 서원구 모충동 123" → "모충동"
     */
    private String extractDongFromAddressLine(String addressLine) {
        try {
            // 공백으로 분리
            String[] parts = addressLine.split(" ");
            
            // 뒤에서부터 검색 (숫자나 특수문자 제외)
            for (int i = parts.length - 1; i >= 0; i--) {
                String part = parts[i];
                // "동" 또는 "리"로 끝나고, 숫자로 시작하지 않는 경우
                if ((part.endsWith("동") || part.endsWith("리")) && !Character.isDigit(part.charAt(0))) {
                    return part;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in extractDongFromAddressLine", e);
        }
        return null;
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
