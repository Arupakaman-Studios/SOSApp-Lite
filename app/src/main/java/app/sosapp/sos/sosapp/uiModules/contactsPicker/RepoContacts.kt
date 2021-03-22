package app.sosapp.sos.sosapp.uiModules.contactsPicker

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.sosapp.sos.sosapp.models.ModelContact
import app.sosapp.sos.sosapp.uiModules.base.BaseRepo
import app.sosapp.sos.sosapp.utils.FirebaseReporterUtil
import app.sosapp.sos.sosapp.utils.cancelIfActive
import app.sosapp.sos.sosapp.utils.executors.DefaultExecutorSupplier
import app.sosapp.sos.sosapp.utils.reportException
import java.util.concurrent.Future

class RepoContacts(mAppContext: Context): BaseRepo(mAppContext) {

    companion object{
        private val TAG by lazy { "RepoContacts" }
    }

    private var mJobContacts: Future<*>? = null

    private val contactsMLD = MutableLiveData<List<ModelContact>?>()
    val contactsLD: LiveData<List<ModelContact>?> = contactsMLD

    fun fetchContacts(isSOSContacts: Boolean) {
        mJobContacts.cancelIfActive()
        mJobContacts = DefaultExecutorSupplier.getInstance().forBackgroundTasks()
            .submit {
                kotlin.runCatching {
                    val sosContactsList: List<ModelContact> = mPrefs.sosContactsModels
                    Log.d(TAG, "sosContactsList -> $sosContactsList")
                    val sosContacts = sosContactsList.map { it.contactNumber }
                    Log.d(TAG, "sosContacts -> $sosContacts")

                    val resolver: ContentResolver = mAppContext.contentResolver
                    val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, null)

                    if (cursor != null && cursor.count > 0) {
                        val contacts = arrayListOf<ModelContact>()
                        val phones = arrayListOf<String>()
                        while (cursor.moveToNext()) {
                            val id =
                                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                            val name =
                                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                            val phoneNumber =
                                (cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))).toInt()

                            if (phoneNumber > 0) {
                                val cursorPhone = resolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                                    arrayOf(id),
                                    null
                                )

                                if (cursorPhone != null && cursorPhone.count > 0) {
                                    while (cursorPhone.moveToNext()) {
                                        val phoneNumValue = cursorPhone.getString(
                                            cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                        )
                                        if (!name.isNullOrBlank() && !phoneNumValue.isNullOrBlank() && !phones.contains(phoneNumValue)) {
                                            contacts.add(ModelContact(name, phoneNumValue, if (isSOSContacts) sosContacts.contains(phoneNumValue) else false))
                                            phones.add(phoneNumValue)
                                        }
                                        Log.e(TAG, "Name -> $name, Phn -> $phoneNumValue")
                                    }
                                }
                                cursorPhone?.close()
                            }
                        }
                        contactsMLD.postValue(contacts.sortedBy { it.contactName })
                    } else {
                        FirebaseReporterUtil.reportException("Empty Contacts Load Cursor")
                        contactsMLD.postValue(emptyList())
                    }
                    cursor?.close()
                }.onFailure {
                    it.reportException("Error loading contacts")
                    Log.e(TAG, "Load Contacts Exc : $it")
                    contactsMLD.postValue(null)
                }
            }
    }

    override fun onClear() {
        mJobContacts.cancelIfActive()
    }

}