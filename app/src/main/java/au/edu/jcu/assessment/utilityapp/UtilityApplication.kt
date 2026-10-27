package au.edu.jcu.assessment.utilityapp

import android.app.Application
import au.edu.jcu.assessment.utilityapp.data.AppContainer
import au.edu.jcu.assessment.utilityapp.data.DefaultAppContainer

/** Creates the dependency container once for the whole app. */
class UtilityApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
