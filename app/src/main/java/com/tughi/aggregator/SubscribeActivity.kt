package com.tughi.aggregator

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

class SubscribeActivity : AppActivity() {

    companion object {
        const val EXTRA_VIA_ACTION = "via_action"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, SubscribeSearchFragment())
                    .commit()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            if (!intent.getBooleanExtra(EXTRA_VIA_ACTION, false)) {
                setHomeAsUpIndicator(R.drawable.action_cancel)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home ->
                finish()
            else ->
                return super.onOptionsItemSelected(item)
        }
        return true
    }

}
