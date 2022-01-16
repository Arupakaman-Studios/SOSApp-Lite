package app.sosapp.sos.sosapp.uiModules.moreApps

import android.view.LayoutInflater
import android.view.ViewGroup
import app.sosapp.sos.sosapp.databinding.ItemArupakamanAppLayoutBinding
import app.sosapp.sos.sosapp.models.ModelArupakamanApp
import app.sosapp.sos.sosapp.uiModules.base.BaseRecyclerViewAdapter
import app.sosapp.sos.sosapp.uiModules.base.BaseViewHolder
import app.sosapp.sos.sosapp.utils.invoke
import app.sosapp.sos.sosapp.utils.setSafeOnClickListener

class AdapterArupakamanApps(private val onClick: (ModelArupakamanApp) -> Unit)
    : BaseRecyclerViewAdapter<AdapterArupakamanApps.MyViewHolder, ModelArupakamanApp>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MyViewHolder(ItemArupakamanAppLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class MyViewHolder(mDataBinding: ItemArupakamanAppLayoutBinding): BaseViewHolder<ItemArupakamanAppLayoutBinding>(mDataBinding){

        init {
            mDataBinding{
                itemRootView.setSafeOnClickListener {
                    getItem(bindingAdapterPosition)?.let(onClick)
                }
            }
        }

        override fun bind(pos: Int) {
            mDataBinding{
                getItem(pos)?.let {model->
                    bModel = model
                }
            }
            super.bind(pos)
        }

    }

}