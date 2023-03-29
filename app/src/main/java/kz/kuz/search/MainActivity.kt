package kz.kuz.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    private var fm = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            fm.beginTransaction()
                    .add(R.id.fragment_container, MainFragment())
                    .commitNow()
        }
    }

    // похоже, что данная функция не используется
    fun ChangeFragment(newFragment: Fragment) {
        fm.beginTransaction()
                .replace(R.id.fragment_container, newFragment)
                .commitNow()
    }
}