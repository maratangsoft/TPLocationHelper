package com.maratangsoft.tplocationhelper.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.maratangsoft.tplocationhelper.activities.MainActivity
import com.maratangsoft.tplocationhelper.activities.PlaceUrlActivity
import com.maratangsoft.tplocationhelper.databinding.FragmentSearchMapBinding
import com.maratangsoft.tplocationhelper.model.Place
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import net.daum.mf.map.api.MapView.POIItemEventListener

class SearchMapFragment : Fragment() {
    private val binding by lazy { FragmentSearchMapBinding.inflate(layoutInflater) }
    val mapView by lazy { MapView(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //맵뷰를 뷰그룹에 추가하여 화면에 배치하도록
        binding.containerMv.addView(mapView)

        //마커or말풍선에 리스너 붙이는 건 반드시 마커 설정보다 먼저 해야 함
        mapView.setPOIItemEventListener(markerEventListener)

        //지도 관련 설정들...
        setMapAndMarkers()
    }

    private fun setMapAndMarkers(){
        //맵 중심점을 내 위치로 변경
        //현재 내 위치정보는 MainActivity의 멤버변수로 저장되어 있음
        val latitude:Double = (requireActivity() as MainActivity).mylocation?.latitude ?: 37.0
        val longitude:Double = (requireActivity() as MainActivity).mylocation?.longitude ?: 127.0

        val myMapPoint:MapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
        mapView.setMapCenterPointAndZoomLevel(myMapPoint, 5, true)
        mapView.zoomIn(true)
        mapView.zoomOut(true)

        //내 위치에 마커 표시
        val myMarker = MapPOIItem()
        myMarker.apply {
            itemName = "ME"
            mapPoint = myMapPoint
            markerType = MapPOIItem.MarkerType.BluePin
            selectedMarkerType = MapPOIItem.MarkerType.YellowPin
        }
        mapView.addPOIItem(myMarker)

        //검색결과 장소들... 마커들을 추가하기
        val documents = (requireActivity() as MainActivity).searchPlaceResponse?.documents
        documents?.forEach {
            val point = MapPoint.mapPointWithGeoCoord(it.y.toDouble(), it.x.toDouble())
            //마커옵션 객체를 만들어서 기본 설정하기
            val placeMarker = MapPOIItem().apply {
                itemName = it.place_name
                mapPoint = point
                markerType = MapPOIItem.MarkerType.RedPin
                selectedMarkerType = MapPOIItem.MarkerType.YellowPin

                //마커에 표시되지는 않지만 저장하고 싶은 데이터가 있을 때 쓰라고 userObject객체가 있음
                userObject = it
            }
            mapView.addPOIItem(placeMarker)
        }
    }
    //마커나 말풍선의 클릭 이벤트에 반응하는 리스너
    private val markerEventListener = object : POIItemEventListener {
        override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {

        }
        //deprecated
        override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {

        }
        //말풍선 클릭시 반응하는 콜백메소드
        override fun onCalloutBalloonOfPOIItemTouched(
            p0: MapView?,
            p1: MapPOIItem?,
            p2: MapPOIItem.CalloutBalloonButtonType?
        ) {
            //2번째 파라미터 p1 == 클릭한 말풍선의 마커 객체
            if (p1?.userObject == null) return
            var place = p1.userObject as Place

            //장소의 상세정보 URL을 가지고 상세정보 웹페이지를 보여주는 화면으로 전환시키기
            val intent = Intent(requireContext(), PlaceUrlActivity::class.java)
            intent.putExtra("place_url", place.place_url)
            startActivity(intent)
        }
        override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {

        }

    }
}