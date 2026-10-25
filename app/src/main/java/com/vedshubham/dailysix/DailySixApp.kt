package com.vedshubham.dailysix

import android.app.Application
import com.vedshubham.dailysix.billing.Billing

class DailySixApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Billing.init(this)
    }
}
