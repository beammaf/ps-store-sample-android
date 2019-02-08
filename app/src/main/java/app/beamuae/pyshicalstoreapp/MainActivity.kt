package app.beamuae.pyshicalstoreapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.widget.ProgressBar
import app.beamuae.physicalstore.PhysicalStoreSDK

import app.beamuae.physicalstore.shared.interfaces.*
import app.beamuae.physicalstore.shared.model.*
import kotlinx.android.synthetic.main.activity_main.*




class MainActivity : Activity(), PhysicalStoreSDKStartListener {
    private val KEY_PARTNER: String = "KEY_PARTNER"
    private val KEY_TOKEN: String = "KEY_TOKEN"

    override fun onSdkStarted() {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        obtainTokenInformation()

        PhysicalStoreSDK.instance.initialize(this, PhysicalStoreServer.STAGING, object : PhysicalStoreCredentialProvider {
            override fun getCredentials(credentialsListener: PhysicalStoreCredentialsListener) {
                credentialsListener.onCredentialReceived(
                    PhysicalStoreCredentials(partnerId = txPartner.text.toString(), token = txToken.text.toString())
                )
            }

        }, this)
        btnDetectStore.setOnClickListener {
            saveTokenToPrefs()
            detectStore()
        }

        btnDetectTerminal.setOnClickListener {
            saveTokenToPrefs()
            detectTerminal()
        }

        btnGetStoreTerminals.setOnClickListener {
            saveTokenToPrefs()
            showDetectedStoreTerminals()
        }



    }

    private fun saveTokenToPrefs() {
        writeToPrefs(KEY_TOKEN,txToken.text.toString())
        writeToPrefs(KEY_PARTNER,txPartner.text.toString())
    }


    private fun obtainTokenInformation() {
        txToken.setText(readFromPrefs(KEY_TOKEN))
        txPartner.setText(readFromPrefs(KEY_PARTNER))
    }

    private fun showDetectedStoreTerminals() {
        showProgress("DETECTING")
        PhysicalStoreSDK.instance.getStoreTerminals(object: PhysicalStoreCallBack<List<Terminal>>{
            override fun onSuccess(result: List<Terminal>) {
                dismissProgress()
                var message = ""
                result.forEach {
                    message += it.terminalDisplayName +" - "
                }
                message.trim()
                showSnackBar(message)
            }

            override fun onFailed(error: PhysicalStoreError) {
                dismissProgress()
                showSnackBar(error.getMessage()!!)
            }

        })
    }

    private fun detectTerminal() {
        showProgress("DETECTING")
        PhysicalStoreSDK.instance.detectTerminal(object : PhysicalStoreCallBack<Terminal> {
            override fun onSuccess(result: Terminal) {
                dismissProgress()
                showSnackBar("You Are paying at terminal ${result.terminalId}")
            }

            override fun onFailed(error: PhysicalStoreError) {
                dismissProgress()
                error.getCause()?.printStackTrace()
                showSnackBar(error.getMessage()!!)
            }


        })
    }

    private fun detectStore() {
        showProgress("DETECTING!!!")
        PhysicalStoreSDK.instance.detectStore(object : PhysicalStoreCallBack<Store> {
            override fun onSuccess(result: Store) {
                dismissProgress()
                showSnackBar("You Are At ${result.storeDisplayName}")
            }

            override fun onFailed(error: PhysicalStoreError) {
                dismissProgress()
                error.getCause()?.printStackTrace()
                showSnackBar(error.getMessage()!!)
            }


        })

    }


    private fun showSnackBar(message: String) {
        Snackbar.make(lytRoot, message, Snackbar.LENGTH_LONG).show()

    }

    private var progressDialog: AlertDialog? = null

    private fun showProgress(message: String) {
        progressDialog = AlertDialog.Builder(this).setView(ProgressBar(this))
            .setMessage(message)
            .setCancelable(false)
            .create()

        progressDialog!!.show()

    }

    private fun dismissProgress() {
        progressDialog?.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }



    fun sharedPrefs() : SharedPreferences = this@MainActivity.getPreferences(Context.MODE_PRIVATE)
    fun readFromPrefs(key: String) : String? = sharedPrefs().getString(key,null)
    fun writeToPrefs(key: String,v:String) = sharedPrefs().edit().putString(key,v).commit()

}


