package at.shockbytes.warehouse.state.box

import kotlin.reflect.KProperty

class TransientBoxActivationDelegate(
    defaultValue: Boolean = true
) : BoxActivationDelegate {

    private var value: Boolean = defaultValue

    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = value

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        this.value = value
    }
}
