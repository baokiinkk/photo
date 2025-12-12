package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.databinding.ItemObjBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class ObjAdapter(
    private val mContext: Context,
) : Adapter<ViewHolder>() {

    val listObjSelected = ArrayList<ObjAuto>()


    lateinit var eventClickObj: (ObjAuto) -> Unit

    private inner class ObjHolder(private val binding: ItemObjBinding) : ViewHolder(binding.root) {

        private var objAuto: ObjAuto? = null

        init {
            binding.root.setOnClickListener {
                objAuto?.let(eventClickObj)
            }
        }

        fun bind(objAuto: ObjAuto) {
            this.objAuto = objAuto

            Glide
                .with(mContext)
                .load(objAuto.bitmapMaskPreview)
                .transform(CenterCrop(), RoundedCorners(10))
                .into(binding.img)

            binding.tvName.text = objAuto.nameObj

            if (objAuto.isRemoved) {
                binding.img.isVisible = false
                binding.tvName.isVisible = false
//                binding.viewSelected.visibility = View.GONE
//                binding.viewState.visibility = View.VISIBLE
////                binding.viewState.setBackgroundResource(R.drawable.bg_btn_cancel_stroke_white)
//                binding.viewState.backgroundTintList = ContextCompat.getColorStateList(mContext, R.color.white)
            } else {
//                binding.img.backgroundTintList = ContextCompat.getColorStateList(mContext, R.color.white)
                if (listObjSelected.contains(objAuto)) {
                    binding.img.isVisible = true
                    binding.rootObjectDetect.setBackgroundResource(R.drawable.stroke_object_detect)
                    binding.tvName.setTextColor(
                        ContextCompat.getColor(
                            binding.tvName.context,
                            R.color.color_900
                        )
                    )
                    binding.tvName.typeface = ResourcesCompat.getFont( binding.tvName.context, R.font.quicksand_semibold)
//                    binding.stateSelected.isVisible=true

//                    binding.viewSelected.visibility = View.VISIBLE
//                    binding.viewState.visibility = View.VISIBLE
////                    binding.viewState.setBackgroundResource(R.drawable.bg_btn_cancel_stroke_white)
//                    binding.viewState.backgroundTintList =
//                        ContextCompat.getColorStateList(mContext, R.color.white)
                } else {
                    binding.img.isVisible = true
//                    binding.stateSelected.isVisible=false
                    binding.rootObjectDetect.setBackgroundResource(R.drawable.stroke_object_un_detect)
                    binding.tvName.setTextColor(
                        ContextCompat.getColor(
                            binding.tvName.context,
                            R.color.color_800
                        )
                    )
                    binding.tvName.typeface = ResourcesCompat.getFont( binding.tvName.context, R.font.quicksand_medium)
//                    binding.viewSelected.visibility = View.GONE
//                    binding.viewState.visibility = View.GONE
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemObjBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return ObjHolder(binding)
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder as? ObjHolder)?.bind(differ.currentList[position])
    }

    fun selectAll(onSelect: (List<ObjAuto>) -> Unit) {
        val listObjNotRemoved = differ.currentList.filter { obj -> !obj.isRemoved }
        if (listObjSelected.size != listObjNotRemoved.size) {
            listObjSelected.clear()
            listObjSelected.addAll(listObjNotRemoved)
        } else {
            listObjSelected.clear()
        }
        onSelect(listObjSelected)
        notifyItemRangeChanged(0, differ.currentList.size)
    }

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<ObjAuto>() {
        override fun areItemsTheSame(oldItem: ObjAuto, newItem: ObjAuto) = oldItem == newItem

        override fun areContentsTheSame(oldItem: ObjAuto, newItem: ObjAuto) = oldItem == newItem

    })
}