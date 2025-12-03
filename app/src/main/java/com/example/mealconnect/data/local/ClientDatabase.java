package com.example.mealconnect.data.local;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mealconnect.data.model.CheckInEvent;
@Database(entities = {ClientEntity.class, AdminEntity.class, VolunteerEntity.class, CheckInEvent.class}, version = 2, exportSchema = false)
public abstract class ClientDatabase extends RoomDatabase {

    private static volatile ClientDatabase INSTANCE;

    public abstract ClientDao clientDao();
    public abstract AdminDao adminDao();
    public abstract VolunteerDao volunteerDao();
    public static ClientDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ClientDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ClientDatabase.class,
                            "client_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
