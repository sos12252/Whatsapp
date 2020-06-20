package com.example.myapplication

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TabsAccessorAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
    override fun getItem(i: Int): Fragment {
        return when (i) {
            0 -> {
                ChatsFragment()
            }
            1 -> {
                GroupsFragment()
            }
            2 -> {
                ContactsFragment()
            }
            3 -> {
                RequestsFragment()
            }
            else -> null
        }
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Chats"
            1 -> "Groups"
            2 -> "Contacts"
            3 -> "Requests"
            else -> null
        }
    }
}