package com.example.mealconnect.data;

import android.util.Log;

import com.example.mealconnect.data.local.ClientDao;
import com.example.mealconnect.data.local.ClientEntity;
import com.example.mealconnect.data.model.Client;
import com.example.mealconnect.data.model.HouseholdMember;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ClientSyncRepository
 *
 * Bridges Firestore → Room (local DB)
 * Ensures volunteers can scan offline and sync automatically.
 */
public class ClientSyncRepository {

    private static final String TAG = "ClientSyncRepo";
    private static final String CLIENTS_COLLECTION = "clients";

    private static volatile ClientSyncRepository instance;
    private final FirebaseFirestore db;
    private final ClientDao clientDao;
    private final ExecutorService executor;

    private ClientSyncRepository(ClientDao clientDao) {
        this.db = FirebaseFirestore.getInstance();
        this.clientDao = clientDao;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static ClientSyncRepository getInstance(ClientDao clientDao) {
        if (instance == null) {
            synchronized (ClientSyncRepository.class) {
                if (instance == null) {
                    instance = new ClientSyncRepository(clientDao);
                }
            }
        }
        return instance;
    }

    /**
     * Starts Firestore → Room synchronization.
     */
    public void startSync() {
        db.collection(CLIENTS_COLLECTION)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Sync listener failed.", error);
                        return;
                    }

                    if (value != null) {
                        executor.execute(() -> {
                            for (QueryDocumentSnapshot doc : value) {
                                try {
                                    Client cloudClient = parseClientFromDocument(doc);

                                    // Lookup by barcode ID
                                    ClientEntity localClient =
                                            clientDao.getClientByBarcodeId(cloudClient.getClientId());

                                    if (localClient == null) {
                                        // Insert new
                                        localClient = mapToEntity(cloudClient);
                                        clientDao.insertClient(localClient);
                                        Log.d(TAG, "Inserted local client: " + cloudClient.getClientId());
                                    } else {
                                        // Update existing
                                        updateEntityFromModel(localClient, cloudClient);
                                        clientDao.updateClient(localClient);
                                        Log.d(TAG, "Updated local client: " + cloudClient.getClientId());
                                    }

                                } catch (Exception e) {
                                    Log.e(TAG, "Error syncing document: " + doc.getId(), e);
                                }
                            }
                        });
                    }
                });
    }

    /**
     * SAFE Firestore → Client model parser.
     * Fixes:
     *  - dietaryRestrictions (String OR List)
     *  - null safety for numbers
     *  - missing optional fields
     */
    private Client parseClientFromDocument(QueryDocumentSnapshot doc) {

        Client client = new Client();

        // Basic fields
        client.setClientId(doc.getString("clientId"));
        client.setName(doc.getString("name"));
        client.setPhone(doc.getString("phone"));
        client.setAddress(doc.getString("address"));
        client.setFirstLanguage(doc.getString("firstLanguage"));
        client.setAuthorizedPickup(doc.getString("authorizedPickup"));

        // Safe number parsing
        Long adults = doc.getLong("numAdults");
        client.setNumAdults(adults != null ? adults.intValue() : 0);

        Long children = doc.getLong("numChildren");
        client.setNumChildren(children != null ? children.intValue() : 0);

        client.setLowIncome(Boolean.TRUE.equals(doc.getBoolean("isLowIncome")));
        client.setNewcomer(Boolean.TRUE.equals(doc.getBoolean("isNewcomer")));
        client.setSenior(Boolean.TRUE.equals(doc.getBoolean("isSenior")));
        client.setSiteCode(doc.getString("siteCode"));

        Long timestamp = doc.getLong("registrationTimestamp");
        client.setRegistrationTimestamp(timestamp != null ? timestamp : 0L);

        // ------------------------------
        // dietaryRestrictions FIX
        // ------------------------------
        Object restrictionsObj = doc.get("dietaryRestrictions");

        if (restrictionsObj instanceof List) {
            // Already a list
            @SuppressWarnings("unchecked")
            List<String> restrictions = (List<String>) restrictionsObj;
            client.setDietaryRestrictions(restrictions);

        } else if (restrictionsObj instanceof String) {
            // Convert single string → list
            String str = (String) restrictionsObj;
            List<String> list = new ArrayList<>();
            if (!str.trim().isEmpty()) {
                for (String part : str.split(",")) {
                    if (!part.trim().isEmpty()) {
                        list.add(part.trim());
                    }
                }
            }
            client.setDietaryRestrictions(list);

        } else {
            client.setDietaryRestrictions(new ArrayList<>());
        }

        // ------------------------------
        // householdMembers FIX
        // ------------------------------
        Object membersObj = doc.get("householdMembers");

        if (membersObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> rawList = (List<Object>) membersObj;

            List<HouseholdMember> members = new ArrayList<>();

            for (Object item : rawList) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) item;

                    HouseholdMember member = new HouseholdMember();
                    member.setType((String) map.get("type"));
                    member.setFirstName((String) map.get("firstName"));
                    member.setLastName((String) map.get("lastName"));
                    member.setDateOfBirth((String) map.get("dateOfBirth"));
                    member.setAllergyNotes((String) map.get("allergyNotes"));
                    member.setContactNumber((String) map.get("contactNumber"));

                    members.add(member);
                }
            }

            client.setHouseholdMembers(members);
        } else {
            client.setHouseholdMembers(new ArrayList<>());
        }

        return client;
    }

    /**
     * Maps Firestore model → Room entity.
     */
    private ClientEntity mapToEntity(Client cloudClient) {
        ClientEntity entity = new ClientEntity();

        entity.clientId = cloudClient.getClientId();
        entity.name = cloudClient.getName();
        entity.phone = cloudClient.getPhone();
        entity.address = cloudClient.getAddress();
        entity.firstLanguage = cloudClient.getFirstLanguage();
        entity.authorizedPickup = cloudClient.getAuthorizedPickup();
        entity.numAdults = cloudClient.getNumAdults();
        entity.numChildren = cloudClient.getNumChildren();
        entity.isLowIncome = cloudClient.isLowIncome();
        entity.isNewcomer = cloudClient.isNewcomer();
        entity.isSenior = cloudClient.isSenior();

        // Convert list → comma string
        if (cloudClient.getDietaryRestrictions() != null) {
            entity.dietaryRestrictions =
                    String.join(",", cloudClient.getDietaryRestrictions());
        } else {
            entity.dietaryRestrictions = "";
        }

        return entity;
    }

    private void updateEntityFromModel(ClientEntity entity, Client cloudClient) {
        entity.name = cloudClient.getName();
        entity.phone = cloudClient.getPhone();
        entity.address = cloudClient.getAddress();
        entity.firstLanguage = cloudClient.getFirstLanguage();
        entity.authorizedPickup = cloudClient.getAuthorizedPickup();
        entity.numAdults = cloudClient.getNumAdults();
        entity.numChildren = cloudClient.getNumChildren();
        entity.isLowIncome = cloudClient.isLowIncome();
        entity.isNewcomer = cloudClient.isNewcomer();
        entity.isSenior = cloudClient.isSenior();

        if (cloudClient.getDietaryRestrictions() != null) {
            entity.dietaryRestrictions =
                    String.join(",", cloudClient.getDietaryRestrictions());
        } else {
            entity.dietaryRestrictions = "";
        }
    }
}
