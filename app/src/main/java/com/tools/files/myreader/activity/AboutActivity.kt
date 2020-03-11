package com.tools.files.myreader.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.tools.files.myreader.BuildConfig
import com.tools.files.myreader.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setSupportActionBar(toolbar_about)
        val mActionBar = supportActionBar!!
        mActionBar.setTitle(R.string.about)
        mActionBar.setDisplayHomeAsUpEnabled(true)

        initView()
    }

    private fun initView() {
        val version = BuildConfig.VERSION_NAME
        tv_version.setText(String.format(resources.getString(R.string.version),version))

        tv_email.setOnClickListener { v ->
            sendEmail()
        }
    }

    private fun sendEmail() {
        try {
            val shareIntent = Intent(
                Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", getString(R.string.email), null)
            )
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
            val send = "\n" +
                    "Message content..."
            shareIntent.putExtra(Intent.EXTRA_TEXT, send)
            startActivity(Intent.createChooser(shareIntent, "Choose one"))
        } catch (unused: Exception) {
            unused.printStackTrace()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }

}
