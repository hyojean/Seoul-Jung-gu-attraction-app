package ddwu.mobile.finalproject.ma01_20181801;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    Place lPlace;
    TextView mkTitle;
    TextView mkAddr;
    ImageView mkImage;
    Place mkPlace;

    //Parsing
    //EditText etTarget;
    ListView lvList;
    String apiAddress;

    String query;

    PlaceAdapter adapter;
    ArrayList<Place> resultList;
    PlaceXmlParser parser;
    ImageFileManager imgManager;

    //DB
    PlaceDB placeDB;
    PlaceDAO placeDAO;
    MyPlaceDB myPlaceDB;
    MyPlaceDAO myPlaceDAO;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    //구글맵
    final int REQ_PERMISSION_CODE = 100;

    FusedLocationProviderClient flpClient;
    Location mLastLocation;

    private GoogleMap mGoogleMap;       // 지도 객체
    private Marker mCenterMarker;         // 중앙 표시 Marker
    private Marker mPoiMarker;
    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mkTitle = findViewById(R.id.mkTitle);
        mkAddr = findViewById(R.id.mkAddr);
        mkImage = findViewById(R.id.mkImage);

        lvList = findViewById(R.id.lvList2);

        //DB
        placeDB = PlaceDB.getDatabase(this);
        placeDAO = placeDB.placeDAO();
        myPlaceDB = MyPlaceDB.getDatabase(this);
        myPlaceDAO = myPlaceDB.myplaceDAO();

        resultList = new ArrayList<>();
        adapter = new PlaceAdapter(this, R.layout.listview_place, resultList);
        lvList.setAdapter(adapter);

        apiAddress = "http://apis.data.go.kr/" +
                "B551011/KorService/areaBasedList" +
                "?numOfRows=100&MobileOS=ETC&MobileApp=AppTest&ServiceKey=J%2Bn0sQmI26t%2FkmZvkOF3A0B2nmf27F4YwvTTpH2GCH0VcnZMCTHOFeEKhjKGlscyCYerYbL8%2F0IEsnSnVYWVnA%3D%3D&listYN=Y&arrange=O&contentTypeId=12&areaCode=1" +
                "&sigunguCode=";
        parser = new PlaceXmlParser();
        imgManager = new ImageFileManager(this);

        flpClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment      // map 객체 생성
                = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mapReadyCallback);

        //위치
        checkPermission();
        //위치 확인 시작
        flpClient.requestLocationUpdates(
                getLocationRequest(),
                mLocCallback,
                Looper.getMainLooper()
        );

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (placeDAO.getPlaceById(1) != null) {
                    lPlace = placeDAO.getPlaceById(1);
                    Log.d("첫번째 title", lPlace.getTitle());
                    Log.d("첫번째 place", lPlace.toString());
                } else
                    Log.d("첫번째 place", "null");
            }
        }).start();

        lvList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Glide 를 사용하여 이미지 파일을 외장메모리에 저장
                // 해당 부분은 파일매니저로 분리 필요

                Glide.with(MainActivity.this)
                        .asBitmap()
                        .load(resultList.get(position).getImage1())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                // 파일 처리 클래스로 분리 필요

                                if (isExternalStorageWritable()) {
                                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                                            "myalbum");
                                    if (!file.mkdirs()) {
                                        Log.d(TAG, "directory not created");
                                    }
                                    File saveFile = new File(file.getPath(), "test.jpg");
                                    try {
                                        FileOutputStream fos = new FileOutputStream((saveFile));
                                        resource.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                        fos.flush();
                                        fos.close();
                                        Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });

                //my attraction list에 저장
                try {
                    Place myListPlace = new mkTask().execute(resultList.get(position).getTitle()).get();
                    Log.d("myListPlace", myListPlace.toString());
                    Single<Long> insertResult = myPlaceDAO.insertMyPlace(new MyPlace(
                            myListPlace.get_id(),
                            myListPlace.getAddr1(),
                            myListPlace.getAddr2(),
                            myListPlace.getImage2(),
                            myListPlace.getImage2(),
                            myListPlace.getMapX(),
                            myListPlace.getMapY(),
                            myListPlace.getSigunguCode(),
                            myListPlace.getTitle(),
                            myListPlace.getImageFileName()
                    ));

                    mDisposable.add(
                            insertResult.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(result -> Log.d(TAG, "Insertion success: " + result),
                                            throwable -> Log.d(TAG, "error")));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return true;
            }
        });

    }


    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mGoogleMap = googleMap;

//            지도 초기 위치 이동
            LatLng latLng = new LatLng(37.5675596477, 126.9765267272);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

//            지도 중심 마커 추가
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title("현위치")
                    .snippet("이동중");

            mCenterMarker = mGoogleMap.addMarker(markerOptions);
            mCenterMarker.showInfoWindow();

            // 마커 클릭 이벤트
            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(@NonNull Marker lMarker) {
                    currentMarker = lMarker;
                    Log.d("currentMarker.getTitle()", currentMarker.getTitle());

                    //마커 클릭 시 화면에 출력
                    if (!currentMarker.getTitle().equals("현위치")) {
                        try {
                            Place markerPlace = new mkTask().execute(currentMarker.getTitle()).get();
                            if (markerPlace != null) {
                                mkTitle.setText(markerPlace.getTitle());
                                mkAddr.setText(markerPlace.getAddr1() + " " + markerPlace.getAddr2());
                                Glide.with(MainActivity.this)
                                        .load(markerPlace.getImage1())
                                        .into(mkImage);
                            } else {
                                Log.d("markerPlace", "null");
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });

        }
    };


    class mkTask extends AsyncTask<String, Void, Place> {
        @Override
        protected Place doInBackground(String... inputParams) {
            Place mkTaskPlace = null;
            String mkTitle = inputParams[0];
            if (placeDAO.getPlaceByTitle(mkTitle) != null) {
                mkTaskPlace = placeDAO.getPlaceByTitle(mkTitle);
                Log.d("marker.getTitle()", mkTitle);
                Log.d("place.toString()", mkTaskPlace.toString());
            }
            return mkTaskPlace;
        }
    }


    LocationCallback mLocCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location loc : locationResult.getLocations()) {
//                지도 위치 이동
                mLastLocation = loc;
                LatLng currentLoc = new LatLng(37.563177, 126.9873604); //명동성당
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 13));

//                지도 마커 위치 이동
                mCenterMarker.setPosition(currentLoc);
            }
        }
    };


    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        imgManager.clearSaveFilesOnInternal();
        //Rxjava
        mDisposable.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        flpClient.removeLocationUpdates(mLocCallback);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSearch:
                //Parsing
                query = "24";
                try {
                    new PlaceAsyncTask().execute(apiAddress + URLEncoder.encode(query, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (resultList != null) {
                    adapter.setList(resultList);
                    adapter.notifyDataSetChanged();
                }

                break;
            case R.id.btnList:
                Intent intent = new Intent(MainActivity.this, MyPlaceActivity.class);
                startActivity(intent);
                break;
        }
    }


    //위치
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "위치권한 획득 완료", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "위치권한 미획득", Toast.LENGTH_SHORT).show();
                }
        }
    }


    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            // 권한이 있을 경우 수행할 동작
            Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
        } else {
            // 권한 요청
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION_CODE);
        }
    }


    private void getLastLocation() {
        checkPermission();
        flpClient.getLastLocation().addOnSuccessListener(
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            mLastLocation = location;
                        } else {
                            Toast.makeText(MainActivity.this, "No location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        flpClient.getLastLocation().addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unknown");
                    }
                }
        );

    }


    //Open Api Parsing
    class PlaceAsyncTask extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDlg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDlg = ProgressDialog.show(MainActivity.this, "Wait", "Downloading...");
        }

        @Override
        protected String doInBackground(String... strings) {
            String address = strings[0];
            String result = downloadContents(address);
            if (result == null) return "Error!";
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, result);

            resultList = parser.parse(result);      // 파싱 수행

            adapter.setList(resultList);    // Adapter 에 파싱 결과를 담고 있는 ArrayList 를 설정
            adapter.notifyDataSetChanged();

            progressDlg.dismiss();

            LatLng poiLatLng;

            for (int i = 0; i < resultList.size(); i++) {
//              poiList 의 POI 로 마커 추가 기능 수행
                //POI의 마커 정보 지정
                poiLatLng = new LatLng(Double.parseDouble(resultList.get(i).getMapY()), Double.parseDouble(resultList.get(i).getMapX()));
                MarkerOptions poiMarkerOptions = new MarkerOptions();
                poiMarkerOptions.position(poiLatLng);
                poiMarkerOptions.title(resultList.get(i).getTitle());
                poiMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                //지도에 마커 추가 후 추가한 마커 정보 기록
                mPoiMarker = mGoogleMap.addMarker(poiMarkerOptions);
                mPoiMarker.setTag(resultList.get(i).get_id());
                mPoiMarker.showInfoWindow();
            }

            //db에 Place insert
            if (lPlace == null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < resultList.size(); i++) {
                            Log.d("resultList.get(i) 출력 : ", resultList.get(i).toString());

                            long result = placeDAO.insertPlace(new Place(
                                    resultList.get(i).getAddr1(),
                                    resultList.get(i).getAddr2(),
                                    resultList.get(i).getImage1(),
                                    resultList.get(i).getImage2(),
                                    resultList.get(i).getMapX(),
                                    resultList.get(i).getMapY(),
                                    resultList.get(i).getSigunguCode(),
                                    resultList.get(i).getTitle()
                            ));
                            Log.d(TAG, "Insert Result: " + result);
                            lPlace = resultList.get(0);     // 필요
                        }
                    }
                }).start();
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (placeDAO.getAllPlaces() != null) {
                            List<Place> placeList = placeDAO.getAllPlaces();
                            for (Place place : placeList) {
                                Log.d("placeDAO.getAllPlaces()", place.toString());
                            }
                        }
                    }
                }).start();
            }

        }

        /* 네트워크 관련 메소드 */
        /* 네트워크 환경 조사 */
        private boolean isOnline() {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        }

        /* URLConnection 을 전달받아 연결정보 설정 후 연결, 연결 후 수신한 InputStream 반환
         * 네이버용을 수정 - ClientID, ClientSeceret 추가 strings.xml 에서 읽어옴*/
        private InputStream getNetworkConnection(HttpURLConnection conn) throws Exception {

            // 클라이언트 아이디 및 시크릿 그리고 요청 URL 선언
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(3000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("X-Naver-Client-Id", getResources().getString(R.string.client_id));
            conn.setRequestProperty("X-Naver-Client-Secret", getResources().getString(R.string.client_secret));

            if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + conn.getResponseCode());
            }

            return conn.getInputStream();
        }

        /* InputStream을 전달받아 문자열로 변환 후 반환 */
        protected String readStreamToString(InputStream stream) {
            StringBuilder result = new StringBuilder();

            try {
                InputStreamReader inputStreamReader = new InputStreamReader(stream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String readLine = bufferedReader.readLine();

                while (readLine != null) {
                    result.append(readLine + "\n");
                    readLine = bufferedReader.readLine();
                }

                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }

        /* 주소(address)에 접속하여 문자열 데이터를 수신한 후 반환 */
        protected String downloadContents(String address) {
            HttpURLConnection conn = null;
            InputStream stream = null;
            String result = null;

            try {
                URL url = new URL(address);
                conn = (HttpURLConnection) url.openConnection();
                stream = getNetworkConnection(conn);
                result = readStreamToString(stream);
                if (stream != null) stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }

            return result;
        }

    }
}
