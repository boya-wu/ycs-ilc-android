package com.yuchens.equipinspectandroid.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yuchens.equipinspectandroid.R
import com.yuchens.equipinspectandroid.databinding.FragmentInspectBinding
import com.yuchens.equipinspectandroid.ui.base.BaseFragment
import com.yuchens.equipinspectandroid.ui.model.Option
import com.yuchens.equipinspectandroid.ui.viewmodel.InspectViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.Base64
import java.util.UUID

@AndroidEntryPoint
class InspectFragment : BaseFragment() {

    private val vm: InspectViewModel by viewModels()

    private var _binding: FragmentInspectBinding? = null
    private val binding get() = _binding!!

    private var lastScanTime = 0L
    private var lastScannedBarcode = ""

    // 舊條碼（可選日期後綴）
    private val barcodeRegex =
        Regex("""^([A-Za-z0-9\-]+)(?:,(?:\d{4}/\d{1,2}/\d{1,2})?)?$""")
    // Base64Url GUID：22（無=）或 24（含==）
    private val base64UrlGuidRegex22 = Regex("^[A-Za-z0-9_-]{22}$")
    private val base64UrlGuidRegex24 = Regex("^[A-Za-z0-9_-]{24}$")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInspectBinding.inflate(inflater, container, false)

        // 先隱藏「異常」相關元件
        binding.spinnerAbnormal.visibility = View.GONE
        binding.edtAbnormalNote.visibility = View.GONE

        // 掃碼（Enter）事件
        binding.edtBarcode.setOnEditorActionListener { _, _, _ ->
            val raw = binding.edtBarcode.text.toString()
            val key = parseScan(raw)

            if (key.isBlank()) {
                binding.edtBarcode.clearAndRefocus()
                return@setOnEditorActionListener true
            }
            val now = System.currentTimeMillis()
            if (key == lastScannedBarcode && now - lastScanTime < 1_000) {
                binding.edtBarcode.clearAndRefocus()
                return@setOnEditorActionListener true
            }
            lastScannedBarcode = key
            lastScanTime = now

            vm.tryLoadWithRateLimit(key)
            binding.edtBarcode.clearAndRefocus()
            true
        }

        // 確認
        binding.btnConfirm.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val abnormalNote =
                    binding.edtAbnormalNote.text?.toString()?.trim().orEmpty().ifBlank { null }
                val ok = vm.confirm(abnormalNote)
                Toast.makeText(
                    requireContext(),
                    if (ok) "更新成功" else "更新失敗",
                    Toast.LENGTH_SHORT
                ).show()
                if (ok) {
                    vm.clear()
                    clearFields()
                }
                binding.edtBarcode.clearAndRefocus()
            }
        }

        // 清除
        binding.btnClear.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val ok = vm.clearAbnormal()
                Toast.makeText(
                    requireContext(),
                    if (ok) "清除成功" else "清除失敗",
                    Toast.LENGTH_SHORT
                ).show()
                if (ok) {
                    // 清空備註輸入框
                    binding.edtAbnormalNote.setText("")
                    binding.edtAbnormalNote.setSelection(0)
                }

                binding.edtBarcode.clearAndRefocus()
            }
        }

        // 預設把焦點放在輸入框
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                binding.edtBarcode.clearAndRefocus()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 候選設備清單 → spinnerEquip（使用 Option<guid,label>）
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.candidates.collect { list ->
                    if (list.isEmpty()) {
                        binding.spinnerEquip.adapter = null
                        binding.spinnerEquip.visibility = View.GONE
                    } else {
                        val options: List<Option> = list.mapNotNull { ui ->
                            val guid = ui.guid?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                            val loc = "${ui.buildingName.orEmpty()} - ${ui.areaName.orEmpty()}".trim('-',' ')
                            val item = "${ui.categoryName.orEmpty()} - ${ui.itemName.orEmpty()}".trim('-',' ')
                            val b = ui.barcode.orEmpty()
                            val label = listOf(loc, item, b)
                                .filter { it.isNotBlank() }
                                .joinToString(" ｜ ")
                            Option(value = guid, text = label)
                        }

                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            options
                        ).also {
                            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        binding.spinnerEquip.adapter = adapter
                        binding.spinnerEquip.visibility = View.VISIBLE

                        // 以 Option 通知 VM（用 guid 切換）
                        binding.spinnerEquip.onItemSelectedListener =
                            object : android.widget.AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: android.widget.AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    val opt = parent?.getItemAtPosition(position) as? Option
                                    val guid = opt?.value
                                    if (!guid.isNullOrBlank()) {
                                        vm.selectCandidateByGuid(guid)
                                    }
                                }
                                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                            }

                        // 預設選取（用 guid 對應回去）
                        val currentGuid = vm.equip.value?.guid
                        val idx = options.indexOfFirst { it.value.equals(currentGuid, ignoreCase = true) }
                            .let { if (it >= 0) it else 0 }
                        if (binding.spinnerEquip.selectedItemPosition != idx) {
                            binding.spinnerEquip.setSelection(idx)
                        }
                    }
                }
            }
        }

        // 2) Equip 明細顯示
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.equip.collect { equip ->
                        if (equip == null) {
                            clearFields()
                            binding.spinnerAbnormal.visibility = View.GONE
                        } else {
                            binding.txtBarcode.text = equip.barcode
                            binding.txtItemName.text = getString(
                                R.string.desc_category_item_format,
                                equip.categoryName,
                                equip.itemName
                            )
                            binding.txtAreaName.text = getString(
                                R.string.desc_building_area_format,
                                equip.buildingName,
                                equip.areaName
                            )
                            binding.txtReplaceDate.text = equip.replaceMedicineDate ?: ""

                            // 顯示並帶入既有異常內容
                            binding.edtAbnormalNote.visibility = View.VISIBLE  // ← 新增
                            binding.edtAbnormalNote.setText(equip.abnormal ?: "")
                            binding.edtAbnormalNote.setSelection(binding.edtAbnormalNote.text?.length ?: 0)
                        }
                    }
                }

                // 3) 異常清單（Option），同時設定選取時把文字追加到 TextArea
                launch {
                    vm.options.collect { opts ->
                        if (opts.isEmpty()) {
                            binding.spinnerAbnormal.adapter = null
                            binding.spinnerAbnormal.visibility = View.GONE
                        } else {
                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                opts
                            ).also {
                                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }
                            binding.spinnerAbnormal.adapter = adapter
                            binding.spinnerAbnormal.visibility = View.VISIBLE

                            // ★ 避免初始化與程式化重設時觸發處理邏輯
                            var skipFirst = true
                            var programmaticReset = false

                            binding.spinnerAbnormal.onItemSelectedListener =
                                object : android.widget.AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(
                                        parent: android.widget.AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long
                                    ) {
                                        if (skipFirst) {
                                            skipFirst = false
                                            return
                                        }
                                        if (programmaticReset) {
                                            programmaticReset = false
                                            return
                                        }

                                        val opt = parent?.getItemAtPosition(position) as? Option ?: return
                                        val txt = opt.text.trim()
                                        val valId = opt.value.trim()

                                        // 選到「空值/正常」就不追加
                                        if (txt.isEmpty() || valId.isEmpty()) return

                                        val area = binding.edtAbnormalNote
                                        val current = area.text?.toString().orEmpty()

                                        // 以「、」為分隔避免重複
                                        val tokens = current.split('、')
                                            .map { it.trim() }
                                            .filter { it.isNotEmpty() }
                                            .toMutableList()

                                        if (tokens.none { it.equals(txt, ignoreCase = true) }) {
                                            tokens.add(txt)
                                            area.setText(tokens.joinToString(separator = "、"))
                                            area.setSelection(area.text?.length ?: 0)
                                        }

                                        // ★ 追加完就重設回「空值」
                                        programmaticReset = true
                                        binding.spinnerAbnormal.setSelection(0)
                                    }
                                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                                }

                            // ★ 初始顯示就放在「空值」
                            binding.spinnerAbnormal.setSelection(0)
                        }
                    }
                }
            }
        }

        // 4) 其他訊息
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.message.collect { msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearFields() {
        binding.txtBarcode.text = ""
        binding.txtItemName.text = ""
        binding.txtAreaName.text = ""
        binding.txtReplaceDate.text = ""
        binding.spinnerAbnormal.adapter = null
        binding.spinnerAbnormal.visibility = View.GONE
        binding.edtAbnormalNote.setText("") // 清空備註
    }

    /** 將掃描字串正規化為「唯一字串鍵」：
     *  - Base64Url(.NET Guid) -> 36字元 UUID
     *  - 舊條碼 -> 條碼本體
     *  - 其他 -> trim 後原樣
     */
    private fun parseScan(raw: String): String {
        val trimmed = raw.trim().trimEnd('\r', '\n')

        // 先試 GUID (Base64Url)
        if (base64UrlGuidRegex22.matches(trimmed) || base64UrlGuidRegex24.matches(trimmed)) {
            decodeDotNetGuidFromBase64Url(trimmed)?.let { uuid ->
                return uuid.toString() // 如需一致可 .lowercase()
            }
        }

        // 舊條碼：取 group(1)
        barcodeRegex.matchEntire(trimmed)?.let { m ->
            return m.groupValues[1]
        }

        // 都不符就回傳原值
        return trimmed
    }

    /** .NET Guid.ToByteArray() → Base64Url 的 16 bytes 轉回 Java UUID（修正小端序） */
    private fun decodeDotNetGuidFromBase64Url(s: String): UUID? {
        return try {
            var b64 = s.replace('-', '+').replace('_', '/')
            if (b64.length % 4 != 0) b64 += "=".repeat(4 - (b64.length % 4))
            val bytes = Base64.getDecoder().decode(b64)
            if (bytes.size != 16) return null

            // .NET: [3,2,1,0, 5,4, 7,6, 8..15] → RFC-4122
            val b = bytes
            val reordered = byteArrayOf(
                b[3], b[2], b[1], b[0],
                b[5], b[4],
                b[7], b[6],
                b[8], b[9], b[10], b[11], b[12], b[13], b[14], b[15]
            )

            val bb = ByteBuffer.wrap(reordered)
            val msb = bb.long
            val lsb = bb.long
            UUID(msb, lsb)
        } catch (_: Exception) {
            null
        }
    }
}
