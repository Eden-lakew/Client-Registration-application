package com.example.mealconnect.ui.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Color; // Added for status color

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealconnect.data.local.ClientEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientListAdapter extends RecyclerView.Adapter<ClientListAdapter.ClientViewHolder> {

    public interface OnClientClickListener {
        void onClientClick(ClientEntity client);
    }

    private List<ClientEntity> clients = new ArrayList<>();
    private Set<String> checkedInClientIds = new HashSet<>(); // Stores IDs of clients who picked up today
    private final OnClientClickListener listener;

    public ClientListAdapter(OnClientClickListener listener) {
        this.listener = listener;
    }

    /**
     * Replaces the current list of clients.
     */
    public void submitList(List<ClientEntity> newList) {
        clients = newList;
        notifyDataSetChanged();
    }

    /**
     * Updates the set of clients who have checked in today.
     * Triggers a refresh to update the status text/color.
     */
    public void setCheckedInClients(Set<String> checkedInIds) {
        this.checkedInClientIds = checkedInIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ClientViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        ClientEntity client = clients.get(position);
        boolean isCheckedIn = checkedInClientIds.contains(client.clientId);
        holder.bind(client, isCheckedIn, listener);
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }

        void bind(ClientEntity client, boolean isCheckedIn, OnClientClickListener listener) {
            // Main Text: Name + Status Indicator
            String statusText = isCheckedIn ? " [PICKED UP]" : " [Not Picked Up]";
            text1.setText(client.name + statusText);

            // Visual Indicator: Green for Picked Up, Default/Red for Not
            if (isCheckedIn) {
                text1.setTextColor(Color.parseColor("#2E7D32")); // Green
            } else {
                text1.setTextColor(Color.BLACK); // Default
            }

            // Sub Text: Details
            text2.setText(String.format("ID: %s | Phone: %s",
                    client.clientId,
                    client.phone));

            itemView.setOnClickListener(v -> listener.onClientClick(client));
        }
    }
}
