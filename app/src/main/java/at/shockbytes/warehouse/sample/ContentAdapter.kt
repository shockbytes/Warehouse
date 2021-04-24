package at.shockbytes.warehouse.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import at.shockbytes.warehouse.R

class ContentAdapter(
    private val data: MutableList<Message>
) : RecyclerView.Adapter<ContentAdapter.LedgerViewHolder>() {

    fun add(block: Message) {
        data.add(block)
        notifyItemInserted(data.size - 1)
    }

    fun lastItem(): Message {
        return data.last()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.content_item, parent, false)
        return LedgerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LedgerViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun setData(messages: List<Message>) {
        data.clear()
        data.addAll(messages)

        notifyDataSetChanged()
    }

    class LedgerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(data: Message) {
            with(data) {
                itemView.findViewById<TextView>(R.id.tv_message_id).text = this.id
                itemView.findViewById<TextView>(R.id.tv_message_recipient).text = this.recipient
                itemView.findViewById<TextView>(R.id.tv_message_message).text = this.content
            }
        }
    }
}
