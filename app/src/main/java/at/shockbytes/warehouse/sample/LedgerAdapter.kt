package at.shockbytes.warehouse.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import at.shockbytes.warehouse.R
import at.shockbytes.warehouse.ledger.Ledger
import at.shockbytes.warehouse.ledger.LedgerBlock

class LedgerAdapter(
    private val data: MutableList<LedgerBlock<Message>>
) : RecyclerView.Adapter<LedgerAdapter.LedgerViewHolder>() {

    fun add(block: LedgerBlock<Message>) {
        data.add(block)
        notifyItemInserted(data.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ledger_item, parent, false)
        return LedgerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LedgerViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    class LedgerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(data: LedgerBlock<Message>) {
            with(data) {
                itemView.findViewById<TextView>(R.id.tv_ledger_content).text = this.data.toString()
                itemView.findViewById<TextView>(R.id.tv_ledger_previousHash).text = this.previousHash
                itemView.findViewById<TextView>(R.id.tv_ledger_currentHash).text = this.hash.value
            }
        }
    }
}
