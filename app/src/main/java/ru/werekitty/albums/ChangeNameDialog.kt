package ru.werekitty.albums

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_changename.view.*

class ChangeNameDialog(id: Int) : DialogFragment() {

    private lateinit var mListener: Listener
    val mID = id

    interface Listener {
        fun DialogHelper(text: String, id: Int)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mListener = context as Listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_changename, null)
        return AlertDialog.Builder(context!!)
            .setView(view!!)
            .setPositiveButton(android.R.string.ok, {_,_ ->
                val text = removeSpaces(view.change_input.text.toString()
                    .replace("\n", "")
                    .replace("exec", ""))
                if (!text.isEmpty()){
                    mListener.DialogHelper(text, mID)
                } else {
                    Toast.makeText(context, "некорректное название", Toast.LENGTH_SHORT).show()
                }

            })
            .setNegativeButton(android.R.string.cancel, {_,_ ->

            })
            .setTitle("Введите новое название")
            .create()
    }
}