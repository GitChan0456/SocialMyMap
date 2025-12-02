package com.example.socialmymap;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class LocationHelperExtension {
    private static final String TAG = "LocationHelperExt";
    
    /**
     * 좌표에서 locality(시) 정보만 추출
     */
    public static String getCityFromCoordinates(Geocoder geocoder, double latitude, double longitude) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locality = address.getLocality();  // 예: "대전시", "청주시"
                
                Log.d(TAG, "Locality: " + locality);
                
                if (locality != null && !locality.isEmpty()) {
                    return locality;
                }
                
                // locality가 없으면 adminArea에서 추출
                String adminArea = address.getAdminArea();
                if (adminArea != null && !adminArea.isEmpty()) {
                    // "특별시", "광역시" 등 제거
                    return adminArea.replace("특별시", "").replace("광역시", "")
                            .replace("특별자치시", "").replace("특별자치도", "").replace("도", "").trim();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder error", e);
        }
        return null;
    }
}
