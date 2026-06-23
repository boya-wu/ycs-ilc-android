package com.yuchens.equipinspectandroid.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.AdapterView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.yuchens.equipinspectandroid.R
import com.yuchens.equipinspectandroid.databinding.FragmentInspectedBinding
import com.yuchens.equipinspectandroid.ui.adapter.EquipCardAdapter
import com.yuchens.equipinspectandroid.ui.base.BaseFragment
import com.yuchens.equipinspectandroid.ui.model.EquipUi
import com.yuchens.equipinspectandroid.ui.model.Option
import com.yuchens.equipinspectandroid.ui.viewmodel.InspectedViewModel
import com.yuchens.equipinspectandroid.util.LogHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InspectedFragment : BaseFragment() {

    private val vm: InspectedViewModel by viewModels()

    private var _binding: FragmentInspectedBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EquipCardAdapter

    // 原始資料（未篩選）
    private var allItems: List<EquipUi> = emptyList()

    // 下拉資料來源與 Adapters
    private lateinit var positionOptions: List<Option>
    private lateinit var itemOptions: List<Option>
    private lateinit var positionAdapter: ArrayAdapter<Option>
    private lateinit var itemAdapter: ArrayAdapter<Option>

    // 目前篩選值（value）
    private var selectedPosition: String = "全部"
    private var selectedItem: String = "全部"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInspectedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView + ListAdapter
        binding.recyclerEquip.layoutManager = LinearLayoutManager(requireContext())
        adapter = EquipCardAdapter { equip ->
            com.yuchens.equipinspectandroid.ui.widget.EquipDetailBottomSheet
                .newInstance(equip)
                .show(childFragmentManager, "equip_detail")
        }
        binding.recyclerEquip.adapter = adapter

        // 先建立空資料的 Spinner Adapter
        positionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPosition.adapter = positionAdapter

        itemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
        itemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerItem.adapter = itemAdapter

        // Spinner 事件
        binding.spinnerPosition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                selectedPosition = (binding.spinnerPosition.selectedItem as? Option)?.value ?: "全部"
                applyFilter()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.spinnerItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                selectedItem = (binding.spinnerItem.selectedItem as? Option)?.value ?: "全部"
                applyFilter()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 觸發載入
        vm.load()

        // 收集狀態
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { state ->
                    allItems = state.items

                    // 每次資料更新時重建下拉選單
                    rebuildFilterOptions(allItems)

                    // 套用目前的篩選條件
                    applyFilter()

                    if (!state.isLoading && state.error == null) {
                        if (state.items.isEmpty()) {
                            Toast.makeText(requireContext(), "沒有已巡檢設備", Toast.LENGTH_SHORT).show()
                        } else {
                            val count = state.items.size
                            Toast.makeText(requireContext(), "已巡檢設備共 $count 筆", Toast.LENGTH_SHORT).show()
                        }
                    }

                    state.error?.let { msg ->
                        LogHelper.write(requireContext(), "Inspected load failed: $msg")
                    }
                }
            }
        }

        // 隨滾動隱藏/顯示上下工具列（維持你原來的行為）
        val topNav = requireActivity().findViewById<View>(R.id.topNavFragment)
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNavFragment)
        setupRecyclerViewScrollBehavior(binding.recyclerEquip, topNav, bottomNav)
    }

    /**
     * 重建兩個下拉選單的 Options。
     * 位置：text/value = "$buildingName-$areaName"
     * 項目：text/value = "$categoryName-$itemName"
     * 兩者第一個選項固定「全部」。
     */
    private fun rebuildFilterOptions(items: List<EquipUi>) {
        val distinctPositions = items
            .map { "${it.buildingName.orEmpty()}-${it.areaName.orEmpty()}" }
            .toSet()
            .toList()
            .sorted()

        val distinctItems = items
            .map { "${it.categoryName.orEmpty()}-${it.itemName.orEmpty()}" }
            .toSet()
            .toList()
            .sorted()

        val all = listOf(Option("全部", "全部"))
        positionOptions = all + distinctPositions.map { key -> Option(key, key) }
        itemOptions = all + distinctItems.map { key -> Option(key, key) }

        // 更新 Adapter 內容
        positionAdapter.clear()
        positionAdapter.addAll(positionOptions)

        itemAdapter.clear()
        itemAdapter.addAll(itemOptions)

        // 對齊目前選取值（若不在清單中則重置為「全部」）
        if (positionOptions.none { it.value == selectedPosition }) {
            selectedPosition = "全部"
            binding.spinnerPosition.setSelection(0, false)
        } else {
            val idx = positionOptions.indexOfFirst { it.value == selectedPosition }
            if (idx >= 0) binding.spinnerPosition.setSelection(idx, false)
        }

        if (itemOptions.none { it.value == selectedItem }) {
            selectedItem = "全部"
            binding.spinnerItem.setSelection(0, false)
        } else {
            val idx = itemOptions.indexOfFirst { it.value == selectedItem }
            if (idx >= 0) binding.spinnerItem.setSelection(idx, false)
        }
    }

    /** 依目前兩個下拉條件做 AND 篩選 */
    private fun applyFilter() {
        val filtered = allItems.filter { ui ->
            val positionKey = "${ui.buildingName.orEmpty()}-${ui.areaName.orEmpty()}"
            val itemKey = "${ui.categoryName.orEmpty()}-${ui.itemName.orEmpty()}"

            val matchPosition = (selectedPosition == "全部" || positionKey == selectedPosition)
            val matchItem = (selectedItem == "全部" || itemKey == selectedItem)

            matchPosition && matchItem
        }
        adapter.submitList(filtered)
    }
}
