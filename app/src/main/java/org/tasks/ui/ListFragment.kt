package org.tasks.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import butterknife.BindView
import com.google.android.material.chip.ChipGroup
import com.todoroo.astrid.api.CaldavFilter
import com.todoroo.astrid.api.Filter
import com.todoroo.astrid.api.GtasksFilter
import com.todoroo.astrid.service.TaskMover
import dagger.hilt.android.AndroidEntryPoint
import org.tasks.R
import org.tasks.activities.ListPicker
import javax.inject.Inject

@AndroidEntryPoint
class ListFragment : TaskEditControlFragment() {
    @BindView(R.id.chip_group)
    lateinit var chipGroup: ChipGroup

    @Inject lateinit var chipProvider: ChipProvider
    
    private lateinit var callback: OnListChanged

    interface OnListChanged {
        fun onListChanged(filter: Filter?)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        callback = activity as OnListChanged
    }

    override fun createView(savedInstanceState: Bundle?) {
        refreshView()
    }

    private fun setSelected(filter: Filter) {
        viewModel.selectedList = filter
        refreshView()
        callback.onListChanged(filter)
    }

    override val layout = R.layout.control_set_remote_list

    override val icon = R.drawable.ic_list_24px

    override fun controlId() = TAG

    override fun onRowClick() = openPicker()

    override val isClickable = true

    private fun openPicker() =
            ListPicker.newListPicker(viewModel.selectedList!!, this, REQUEST_CODE_SELECT_LIST)
                    .show(parentFragmentManager, FRAG_TAG_GOOGLE_TASK_LIST_SELECTION)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SELECT_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                data?.getParcelableExtra<Filter>(ListPicker.EXTRA_SELECTED_FILTER)?.let {
                    if (it is GtasksFilter || it is CaldavFilter) {
                        setSelected(it)
                    } else {
                        throw RuntimeException("Unhandled filter type")
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun refreshView() {
        chipGroup.removeAllViews()
        val chip = chipProvider.newChip(
                viewModel.selectedList!!,
                R.drawable.ic_list_24px,
                showText = true,
                showIcon = true)!!
        chip.setOnClickListener { openPicker() }
        chipGroup.addView(chip)
    }

    companion object {
        const val TAG = R.string.TEA_ctrl_google_task_list
        private const val FRAG_TAG_GOOGLE_TASK_LIST_SELECTION = "frag_tag_google_task_list_selection"
        private const val REQUEST_CODE_SELECT_LIST = 10101
    }
}