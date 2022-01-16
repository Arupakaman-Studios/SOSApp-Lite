package app.sosapp.sos.sosapp.uiModules.home

import android.view.LayoutInflater
import android.view.ViewGroup
import app.sosapp.sos.sosapp.databinding.ItemHomeOptionsLayoutBinding
import app.sosapp.sos.sosapp.databinding.ItemSosToolsLayoutBinding
import app.sosapp.sos.sosapp.models.ModelEmergencyNumber
import app.sosapp.sos.sosapp.models.ModelSOSTool
import app.sosapp.sos.sosapp.uiModules.base.BaseRecyclerViewAdapter
import app.sosapp.sos.sosapp.uiModules.base.BaseViewHolder
import app.sosapp.sos.sosapp.utils.invoke
import app.sosapp.sos.sosapp.utils.setSafeOnClickListener

class AdapterSOSTools(private val onClick: (Int) -> Unit)
    : BaseRecyclerViewAdapter<AdapterSOSTools.MyViewHolder, ModelSOSTool>() {

    init {
        mItemsList = ModelSOSTool.getSOSTools()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(ItemSosToolsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class MyViewHolder(mDataBinding: ItemSosToolsLayoutBinding): BaseViewHolder<ItemSosToolsLayoutBinding>(mDataBinding){

        init {
            mDataBinding{
                itemRootView.setSafeOnClickListener {
                    getItem(bindingAdapterPosition)?.let{
                        onClick(bindingAdapterPosition)
                    }
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