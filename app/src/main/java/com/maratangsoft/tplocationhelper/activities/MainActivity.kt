package com.maratangsoft.tplocationhelper.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.maratangsoft.tplocationhelper.R
import com.maratangsoft.tplocationhelper.databinding.ActivityMainBinding
import com.maratangsoft.tplocationhelper.fragments.SearchListFragment
import com.maratangsoft.tplocationhelper.fragments.SearchMapFragment
import com.maratangsoft.tplocationhelper.model.KakaoPlaceResponse
import com.maratangsoft.tplocationhelper.network.RetrofitHelper
import com.maratangsoft.tplocationhelper.network.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//import com.maratangsoft.tplocationhelper.R
// 타 패키지에 있는 Resource위치를 못알아보므로, R장부를 호출해야 한다면 import 해줘야 함

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    var searchQuery = "화장실" //검색 장소명
    var mylocation: Location? = null //내 위치정보

    //Google Fused API
    val providerClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    //카카오 장소검색 결과: 프래그먼트에서 써야 하므로 멤버변수에 둔다
    //부모 액티비티의 멤버변수는 프래그먼트에서 받아서 쓸 수 있음
    var searchPlaceResponse: KakaoPlaceResponse? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportFragmentManager.beginTransaction().add(R.id.frag_container, SearchListFragment()).commit()
        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text){
                    "LIST" -> {
                        supportFragmentManager.beginTransaction().replace(R.id.frag_container, SearchListFragment()).commit()
                    }
                    "MAP" -> {
                        supportFragmentManager.beginTransaction().replace(R.id.frag_container, SearchMapFragment()).commit()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        //소프트키보드 검색버튼
        binding.etSearch.setOnEditorActionListener { textView, i, keyEvent ->
            searchQuery = binding.etSearch.text.toString()
            searchPlaces()
            //return값 true: 액션버튼 클릭시 여기서 모든 액션을 소모한다. false: 소모하지 않는다 -> 그냥 false해라
           false
        }

        setChoiceButtonsListener()

        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_DENIED){
            //퍼미션 거부됨
            requestPermissions(permissions, 10)
        }else{
            //퍼미션 허가됨
            requestMyLocation()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_help -> Toast.makeText(this, "help", Toast.LENGTH_SHORT).show()
            R.id.menu_logout -> Toast.makeText(this, "logout", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            requestMyLocation()
        }else{
            Toast.makeText(this, "위치정보 요청 거부됨", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun requestMyLocation(){
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        providerClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }
    
    //위치정보 갱신될 때마다 발동하는 콜백 객체
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            mylocation = p0.lastLocation
            //위치탐색이 끝났으니 내 위치 업데이트를 종료
            providerClient.removeLocationUpdates(this) //this: locationCallback
            searchPlaces()
        }
    }

    fun searchPlaces(){
        //검색에 필요한 요청변수들: 검색어, 내 위치
        Log.d("ttt", "${searchQuery}: ${mylocation?.latitude}, ${mylocation?.longitude}")
        RetrofitHelper.getInstance("https://dapi.kakao.com")
            .create(RetrofitService::class.java)
            .getKakaoPlace(searchQuery, mylocation?.longitude.toString(), mylocation?.latitude.toString())
            .enqueue(object : Callback<KakaoPlaceResponse> {
                override fun onResponse(
                    call: Call<KakaoPlaceResponse>,
                    response: Response<KakaoPlaceResponse>
                ) {
                    //응답된 json 문자열을 파싱한 객체 참조하기
                    searchPlaceResponse = response.body()
                    var meta = searchPlaceResponse?.meta
                    var documents = searchPlaceResponse?.documents
                    //AlertDialog.Builder(this@MainActivity).setMessage("${meta?.total_count}\n${documents?.get(0)?.place_name}").show()
                    
                    //무조건 검색이 완료되면 List Fragment를 먼저 보여주기
                    supportFragmentManager.beginTransaction().replace(R.id.frag_container, SearchListFragment()).commit()
                    //탭버튼 위치 변경
                    binding.tabLayout.getTabAt(0)?.select()
                }

                override fun onFailure(call: Call<KakaoPlaceResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "서버 오류가 있습니다./n잠시 뒤 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setChoiceButtonsListener(){
        binding.choiceList.choiceToliet.setOnClickListener { clickChoice(it) }
        binding.choiceList.choiceParking.setOnClickListener { clickChoice(it) }
        binding.choiceList.choiceMovie.setOnClickListener { clickChoice(it) }
        binding.choiceList.choiceGas.setOnClickListener { clickChoice(it) }
        binding.choiceList.choiceEv.setOnClickListener { clickChoice(it) }
        binding.choiceList.choicePharmacy.setOnClickListener { clickChoice(it) }
        binding.choiceList.choicePark.setOnClickListener { clickChoice(it) }
        binding.choiceList.choiceFood.setOnClickListener { clickChoice(it) }
        binding.choiceList.choicePcbang.setOnClickListener { clickChoice(it) }
        binding.choiceList.choiceKaraoke.setOnClickListener { clickChoice(it) }
    }

    var prevChoiceId = R.id.choice_toliet //이전에 선택했던 뷰 ID
    private fun clickChoice(view: View){
        //이전에 선택했던 뷰의 배경 변경
        findViewById<ImageView>(prevChoiceId).setBackgroundResource(R.drawable.bg_choice)
        //현재 선택한 뷰의 배경 변경
        view.setBackgroundResource(R.drawable.bg_choice_selected)
        //이전 선택 뷰 ID 변수 변경
        prevChoiceId = view.id

        when (view.id){
            R.id.choice_toliet -> searchQuery = "화장실"
            R.id.choice_parking -> searchQuery = "주차장"
            R.id.choice_movie -> searchQuery = "영화관"
            R.id.choice_gas -> searchQuery = "주유소"
            R.id.choice_ev -> searchQuery = "전기차충전소"
            R.id.choice_pharmacy -> searchQuery = "약국"
            R.id.choice_park -> searchQuery = "공원"
            R.id.choice_food -> searchQuery = "맛집"
            R.id.choice_pcbang -> searchQuery = "피씨방"
            R.id.choice_karaoke -> searchQuery = "노래방"
        }
        searchPlaces()
        binding.etSearch.text.clear()
        binding.etSearch.clearFocus()
    }
}