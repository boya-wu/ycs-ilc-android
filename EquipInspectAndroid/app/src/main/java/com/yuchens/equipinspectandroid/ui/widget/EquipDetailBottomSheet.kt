package com.yuchens.equipinspectandroid.ui.widget

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yuchens.equipinspectandroid.R
import com.yuchens.equipinspectandroid.ui.model.EquipUi

class EquipDetailBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener { di ->
            val d = di as BottomSheetDialog
            val bottomSheet =
                d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                // 如需全高貼合內容，可再視需要設定：
                // behavior.isFitToContents = true   // 預設即為 true
                // behavior.peekHeight = 0           // 讓展開高度受內容支配
            }
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_equip_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = requireArguments()

        view.findViewById<android.widget.TextView>(R.id.txtBarcode).text = args.getString(KEY_BARCODE, "")
        view.findViewById<android.widget.TextView>(R.id.txtItemName).text = getString(
            R.string.desc_category_item_format,
            args.getString(KEY_CATEGORY, ""),
            args.getString(KEY_ITEM, "")
        )
        view.findViewById<android.widget.TextView>(R.id.txtAreaName).text = getString(
            R.string.desc_building_area_format,
            args.getString(KEY_BUILDING, ""),
            args.getString(KEY_AREA, "")
        )

        // ★ 異常區塊：無資料就整塊隱藏
        val abnormal = (args.getString(KEY_ABNORMAL) ?: "").trim()
        val sectionAbnormal = view.findViewById<View>(R.id.sectionAbnormal)
        val txtAbnormal = view.findViewById<android.widget.TextView>(R.id.txtAbnormalName)
        if (abnormal.isEmpty()) {
            sectionAbnormal.visibility = View.GONE
        } else {
            sectionAbnormal.visibility = View.VISIBLE
            txtAbnormal.text = abnormal
        }

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnClose).setOnClickListener { dismiss() }
    }


    companion object {
        private const val KEY_BARCODE = "barcode"
        private const val KEY_CATEGORY = "category"
        private const val KEY_ITEM = "item"
        private const val KEY_BUILDING = "building"
        private const val KEY_AREA = "area"
        private const val KEY_ABNORMAL = "abnormal"

        fun newInstance(e: EquipUi) = EquipDetailBottomSheet().apply {
            arguments = Bundle().apply {
                putString(KEY_BARCODE, e.barcode)
                putString(KEY_CATEGORY, e.categoryName)
                putString(KEY_ITEM, e.itemName)
                putString(KEY_BUILDING, e.buildingName)
                putString(KEY_AREA, e.areaName)
                putString(KEY_ABNORMAL, e.abnormal ?: "")
            }
        }
    }
}
