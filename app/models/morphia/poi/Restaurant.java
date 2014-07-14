package models.morphia.poi;

import com.fasterxml.jackson.databind.JsonNode;
import models.TravelPiBaseItem;
import models.morphia.geo.Address;
import models.morphia.misc.Contact;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.List;

/**
 * 酒店。
 *
 * @author Zephyre
 */
@Entity
public class Restaurant extends TravelPiBaseItem {
    @Id
    public ObjectId id;

    @Embedded
    public Ratings ratings;

    @Embedded
    public Contact contact;

    @Embedded
    public Address addr;

    public String name;

    public String url;

    public Double price;

    public String priceDesc;

    public String desc;

    public List<String> imageList;

    public List<String> tags;

    public List<String> alias;

    @Override
    public JsonNode toJson() {
        return null;
    }
}
