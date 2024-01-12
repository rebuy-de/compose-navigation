package com.katajona.composenavigation

import com.katajona.composenavigation.navigation.NavigationType
import com.katajona.composenavigation.navigation.Router
import org.junit.Assert

class RouterMock : Router() {
    var collectedActions: MutableList<NavigationType> = mutableListOf()

    override fun dispatch(navTarget: NavigationType){
        collectedActions.add(navTarget)
    }

    fun removeFirst() {
        collectedActions.removeFirst()
    }

    inline fun <reified Type> assert(current: ((action: Type) -> Boolean)): RouterMock {
        Assert.assertTrue(this.collectedActions.first() is Type)

        val act = this.collectedActions.first() as Type
        Assert.assertTrue(current(act))

        removeFirst()

        return this
    }

    inline fun <reified Type> assertNot(): RouterMock {
        Assert.assertFalse(collectedActions.firstOrNull() is Type)

        return this
    }

    inline fun <reified Type> assert(): RouterMock {
        Assert.assertTrue(collectedActions.first() is Type)

        removeFirst()

        return this
    }
}
