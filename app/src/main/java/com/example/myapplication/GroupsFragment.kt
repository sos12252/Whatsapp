package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.codingcafe.whatsapp.R
import com.google.firebase.database.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class GroupsFragment : Fragment() {
    private var groupFragmentView: View? = null
    private var list_view: ListView? = null
    private var arrayAdapter: ArrayAdapter<String?>? = null
    private val list_of_groups = ArrayList<String?>()
    private var GroupRef: DatabaseReference? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false)
        GroupRef = FirebaseDatabase.getInstance().reference.child("Groups")
        IntializeFields()
        RetrieveAndDisplayGroups()
        list_view!!.onItemClickListener = OnItemClickListener { adapterView, view, position, id ->
            val currentGroupName = adapterView.getItemAtPosition(position).toString()
            val groupChatIntent = Intent(context, GroupChatActivity::class.java)
            groupChatIntent.putExtra("groupName", currentGroupName)
            startActivity(groupChatIntent)
        }
        return groupFragmentView
    }

    private fun IntializeFields() {
        list_view = groupFragmentView!!.findViewById<View>(R.id.list_view) as ListView
        arrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, list_of_groups)
        list_view!!.adapter = arrayAdapter
    }

    private fun RetrieveAndDisplayGroups() {
        GroupRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val set: MutableSet<String?> = HashSet()
                val iterator: Iterator<*> = dataSnapshot.children.iterator()
                while (iterator.hasNext()) {
                    set.add((iterator.next() as DataSnapshot).key)
                }
                list_of_groups.clear()
                list_of_groups.addAll(set)
                arrayAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}