package com.example.mealconnect.ui.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.mealconnect.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealconnect.data.local.ClientEntity;

import java.util.ArrayList;
import java.util.List;

public class ClientListAdapter extends RecyclerView.Adapter<ClientListAdapter.ClientViewHolder> {

    public interface OnClientClickListener {
        void onClientClick(ClientEntity client);
    }

    private List<ClientEntity> clients = new ArrayList<>();
    private final OnClientClickListener listener;

    public ClientListAdapter(OnClientClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ClientEntity> newList) {
        clients = newList;
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
        holder.bind(client, listener);
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

        void bind(ClientEntity client, OnClientClickListener listener) {
            text1.setText(client.name);
            text2.setText("ID: " + client.clientId + " • " + client.dietaryRestrictions);
            itemView.setOnClickListener(v -> listener.onClientClick(client));
        }
    }
}
