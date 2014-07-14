package scripts;

import com.mongodb.*;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.geo.Address;
import models.morphia.geo.Coords;
import models.morphia.misc.Contact;
import models.morphia.misc.SimpleRef;
import models.morphia.poi.Ratings;
import models.morphia.poi.Restaurant;
import models.morphia.poi.ViewSpot;
import models.morphia.traffic.AirPrice;
import models.morphia.traffic.AirRoute;
import models.morphia.traffic.Airline;
import models.morphia.traffic.Airport;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import utils.Utils;

import java.lang.reflect.Field;
import java.util.*;

public class Main {

    public static void importVs() throws TravelPiException, NoSuchFieldException, IllegalAccessException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        DBCollection col = Utils.getMongoClient("localhost", 28017).getDB("poi").getCollection("view_spot");
//        DBCursor cursor = col.find(QueryBuilder.start("_id").greaterThanEquals(new ObjectId("53b0545e10114e051426e4fc")).get());
        DBCursor cursor = col.find(QueryBuilder.start().get());
        System.out.println(String.format("TOTAL RECORDS: %d", cursor.count()));

        int i = 0;
        while (cursor.hasNext()) {
            i++;
            Object tmp;
            DBObject poiDB = cursor.next();

//            Hotel poi = new Hotel();
            ViewSpot poi = new ViewSpot();
            poi.id = (ObjectId) poiDB.get("_id");
            poi.name = poiDB.get("name").toString();
            tmp = poiDB.get("url");
            if (tmp != null)
                poi.url = tmp.toString();
            tmp = poiDB.get("price");
            if (tmp != null && tmp instanceof Number)
                poi.price = ((Number) tmp).doubleValue();
            tmp = poiDB.get("priceDesc");
            if (tmp != null)
                poi.priceDesc = tmp.toString();
            tmp = poiDB.get("desc");
            if (tmp != null)
                poi.desc = tmp.toString();

            tmp = poiDB.get("imageList");
            if (tmp != null && tmp instanceof BasicDBList) {
                BasicDBList imageListDB = (BasicDBList) tmp;
                List<String> imageList = new ArrayList<>();
                for (Object obj : imageListDB)
                    imageList.add(obj.toString());
                poi.imageList = imageList;
            }
            tmp = poiDB.get("alias");
            if (tmp != null && tmp instanceof BasicDBList) {
                BasicDBList imageListDB = (BasicDBList) tmp;
                List<String> imageList = new ArrayList<>();
                for (Object obj : imageListDB)
                    imageList.add(obj.toString());
                poi.alias = imageList;
            }
            tmp = poiDB.get("tags");
            if (tmp != null && tmp instanceof BasicDBList) {
                BasicDBList imageListDB = (BasicDBList) tmp;
                List<String> imageList = new ArrayList<>();
                for (Object obj : imageListDB)
                    imageList.add(obj.toString());
                poi.tags = imageList;
            }
            tmp = poiDB.get("ratings");
            if (tmp != null && tmp instanceof DBObject) {
                Ratings r = new Ratings();
                Object tmp2;

                tmp2 = ((DBObject) tmp).get("level");
                if (tmp2 != null && tmp2 instanceof Number)
                    poi.rankingA = ((Number) tmp2).intValue();

                tmp2 = ((DBObject) tmp).get("score");
                if (tmp2 != null && tmp2 instanceof Number)
                    r.score = ((Number) tmp2).intValue();

                tmp2 = ((DBObject) tmp).get("foodIndex");
                if (tmp2 != null && tmp2 instanceof Number)
                    r.dinningIdx = ((Number) tmp2).intValue();

                tmp2 = ((DBObject) tmp).get("shoppingIndex");
                if (tmp2 != null && tmp2 instanceof Number)
                    r.shoppingIdx = ((Number) tmp2).intValue();

                poi.ratings = r;
            }
            tmp = poiDB.get("tel");
            if (tmp != null) {
                Contact c = new Contact();
                c.phoneList = new ArrayList<>();
                c.phoneList.add(tmp.toString());
                poi.contact = c;
            }
            tmp = poiDB.get("geo");
            if (tmp != null && tmp instanceof DBObject) {
                DBObject geo = (DBObject) tmp;
                Address addr = new Address();

                Coords coords = new Coords();
                for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
                    Object tmp2 = geo.get(k);
                    if (tmp2 == null || !(tmp2 instanceof Number))
                        continue;
                    double v = ((Number) tmp2).doubleValue();

                    Field field = coords.getClass().getField(k);
                    field.set(coords, v);
                }
                addr.coords = coords;

                SimpleRef loc = new SimpleRef();
                loc.id = (ObjectId) geo.get("locId");
                loc.zhName = geo.get("locName").toString();
                addr.loc = loc;

                Object tmp2 = geo.get("addr");
                if (tmp2 != null)
                    addr.address = tmp2.toString();
                poi.addr = addr;
            }
            tmp = poiDB.get("spotId");
            if (tmp != null && tmp instanceof Number)
                poi.spotId = ((Number) tmp).intValue();
            tmp = poiDB.get("isWorldHeritage");
            if (tmp != null && tmp instanceof Boolean)
                poi.worldHeritage = (Boolean) tmp;
            tmp = poiDB.get("contact");
            if (tmp != null && tmp instanceof DBObject) {
                DBObject tmp2 = (DBObject) tmp;
                Object tmp3 = tmp2.get("tel");
                if (tmp3 != null) {
                    Contact c = new Contact();
                    c.phoneList = Arrays.asList(tmp3.toString());
                    poi.contact = c;
                }
            }
            tmp = poiDB.get("openTime");
            if (tmp != null)
                poi.openTime = tmp.toString();

            System.out.println(String.format("%d: %s", i, poi.id.toString()));
            ds.save(poi);
        }
    }

    public static void importPoi() throws TravelPiException, NoSuchFieldException, IllegalAccessException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        DBCollection col = Utils.getMongoClient("localhost", 28017).getDB("poi").getCollection("restaurant");
        DBCursor cursor = col.find(QueryBuilder.start("_id").greaterThan(new ObjectId("53b0d14510114e05e4497daf")).get());
//        DBCursor cursor = col.find(QueryBuilder.start().get());
        System.out.println(String.format("TOTAL RECORDS: %d", cursor.count()));

        int i = 94969;
        while (cursor.hasNext()) {
            i++;
            Object tmp;
            DBObject poiDB = cursor.next();

            Restaurant poi = new Restaurant();
            poi.id = (ObjectId) poiDB.get("_id");
            poi.name = poiDB.get("name").toString();
            tmp = poiDB.get("url");
            if (tmp != null)
                poi.url = tmp.toString();
            tmp = poiDB.get("price");
            if (tmp != null && tmp instanceof Number)
                poi.price = ((Number) tmp).doubleValue();
            tmp = poiDB.get("priceDesc");
            if (tmp != null)
                poi.priceDesc = tmp.toString();
            tmp = poiDB.get("desc");
            if (tmp != null)
                poi.desc = tmp.toString();

            tmp = poiDB.get("imageList");
            if (tmp != null && tmp instanceof BasicDBList) {
                BasicDBList imageListDB = (BasicDBList) tmp;
                List<String> imageList = new ArrayList<>();
                for (Object obj : imageListDB)
                    imageList.add(obj.toString());
                poi.imageList = imageList;
            }
            tmp = poiDB.get("alias");
            if (tmp != null && tmp instanceof BasicDBList) {
                BasicDBList imageListDB = (BasicDBList) tmp;
                List<String> imageList = new ArrayList<>();
                for (Object obj : imageListDB)
                    imageList.add(obj.toString());
                poi.alias = imageList;
            }
            tmp = poiDB.get("tags");
            if (tmp != null && tmp instanceof BasicDBList) {
                BasicDBList imageListDB = (BasicDBList) tmp;
                List<String> imageList = new ArrayList<>();
                for (Object obj : imageListDB)
                    imageList.add(obj.toString());
                poi.tags = imageList;
            }
            tmp = poiDB.get("ratings");
            if (tmp != null && tmp instanceof DBObject) {
                Object tmp2 = ((DBObject) tmp).get("score");
                if (tmp2 != null && tmp2 instanceof Number) {
                    Ratings r = new Ratings();
                    r.score = ((Number) tmp2).intValue();
                    poi.ratings = r;
                }
            }
            tmp = poiDB.get("tel");
            if (tmp != null) {
                Contact c = new Contact();
                c.phoneList = new ArrayList<>();
                c.phoneList.add(tmp.toString());
                poi.contact = c;
            }
            tmp = poiDB.get("geo");
            if (tmp != null && tmp instanceof DBObject) {
                DBObject geo = (DBObject) tmp;
                Address addr = new Address();

                Coords coords = new Coords();
                for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
                    Object tmp2 = geo.get(k);
                    if (tmp2 == null || !(tmp2 instanceof Number))
                        continue;
                    double v = ((Number) tmp2).doubleValue();

                    Field field = coords.getClass().getField(k);
                    field.set(coords, v);
                }
                addr.coords = coords;

                SimpleRef loc = new SimpleRef();
                loc.id = (ObjectId) geo.get("locId");
                loc.zhName = geo.get("locName").toString();
                addr.loc = loc;

                Object tmp2 = geo.get("addr");
                if (tmp2 != null)
                    addr.address = tmp2.toString();
                poi.addr = addr;
            }

            System.out.println(String.format("%d: %s", i, poi.id.toString()));
            ds.save(poi);
        }
    }

    public static void importAirline() throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
        DBCollection col = Utils.getMongoClient("localhost", 28017).getDB("traffic").getCollection("airline");
//        DBCursor cursor = col.find(QueryBuilder.start("_id").greaterThan(new ObjectId("53b05a7e10114e05e4483b47")).get());
        DBCursor cursor = col.find(QueryBuilder.start().get());
        System.out.println(String.format("TOTAL RECORDS: %d", cursor.count()));

        int i = 0;
        while (cursor.hasNext()) {
            i++;
            Object tmp;
            DBObject poiDB = cursor.next();

            Airline node = new Airline();
            node.id = (ObjectId) poiDB.get("_id");
            node.code = poiDB.get("code").toString();
            node.name = poiDB.get("name").toString();
            tmp = poiDB.get("fullName");
            if (tmp != null)
                node.fullName = tmp.toString();
            tmp = poiDB.get("shortName");
            if (tmp != null)
                node.shortName = tmp.toString();

            System.out.println(String.format("%d: %s", i, node.id.toString()));
            ds.save(node);
        }
    }


    public static void importAirport() throws TravelPiException {
        DB db = Utils.getMongoClient().getDB("traffic");
        DBCollection col = db.getCollection("airport");
        DBCursor cursor = col.find(QueryBuilder.start().get());

        MorphiaFactory factory = MorphiaFactory.getInstance();
        Datastore ds = factory.getDatastore(MorphiaFactory.DBType.TRAFFIC);

        while (cursor.hasNext()) {
            Object tmp;
            DBObject ap = cursor.next();
//            Airport airPort = airPortfromOldDb((ObjectId) loc.get("_id"));

            Airport airport = new models.morphia.traffic.Airport();
            airport.id = (ObjectId) ap.get("_id");
            airport.zhName = (String) ap.get("name");
            airport.enName = (String) ap.get("enName");
            airport.url = (String) ap.get("url");
            airport.desc = (String) ap.get("desc");

            tmp = ap.get("tel");
            if (tmp != null) {
                Contact c = new Contact();
                c.phoneList = Arrays.asList(tmp.toString());
                airport.contact = c;
            }

            Object alias = ap.get("alias");
            if (alias != null) {
                airport.alias = Arrays.asList(((BasicDBList) alias).toArray(new String[]{""}));
            }
            Object geo = ap.get("geo");
            if (geo != null) {
                Address address = new models.morphia.geo.Address();
                Double lat = (Double) ((BasicDBObject) geo).get("lat");
                Double lng = (Double) ((BasicDBObject) geo).get("lng");
                address.address = (String) ((BasicDBObject) geo).get("addr");
                Coords coords = new models.morphia.geo.Coords();
                coords.lat = lat;
                coords.lng = lng;
                address.coords = coords;
                Object loc = ((BasicDBObject) geo).get("locality");
                if (loc != null) {
                    ObjectId locId = (ObjectId) ((BasicDBObject) loc).get("id");
                    String locName = ((BasicDBObject) loc).get("localityName").toString();
                    SimpleRef ref = new SimpleRef();
                    ref.id = locId;
                    ref.zhName = locName;
                    address.loc = ref;
                }
                airport.addr = address;
            }

            ds.save(airport);
        }
    }


    public static void importAirRoute() throws TravelPiException, IllegalAccessException, NoSuchFieldException {
        DB db = Utils.getMongoClient().getDB("traffic");
        DBCollection col = db.getCollection("air_route");
        DBCursor cursor = col.find(QueryBuilder.start().get());

        MorphiaFactory factory = MorphiaFactory.getInstance();
        Datastore ds = factory.getDatastore(MorphiaFactory.DBType.TRAFFIC);

        int i = -1;
        while (cursor.hasNext()) {
            i++;
            Object tmp;
            DBObject ar = cursor.next();

            AirRoute airRoute = new AirRoute();
            airRoute.id = (ObjectId) ar.get("_id");
            tmp = ar.get("distance");
            if (tmp != null)
                airRoute.distance = ((Number) tmp).intValue();
            airRoute.flightCode = (String) ar.get("code");
            tmp = ar.get("timeCost");
            if (tmp != null)
                airRoute.timeCost = ((Number) tmp).intValue();

            for (String k : new String[]{"depAirport", "arrAirport"}) {
                tmp = ar.get(k);
                if (tmp != null) {
                    SimpleRef ref = new SimpleRef();
                    BasicDBObject tmp2 = (BasicDBObject) tmp;
                    ref.id = (ObjectId) tmp2.get("_id");
                    ref.zhName = (String) tmp2.get("name");
                    Field field = AirRoute.class.getField(k);
                    field.set(airRoute, ref);
                }
            }
            for (Map.Entry<String, String> entry : (new HashMap<String, String>() {
                {
                    put("dep", "depLoc");
                    put("arr", "arrLoc");
                }
            }).entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();

                tmp = ar.get(k);
                if (tmp != null) {
                    SimpleRef ref = new SimpleRef();
                    BasicDBObject tmp2 = (BasicDBObject) tmp;
                    ref.id = (ObjectId) tmp2.get("_id");
                    ref.zhName = (String) tmp2.get("name");
                    Field field = AirRoute.class.getField(v);
                    field.set(airRoute, ref);
                }
            }
            for (String k : new String[]{"depTime", "arrTime"}) {
                tmp = ar.get(k);
                if (tmp == null || !(tmp instanceof Date))
                    continue;
                Field field = AirRoute.class.getField(k);
                field.set(airRoute, tmp);
            }

            tmp = ar.get("price");
            if (tmp != null && tmp instanceof DBObject) {
                DBObject tmp2 = (DBObject) tmp;
                AirPrice price = new AirPrice();
                for (String k : new String[]{"price", "tax", "surcharge", "discount"}) {
                    Number val = (Number) tmp2.get(k);
                    if (val != null) {
                        Field field = AirPrice.class.getField(k);
                        field.set(price, val.doubleValue());
                    }
                }
                price.provider = (String) tmp2.get("provider");
                airRoute.price = price;
            }

            tmp = ar.get("carrier");
            if (tmp != null && tmp instanceof DBObject) {
                DBObject tmp2 = (DBObject) tmp;
                SimpleRef carrier = new SimpleRef();
                carrier.id = (ObjectId) tmp2.get("_id");
                carrier.zhName = (String) tmp2.get("name");
                airRoute.carrier = carrier;
            }

            for (String k : new String[]{"selfChk", "meal", "nonStop", "arrTerm", "depTerm"}) {
                tmp = ar.get(k);
                if (tmp == null)
                    continue;
                Field field = AirRoute.class.getField(k);
                field.set(airRoute, (ar.get(k)));
            }

            tmp = ar.get("jetType");
            if (tmp != null && tmp instanceof DBObject) {
                DBObject tmp2 = (DBObject) tmp;
                airRoute.jetName = (String) tmp2.get("short");
                airRoute.jetFullName = (String) tmp2.get("full");
            }

            ds.save(airRoute);
            System.out.println(String.format("%d: %s", i, airRoute.id.toString()));
        }
    }

    public static void main(String[] args) {
        System.out.println("HelloWorld");
        try {
            importPoi();
//            importAirline();
//            importAirport();
//            importAirRoute();
        } catch (TravelPiException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
