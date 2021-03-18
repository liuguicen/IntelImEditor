//package a.baozouptu.home.tietuChoose.ui.main
//
//import a.baozouptu.ptu.tietu.onlineTietu.TietuListFragment
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.FragmentManager
//import androidx.fragment.app.FragmentPagerAdapter
//import androidx.viewpager2.adapter.FragmentStateAdapter
//
///**
// * A [FragmentPagerAdapter] that returns a fragment corresponding to
// * one of the sections/tabs/pages.
// */
//class SectionsPagerAdapter(private val fm: FragmentManager,
//                           private val firstClass: String,
//                           private val secondClass: String
//) : FragmentStateAdapter(fm) {
//    private var tagTitles = mutableListOf("本地", "热门", "我的", "分组")
//
//    override fun getItem(position: Int): Fragment {
//        return TietuListFragment.newInstance()
//    }
//
//    override fun getPageTitle(position: Int): CharSequence? {
//        return tagTitles[position]
//    }
//
//    override fun getCount(): Int {
//        return tagTitles.size
//    }
//}