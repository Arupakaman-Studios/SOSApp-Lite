package app.sosapp.sos.sosapp.uiModules.home

import android.view.LayoutInflater
import android.view.ViewGroup
import app.sosapp.sos.sosapp.databinding.ItemHomeOptionsLayoutBinding
import app.sosapp.sos.sosapp.models.ModelEmergencyNumber
import app.sosapp.sos.sosapp.uiModules.base.BaseRecyclerViewAdapter
import app.sosapp.sos.sosapp.uiModules.base.BaseViewHolder
import app.sosapp.sos.sosapp.utils.invoke
import app.sosapp.sos.sosapp.utils.setSafeOnClickListener

class AdapterEmergencyNumbers(private val onClick: (ModelEmergencyNumber) -> Unit)
    : BaseRecyclerViewAdapter<AdapterEmergencyNumbers.MyViewHolder, ModelEmergencyNumber>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MyViewHolder(ItemHomeOptionsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class MyViewHolder(mDataBinding: ItemHomeOptionsLayoutBinding): BaseViewHolder<ItemHomeOptionsLayoutBinding>(mDataBinding){

        init {
            mDataBinding{
                itemRootView.setSafeOnClickListener {
                    getItem(adapterPosition)?.let(onClick)
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