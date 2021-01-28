package fr.acinq.tor

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.runner.AndroidJUnitRunner
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class TorAndroidTests : TestCase() {

    @Test
    public fun sslTest() {
        assertEquals('a', "action"[0])
    }

}
