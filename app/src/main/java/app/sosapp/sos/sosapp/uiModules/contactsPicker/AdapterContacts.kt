package app.sosapp.sos.sosapp.uiModules.contactsPicker

import android.view.LayoutInflater
import android.view.ViewGroup
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.databinding.ItemContactLayoutBinding
import app.sosapp.sos.sosapp.models.ModelContact
import app.sosapp.sos.sosapp.uiModules.base.BaseRecyclerViewAdapter
import app.sosapp.sos.sosapp.uiModules.base.BaseViewHolder
import app.sosapp.sos.sosapp.utils.SOSAppRes
import app.sosapp.sos.sosapp.utils.invoke
import app.sosapp.sos.sosapp.utils.setSafeOnClickListener
import app.sosapp.sos.sosapp.utils.toast
import com.google.firebase.crashlytics.internal.common.AppData
import com.turingtechnologies.materialscrollbar.INameableAdapter


class AdapterContacts(
    private val maxSelect: Int = Int.MAX_VALUE,
    private val isPickContacts: Boolean = true,
    private val onClick: (ModelContact, Int) -> Unit,
    private val onDeleteClick: (ModelContact) -> Unit = {}
)
    : BaseRecyclerViewAdapter<AdapterContacts.MyViewHolder, ModelContact>(), INameableAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MyViewHolder(
                ItemContactLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(position)
    }

    fun getSelectedContacts() =  mItemsList.filter { it.isSOSContact }

    override fun getCharacterForElement(element: Int): Char {
        getItem(element)?.let {model->
            var c: Char = model.contactName.first()
            if (Character.isDigit(c)) {
                c = '#'
            }
            return c
        }
        return ' '
    }

    inner class MyViewHolder(mDataBinding: ItemContactLayoutBinding): BaseViewHolder<ItemContactLayoutBinding>(
        mDataBinding
    ){

        init {
            mDataBinding{

                ivDeleteBtn.setSafeOnClickListener {
                    getItem(adapterPosition)?.let(onDeleteClick)
                }

                itemRootView.setSafeOnClickListener {
                    kotlin.runCatching {
                        getItem(adapterPosition)?.apply {
                            if (!isPickContacts || (getSelectedContacts().size < maxSelect || isSOSContact)){
                                isSOSContact = !isSOSContact
                                onClick(this, adapterPosition)
                                notifyItemChanged(adapterPosition)
                            }else{
                                itemRootView.context?.applicationContext?.toast(
                                    SOSAppRes.getString(
                                        R.string.msg_contact_select_limit
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        override fun bind(pos: Int) {
            mDataBinding{
                getItem(pos)?.let { model->
                    bModel = model

                    bIsPick = isPickContacts
                }
            }
            super.bind(pos)
        }

    }

}