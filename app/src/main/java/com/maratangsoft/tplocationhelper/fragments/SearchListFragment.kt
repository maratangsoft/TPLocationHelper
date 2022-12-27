package com.maratangsoft.tplocationhelper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.maratangsoft.tplocationhelper.activities.MainActivity
import com.maratangsoft.tplocationhelper.adapters.PlaceListAdapter
import com.maratangsoft.tplocationhelper.databinding.FragmentSearchListBinding
import com.maratangsoft.tplocationhelper.model.Place

class SearchListFragment : Fragment() {
    private val binding by lazy { FragmentSearchListBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //MainActivity를 참조하기
        val ma = activity as MainActivity

        //아직 MainActivity에서 파싱이 안 끝났을 수도 있음
        if (ma.searchPlaceResponse==null) return

        binding.rv.adapter =
            ma.searchPlaceResponse?.documents?.let { PlaceListAdapter(requireContext(), it) }
    }
}