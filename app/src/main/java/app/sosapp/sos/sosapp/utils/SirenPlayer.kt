package app.sosapp.sos.sosapp.utils

import android.content.Context
import android.media.MediaPlayer
import androidx.core.os.bundleOf
import app.sosapp.sos.sosapp.R


class SirenPlayer(private val mContext: Context) {

    private var mMediaPlayer: MediaPlayer? = null

    fun stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer?.release()
            mMediaPlayer = null
        }
    }

   /* fun play() {
        stop()
        mMediaPlayer = MediaPlayer.create(mContext, R.raw.police_siren)
        mMediaPlayer.setOnCompletionListener(OnCompletionListener { stop() })
        mMediaPlayer.start()
    }*/

    fun playRepeatably() {
        stop()
        mMediaPlayer = MediaPlayer.create(mContext, R.raw.police_siren)
        mMediaPlayer?.apply {
            setOnCompletionListener {
                playRepeatably()
            }
            start()
            mContext.setFirebaseAnalyticsLogEvent("SOS_Siren_Played", bundleOf("Siren" to "Played"))
        }
    }

}