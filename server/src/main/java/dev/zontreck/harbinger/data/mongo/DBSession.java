package dev.zontreck.harbinger.data.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.zontreck.harbinger.data.types.GenericClass;
import dev.zontreck.harbinger.thirdparty.libomv.types.UUID;

public class DBSession
{
    private UUID ID;
    private MongoClient client;
    public MongoDatabase database;


    public UUID getID()
    {
        return ID;
    }


    public DBSession(MongoClient client)
    {
        this.client=client;
        ID = UUID.GenerateUUID();
    }

    /**
     * Closes out the session, warning: should only be invoked from MongoDriver#closeSession
     */
    protected void close()
    {
        ID = UUID.Zero;

        database=null;
        client.close();
        client=null;
    }

    public void openDB(String DBName)
    {
        database = client.getDatabase(DBName);
    }


    public <T> MongoCollection<T> getTableFor(String ID, GenericClass<T> clazz)
    {
        MongoDatabase db = database;
        if(db!=null)
        {
            return db.getCollection(ID, clazz.getMyType());
        }else return null;
    }
}
