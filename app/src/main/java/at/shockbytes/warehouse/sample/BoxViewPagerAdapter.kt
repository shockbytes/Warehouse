package at.shockbytes.warehouse.sample

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import at.shockbytes.warehouse.box.Box

class BoxViewPagerAdapter(
    private val boxes: List<Box<*>>,
    private val context: Context,
    fragmentManager: FragmentManager
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return boxes.size
    }

    override fun getItem(position: Int): Fragment {
        TODO()
    }

    override fun getPageTitle(position: Int): CharSequence {
        return boxes[position].name
    }

}