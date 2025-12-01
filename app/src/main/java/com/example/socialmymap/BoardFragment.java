package com.example.socialmymap;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class BoardFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST = 200;

    public static final int REQUEST_CODE_WRITE_POST = 101;
    public static final int REQUEST_CODE_POST_DETAIL = 102;

    private PostAdapter adapter;
    private final List<Post> posts = new ArrayList<>();
    private CommunityDao communityDao;
    private LocationHelper locationHelper;
    private Spinner spinnerFilter;

    private String currentFilterMode = "전체";
    private Location currentLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);

        communityDao = new CommunityDao(requireContext());
        locationHelper = new LocationHelper(requireContext());

        RecyclerView rvPosts = view.findViewById(R.id.rv_posts_fragment);
        adapter = new PostAdapter(posts, this);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        // 지역 필터 Spinner 설정
        spinnerFilter = view.findViewById(R.id.spinner_region_filter);
        String[] filterOptions = { "전체", "우리 동네", "현재 위치", "주변 5km" };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, filterOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilterMode = filterOptions[position];
                loadPostsWithFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        loadPostsWithFilter();
        return view;
    }

    private void loadPostsWithFilter() {
        switch (currentFilterMode) {
            case "전체":
                loadAllPosts();
                break;
            case "우리 동네":
                loadHomeRegionPosts();
                break;
            case "현재 위치":
                loadCurrentLocationPosts();
                break;
            case "주변 5km":
                loadNearbyPosts();
                break;
        }
    }

    private void loadAllPosts() {
        posts.clear();
        posts.addAll(communityDao.getAllPosts());
        adapter.notifyDataSetChanged();
    }

    private void loadHomeRegionPosts() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", requireActivity().MODE_PRIVATE);
        String homeRegion = prefs.getString("home_region", null);

        if (homeRegion == null || homeRegion.isEmpty()) {
            Toast.makeText(requireContext(), "설정에서 우리 동네를 먼저 설정해주세요", Toast.LENGTH_SHORT).show();
            spinnerFilter.setSelection(0); // 전체로 변경
            return;
        }

        posts.clear();
        posts.addAll(communityDao.getPostsByRegion(homeRegion));
        adapter.notifyDataSetChanged();
    }

    private void loadCurrentLocationPosts() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        Task<Location> locationTask = locationHelper.getCurrentLocation();
        if (locationTask != null) {
            locationTask.addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = location;
                    new Thread(() -> {
                        String region = locationHelper.getRegionFromLocation(
                                location.getLatitude(), location.getLongitude());
                        requireActivity().runOnUiThread(() -> {
                            posts.clear();
                            posts.addAll(communityDao.getPostsByRegion(region));
                            adapter.notifyDataSetChanged();
                        });
                    }).start();
                } else {
                    Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다", Toast.LENGTH_SHORT).show();
                    spinnerFilter.setSelection(0);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "위치 가져오기 실패", Toast.LENGTH_SHORT).show();
                spinnerFilter.setSelection(0);
            });
        }
    }

    private void loadNearbyPosts() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        Task<Location> locationTask = locationHelper.getCurrentLocation();
        if (locationTask != null) {
            locationTask.addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = location;
                    filterByDistance(location, 5000); // 5km
                } else {
                    Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다", Toast.LENGTH_SHORT).show();
                    spinnerFilter.setSelection(0);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "위치 가져오기 실패", Toast.LENGTH_SHORT).show();
                spinnerFilter.setSelection(0);
            });
        }
    }

    private void filterByDistance(Location userLocation, float maxDistanceMeters) {
        List<Post> allPosts = communityDao.getAllPosts();
        List<Post> nearbyPosts = new ArrayList<>();

        for (Post post : allPosts) {
            if (post.latitude != 0 || post.longitude != 0) {
                float distance = LocationHelper.calculateDistance(
                        userLocation.getLatitude(), userLocation.getLongitude(),
                        post.latitude, post.longitude);
                if (distance <= maxDistanceMeters) {
                    nearbyPosts.add(post);
                }
            }
        }

        posts.clear();
        posts.addAll(nearbyPosts);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (communityDao != null) {
            loadPostsWithFilter();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == REQUEST_CODE_WRITE_POST || requestCode == REQUEST_CODE_POST_DETAIL) {
            loadPostsWithFilter();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadPostsWithFilter();
            } else {
                Toast.makeText(requireContext(), "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                spinnerFilter.setSelection(0); // 전체로 변경
            }
        }
    }
}
