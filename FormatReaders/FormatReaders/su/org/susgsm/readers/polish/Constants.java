package su.org.susgsm.readers.polish;

import ru.ts.utils.data.Pair;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: VLADM
 * Date: 18.04.2007
 * Time: 18:17:59
 */
public class Constants
{

	public final static String DEFAULT_LAYER_ID = "0xFFFFFF";
	public final static String RZD_LAYER_ID = "0xFFFFF";
	public final static String RZD_ST_LAYER_ID = "0xFFFF1";
	public final static String SIGNAL_LAYER_ID = "0xFFFF2";
	public final static String TECH_LAYER_ID = "0xFFFF3";
	public static final String DEFSUPERTYPE = "UNK";
	public static final String RGN40 = "rgn40";
	public static final String RGN10 = "rgn10#20";
	public static final String RGN20 = RGN10;
	public static final String RGN80 = "rgn80";
	static public final int DEF_COLOR_LINE = 0xff818181;
	static public final int DEF_COLOR_FILL = 0xffB7E999;
	//Параметры задающие радиус пересечения точки и графического объекта в координатах экрана
	public static final int ISCDX = 10;
	public static final int ISCDY = 10;
	public static final double ZONEDEVIDER = 4.0; //На сколько нужно подлить расстояния в пикселях что бы попасть в зону точки объекта
	public static final double ZONEMV = (ISCDX * ISCDX + ISCDY * ISCDY) / (ZONEDEVIDER * ZONEDEVIDER);
	public static final int MAXPROJDISTANCE = 300;//Максимальная дистанция на которую можно проецироваться
	public static Map<Integer, Integer> livelmap = new HashMap<Integer, Integer>();
	public static Map<String, Map<Integer, String>> nmbytypes = new HashMap<String, Map<Integer, String>>();

	static
	{
		livelmap.put(24, 120);
		livelmap.put(23, 300);
		livelmap.put(22, 500);
		livelmap.put(21, 800);
		livelmap.put(20, 2000);
		livelmap.put(19, 3000);
		livelmap.put(18, 5000);
		livelmap.put(17, 12000);
		livelmap.put(16, 20000);
		livelmap.put(15, 50000);
		livelmap.put(14, 80000);
		livelmap.put(13, 200000);
		livelmap.put(12, 500000);


		Map<Integer, String> rgn40 = new HashMap<Integer, String>();

		rgn40.put(0x01, "Major HWY thick");
		rgn40.put(0x02, "Principal HWY-thick");
		rgn40.put(0x03, "Principal HWY-medium");
		rgn40.put(0x04, "Arterial Road -medium");
		rgn40.put(0x05, "Arterial Road-thick");
		rgn40.put(0x06, "Road-thin");
		rgn40.put(0x07, "Alley-thick");
		rgn40.put(0x08, "Ramp");
		rgn40.put(0x09, "Ramp");
		rgn40.put(0x0a, "Unpaved Road-thin");
		rgn40.put(0x0b, "Major HWY Connector-thick");
		rgn40.put(0x0c, "Roundabout");
		rgn40.put(0x14, "Railroad");
		rgn40.put(0x15, "Shoreline");
		rgn40.put(0x16, "Trail");
		rgn40.put(0x18, "Stream-thin");
		rgn40.put(0x19, "Time-Zone");
		rgn40.put(0x1a, "Ferry");
		rgn40.put(0x1b, "Ferry");
		rgn40.put(0x1c, "Political Boundary");
		rgn40.put(0x1d, "County Boundary");
		rgn40.put(0x1e, "Intl. Boundary");
		rgn40.put(0x1f, "River");
		rgn40.put(0x20, "Land Contour (thin) Height in feet");
		rgn40.put(0x21, "Land Contour (medium) Height in feet");
		rgn40.put(0x22, "Land Contour (thick) Height in feet");
		rgn40.put(0x23, "Depth Contour (thin) Depth in feet");
		rgn40.put(0x24, "Depth Contour (medium) Depth in feet");
		rgn40.put(0x25, "Depth Contour (thick) Depth in feet");
		rgn40.put(0x26, "Intermittent River");
		rgn40.put(0x27, "Airport Runway");
		rgn40.put(0x28, "Pipeline");
		rgn40.put(0x29, "Powerline");
		rgn40.put(0x2a, "Marine Boundary (no line)");
		rgn40.put(0x2b, "Marine Hazard (no line)");


		rgn40.put(Integer.parseInt(DEFAULT_LAYER_ID.substring(2), 16), "слой по умолчанию");
		nmbytypes.put(RGN40, rgn40);

		Map<Integer, String> rgn10_20 = new HashMap<Integer, String>();

		rgn10_20.put(0x0100, "City name(Point, fat, big)");
		rgn10_20.put(0x0600, "City name(Point, big)");
		rgn10_20.put(0x0B00, "City name (Point, small)");
		rgn10_20.put(0x0C00, "City name (Point, small)");
		rgn10_20.put(0x0D00, "City name (Point, small)");
		rgn10_20.put(0x0E00, "City name(Point, big)");
		rgn10_20.put(0x1200, "Marine info");
		rgn10_20.put(0x1400, "Region name (no Point, big)");
		rgn10_20.put(0x1E00, "Region name (no Point, middle)");
		rgn10_20.put(0x210F, "Exit(Service)");
		rgn10_20.put(0x2800, "Region name (no Point, small)");
		rgn10_20.put(0x2A00, "Dining(Other)");
		rgn10_20.put(0x2A01, "Dining(American)");
		rgn10_20.put(0x2A02, "Dining(Asian)");
		rgn10_20.put(0x2A03, "Dining(Barbecue)");
		rgn10_20.put(0x2A04, "Dining(Chinese)");
		rgn10_20.put(0x2A05, "Dining(Deli/Bakery)");
		rgn10_20.put(0x2A06, "Dining(International)");
		rgn10_20.put(0x2A07, "Fast Food");
		rgn10_20.put(0x2A08, "Dining(Italian)");
		rgn10_20.put(0x2A09, "Dining(Mexican)");
		rgn10_20.put(0x2A0A, "Dining(Pizza)");
		rgn10_20.put(0x2A0B, "Dining(Sea Food)");
		rgn10_20.put(0x2A0C, "Dining(Steak/Grill)");
		rgn10_20.put(0x2A0D, "Dining(Bagel/Donut)");
		rgn10_20.put(0x2A0E, "Dining(Cafe/Diner)");
		rgn10_20.put(0x2A0F, "Dining(French)");
		rgn10_20.put(0x2A10, "Dining(German)");
		rgn10_20.put(0x2A11, "Dining(British Isles)");
		rgn10_20.put(0x2B00, "Hotel(Other)");
		rgn10_20.put(0x2B01, "Hotel/Motel");
		rgn10_20.put(0x2B02, "Bed & Breakfast inn");
		rgn10_20.put(0x2B03, "Camping/RV-Park");
		rgn10_20.put(0x2B04, "Resort");
		rgn10_20.put(0x2C01, "Amusement Park");
		rgn10_20.put(0x2C02, "Museum/History");
		rgn10_20.put(0x2C03, "Libraries");
		rgn10_20.put(0x2C04, "Land Mark");
		rgn10_20.put(0x2C05, "School");
		rgn10_20.put(0x2C06, "Park");
		rgn10_20.put(0x2C07, "Zoo");
		rgn10_20.put(0x2C08, "Sportpark, Stadium,(point)");
		rgn10_20.put(0x2C09, "Fair, Conference(point)");
		rgn10_20.put(0x2C0A, "Vine restaurant(point)");
		rgn10_20.put(0x2C0B, "Place of Worship");
		rgn10_20.put(0x2C0C, "Hot Spring");
		rgn10_20.put(0x2D01, "Theater");
		rgn10_20.put(0x2D02, "Bar");
		rgn10_20.put(0x2D03, "Cinema");
		rgn10_20.put(0x2D04, "Casino");
		rgn10_20.put(0x2D05, "Golf");
		rgn10_20.put(0x2D06, "Skiing Center");
		rgn10_20.put(0x2D07, "Bowling");
		rgn10_20.put(0x2D08, "Ice/Sporting");
		rgn10_20.put(0x2D09, "Swimming");
		rgn10_20.put(0x2D0A, "Sports(point)");
		rgn10_20.put(0x2D0B, "Sailing Airport");
		rgn10_20.put(0x2E01, "Department Store");
		rgn10_20.put(0x2E02, "Grocery");
		rgn10_20.put(0x2E03, "General Merchandiser");
		rgn10_20.put(0x2E04, "Shopping Center");
		rgn10_20.put(0x2E05, "Pharmacy");
		rgn10_20.put(0x2E06, "Convenience");
		rgn10_20.put(0x2E07, "Apparel");
		rgn10_20.put(0x2E08, "House and Garden");
		rgn10_20.put(0x2E09, "Home Furnishing");
		rgn10_20.put(0x2E0a, "Special Retail");
		rgn10_20.put(0x2E0b, "Computer/Software");
		rgn10_20.put(0x2F00, "generic service");
		rgn10_20.put(0x2F01, "Fuel/Gas");
		rgn10_20.put(0x2F02, "Car Rental");
		rgn10_20.put(0x2F03, "Car Repair");
		rgn10_20.put(0x2F04, "Airport");
		rgn10_20.put(0x2F05, "Post Office");
		rgn10_20.put(0x2F06, "Bank");
		rgn10_20.put(0x2F07, "Car Dealer(point)");
		rgn10_20.put(0x2F08, "Bus Station");
		rgn10_20.put(0x2F09, "Marina");
		rgn10_20.put(0x2F0A, "Wrecker Service");
		rgn10_20.put(0x2F0B, "Parking");
		rgn10_20.put(0x2F0C, "Restroom");
		rgn10_20.put(0x2F0D, "Automobile Club");
		rgn10_20.put(0x2F0E, "Car Wash");
		rgn10_20.put(0x2F0F, "Garmin Dealer");
		rgn10_20.put(0x2F10, "Personal Service");
		rgn10_20.put(0x2F11, "Business Service");
		rgn10_20.put(0x2F12, "Communication");
		rgn10_20.put(0x2F13, "Repair Service");
		rgn10_20.put(0x2F14, "Social Service");
		rgn10_20.put(0x2F15, "Utility");
		rgn10_20.put(0x2F16, "Truck Stop");
		rgn10_20.put(0x3000, "generic emergency/government");
		rgn10_20.put(0x3001, "Police Station");
		rgn10_20.put(0x3002, "Hospital");
		rgn10_20.put(0x3003, "Public Office");
		rgn10_20.put(0x3004, "Justice");
		rgn10_20.put(0x3005, "Concert hall(point)");
		rgn10_20.put(0x3006, "Border Station(point)");
		rgn10_20.put(0x4000, "Golf");
		rgn10_20.put(0x4100, "Fish");
		rgn10_20.put(0x4200, "Wreck");
		rgn10_20.put(0x4300, "Marina");
		rgn10_20.put(0x4400, "Gas");
		rgn10_20.put(0x4500, "Restaurant");
		rgn10_20.put(0x4600, "Bar");
		rgn10_20.put(0x4700, "Boat Ramp");
		rgn10_20.put(0x4800, "Camping");
		rgn10_20.put(0x4900, "Park");
		rgn10_20.put(0x4A00, "Picnic Area");
		rgn10_20.put(0x4B00, "Hospital");
		rgn10_20.put(0x4C00, "Information");
		rgn10_20.put(0x4D00, "Parking");
		rgn10_20.put(0x4E00, "Restroom");
		rgn10_20.put(0x4F00, "Shower");
		rgn10_20.put(0x5000, "Drinking Water");
		rgn10_20.put(0x5100, "Telephone");
		rgn10_20.put(0x5200, "Scenic Area");
		rgn10_20.put(0x5300, "Skiing");
		rgn10_20.put(0x5400, "Swimming");
		rgn10_20.put(0x5500, "Dam");
		rgn10_20.put(0x5700, "Danger Area");
		rgn10_20.put(0x5800, "restricted Area");
		rgn10_20.put(0x5900, "Generic Airport");
		rgn10_20.put(0x5901, "Large Airport");
		rgn10_20.put(0x5902, "Medium Airport");
		rgn10_20.put(0x5903, "Small Airport");
		rgn10_20.put(0x5904, "Heliport");
		rgn10_20.put(0x5905, "Airport");
		rgn10_20.put(0x5D00, "Daymark,Green Square");
		rgn10_20.put(0x5E00, "Daymark,Red Triangle");
		rgn10_20.put(0x6200, "Depth with point in feet one decimal place");
		rgn10_20.put(0x6300, "Height without point in feet no decimal place");
		rgn10_20.put(0x6400, "Manmade Feature");
		rgn10_20.put(0x6401, "Bridge");
		rgn10_20.put(0x6402, "Building");
		rgn10_20.put(0x6403, "Cemetery");
		rgn10_20.put(0x6404, "Church");
		rgn10_20.put(0x6405, "Civil");
		rgn10_20.put(0x6406, "Crossing");
		rgn10_20.put(0x6407, "Dam");
		rgn10_20.put(0x6408, "Hospital");
		rgn10_20.put(0x6409, "Levee");
		rgn10_20.put(0x640A, "Locale");
		rgn10_20.put(0x640B, "Military");
		rgn10_20.put(0x640C, "Mine");
		rgn10_20.put(0x640D, "Oil Field");
		rgn10_20.put(0x640E, "Park");
		rgn10_20.put(0x640F, "Post");
		rgn10_20.put(0x6410, "School");
		rgn10_20.put(0x6411, "Tower");
		rgn10_20.put(0x6412, "Trail");
		rgn10_20.put(0x6413, "Tunnel");
		rgn10_20.put(0x6414, "Drink water");
		rgn10_20.put(0x6415, "Ghost Town");
		rgn10_20.put(0x6416, "Subdivision");
		rgn10_20.put(0x6500, "Water Feature");
		rgn10_20.put(0x6501, "Arroyo");
		rgn10_20.put(0x6502, "Sand Bar");
		rgn10_20.put(0x6503, "Bay");
		rgn10_20.put(0x6504, "Bend");
		rgn10_20.put(0x6505, "Canal");
		rgn10_20.put(0x6506, "Channel");
		rgn10_20.put(0x6507, "Cove");
		rgn10_20.put(0x6508, "Falls");
		rgn10_20.put(0x6509, "Geyser");
		rgn10_20.put(0x650A, "Glacier");
		rgn10_20.put(0x650B, "Harbour");
		rgn10_20.put(0x650C, "Island");
		rgn10_20.put(0x650E, "Rapids");
		rgn10_20.put(0x650F, "Reservoir");
		rgn10_20.put(0x6510, "Sea");
		rgn10_20.put(0x6511, "Spring");
		rgn10_20.put(0x6512, "Stream");
		rgn10_20.put(0x6513, "Swamp");
		rgn10_20.put(0x6600, "Land Feature");
		rgn10_20.put(0x6602, "Area");
		rgn10_20.put(0x6603, "Basin");
		rgn10_20.put(0x6604, "Beach");
		rgn10_20.put(0x6605, "Bench");
		rgn10_20.put(0x6606, "Cape");
		rgn10_20.put(0x6607, "Cliff");
		rgn10_20.put(0x6608, "Crater");
		rgn10_20.put(0x6609, "Flat");
		rgn10_20.put(0x660A, "Forest");
		rgn10_20.put(0x660B, "Gap");
		rgn10_20.put(0x660C, "Gut");
		rgn10_20.put(0x660D, "Isthmus");
		rgn10_20.put(0x660E, "Lava");
		rgn10_20.put(0x660F, "Pillar");
		rgn10_20.put(0x6610, "Plain");
		rgn10_20.put(0x6611, "Range");
		rgn10_20.put(0x6612, "Reserve");
		rgn10_20.put(0x6613, "Ridge");
		rgn10_20.put(0x6614, "Rock");
		rgn10_20.put(0x6615, "Slope");
		rgn10_20.put(0x6616, "Summit");
		rgn10_20.put(0x6616, "Summit");
		rgn10_20.put(0x6617, "Valley");
		rgn10_20.put(0x6618, "Woods");
		rgn10_20.put(0x1C00, "unclassified Obstruction");
		rgn10_20.put(0x1C01, "Wreck");
		rgn10_20.put(0x1C02, "submerged Wreck, dangerous");
		rgn10_20.put(0x1C03, "submerged Wreck, non-dangerous");
		rgn10_20.put(0x1C04, "Wreck, cleared by Wire-drag");
		rgn10_20.put(0x1C05, "Obstruction, visible at high Water");
		rgn10_20.put(0x1C06, "Obstruction, awash");
		rgn10_20.put(0x1C07, "Obstruction, submerged");
		rgn10_20.put(0x1C08, "Obstruction, cleared by Wire-drag");
		rgn10_20.put(0x1C09, "Rock, awash");
		rgn10_20.put(0x1C0A, "Rock, submerged at low Water");
		rgn10_20.put(0x1C0B, "Sounding");
		rgn10_20.put(0x1D01, "Tide Prediction");
		rgn10_20.put(0x1B01, "Fog Horn");
		rgn10_20.put(0x1A01, "Fog Horn");
		rgn10_20.put(0x1901, "Fog Horn");
		rgn10_20.put(0x1801, "Fog Horn");
		rgn10_20.put(0x1701, "Fog Horn");
		rgn10_20.put(0x1601, "Fog Horn");
		rgn10_20.put(0x1B02, "Radio Beacon");
		rgn10_20.put(0x1A02, "Radio Beacon");
		rgn10_20.put(0x1902, "Radio Beacon");
		rgn10_20.put(0x1802, "Radio Beacon");
		rgn10_20.put(0x1702, "Radio Beacon");
		rgn10_20.put(0x1602, "Radio Beacon");
		rgn10_20.put(0x1B03, "Racon");
		rgn10_20.put(0x1A03, "Racon");
		rgn10_20.put(0x1903, "Racon");
		rgn10_20.put(0x1803, "Racon");
		rgn10_20.put(0x1703, "Racon");
		rgn10_20.put(0x1603, "Racon");
		rgn10_20.put(0x1B04, "Daybeacon, red Triangle");
		rgn10_20.put(0x1A04, "Daybeacon, red Triangle");
		rgn10_20.put(0x1904, "Daybeacon, red Triangle");
		rgn10_20.put(0x1804, "Daybeacon, red Triangle");
		rgn10_20.put(0x1704, "Daybeacon, red Triangle");
		rgn10_20.put(0x1604, "Daybeacon, red Triangle");
		rgn10_20.put(0x1B05, "Daybeacon, green Square");
		rgn10_20.put(0x1A05, "Daybeacon, green Square");
		rgn10_20.put(0x1905, "Daybeacon, green Square");
		rgn10_20.put(0x1805, "Daybeacon, green Square");
		rgn10_20.put(0x1705, "Daybeacon, green Square");
		rgn10_20.put(0x1605, "Daybeacon, green Square");
		rgn10_20.put(0x1B06, "Daybeacon, white Diamond");
		rgn10_20.put(0x1A06, "Daybeacon, white Diamond");
		rgn10_20.put(0x1906, "Daybeacon, white Diamond");
		rgn10_20.put(0x1806, "Daybeacon, white Diamond");
		rgn10_20.put(0x1706, "Daybeacon, white Diamond");
		rgn10_20.put(0x1606, "Daybeacon, white Diamond");
		rgn10_20.put(0xf006, "Railroad Station");
		rgn10_20.put(Integer.parseInt(DEFAULT_LAYER_ID.substring(2), 16), "слой по умолчанию");

		nmbytypes.put(RGN10, rgn10_20);

		Map<Integer, String> rgn80 = new HashMap<Integer, String>();


		rgn80.put(0x01, "City");
		rgn80.put(0x02, "City");
		rgn80.put(0x03, "City");
		rgn80.put(0x04, "Military");
		rgn80.put(0x05, "Parking Lot");
		rgn80.put(0x06, "Parking Garage");
		rgn80.put(0x07, "Airport");
		rgn80.put(0x08, "Shopping Center");
		rgn80.put(0x09, "Marina");
		rgn80.put(0x0a, "University");
		rgn80.put(0x0b, "Hospital");
		rgn80.put(0x0c, "Industrial");
		rgn80.put(0x0d, "Reservation");
		rgn80.put(0x0e, "Airport Runway");
		rgn80.put(0x13, "Man made area");
		rgn80.put(0x14, "National park");
		rgn80.put(0x15, "National park");
		rgn80.put(0x16, "National park");
		rgn80.put(0x17, "City Park");
		rgn80.put(0x18, "Golf");
		rgn80.put(0x19, "Sport");
		rgn80.put(0x1a, "Cemetery");
		rgn80.put(0x1e, "State Park");
		rgn80.put(0x1f, "State Park");
		rgn80.put(0x28, "Ocean");
		rgn80.put(0x3b, "Blue-Unknown");
		rgn80.put(0x32, "Sea");
		rgn80.put(0x3b, "Blue-Unknown");
		rgn80.put(0x3c, "Lake");
		rgn80.put(0x3d, "Lake");
		rgn80.put(0x3e, "Lake");
		rgn80.put(0x3f, "Lake");
		rgn80.put(0x40, "Lake");
		rgn80.put(0x41, "Lake");
		rgn80.put(0x42, "Lake");
		rgn80.put(0x43, "Lake");
		rgn80.put(0x44, "Lake");
		rgn80.put(0x45, "Blue-Unknown");
		rgn80.put(0x46, "River");
		rgn80.put(0x47, "River");
		rgn80.put(0x48, "River");
		rgn80.put(0x49, "River");
		rgn80.put(0x4b, "Background");
		rgn80.put(0x4c, "Intermittent River/Lake");
		rgn80.put(0x4d, "Glaciers");
		rgn80.put(0x4e, "Scrub");
		rgn80.put(0x4f, "Orchard or plantation");
		rgn80.put(0x50, "Woods");
		rgn80.put(0x51, "Wetland");
		rgn80.put(0x52, "Tundra");
		rgn80.put(0x53, "Flats");
		rgn80.put(Integer.parseInt(DEFAULT_LAYER_ID.substring(2), 16), "слой по умолчанию");

		nmbytypes.put(RGN80, rgn80);


		Map<Integer, String> defrgntypes = new HashMap<Integer, String>();

		defrgntypes.put(Integer.parseInt(SIGNAL_LAYER_ID.substring(2), 16), "Сигнальные слой");
		defrgntypes.put(Integer.parseInt(RZD_ST_LAYER_ID.substring(2), 16), "Жел. станции");
		defrgntypes.put(Integer.parseInt(RZD_LAYER_ID.substring(2), 16), "Жел. дорога");
		defrgntypes.put(Integer.parseInt(TECH_LAYER_ID.substring(2), 16), "Тех. слой");
		defrgntypes.put(Integer.parseInt(DEFAULT_LAYER_ID.substring(2), 16), "слой по умолчанию");
		nmbytypes.put(DEFSUPERTYPE, defrgntypes);

	}

	static public CodePair[] getLayerCodePair(String supertype)
	{
		LinkedList<CodePair> rv = new LinkedList<CodePair>();
		Map<Integer, String> rgntypes = nmbytypes.get(supertype);

		if (rgntypes == null)
			rgntypes = nmbytypes.get(DEFSUPERTYPE);

		Set<Integer> ks = rgntypes.keySet();
		for (Integer k : ks)
			rv.add(new CodePair(k, rgntypes.get(k)));

		Collections.sort(rv, new Comparator<CodePair>()
		{

			public int compare(CodePair o1, CodePair o2)
			{
				return o1.toString().compareToIgnoreCase(o2.toString());
			}
		});
		return rv.toArray(new CodePair[rv.size()]);
	}

//	static public String getHRNameLayer(String supertype,Integer typeindex)
//	{
//		Map<Integer, String> rgntypes = nmbytypes.get(supertype);
//		if (rgntypes == null)
//			rgntypes = nmbytypes.get(DEFSUPERTYPE);
//
//		String s = rgntypes.get(typeindex);
//		return s==null?DEFSUPERTYPE:s;
//	}

	static public int findCodePair(String str, CodePair[] pr)
	{
		for (int i = 0; i < pr.length; i++)
		{
			CodePair codePair = pr[i];
			if (codePair.toString().equalsIgnoreCase(str))
				return i;
		}
		return -1;
	}

	static public class CodePair
	{
		Pair<Integer, String> pr;
		private String rawlayerType="_"+DEFSUPERTYPE+"_"+DEFAULT_LAYER_ID;

		CodePair(int code, String name)
		{
			pr = new Pair<Integer, String>(code, name);
		}

		public String getRawlayerType()
		{
			return rawlayerType;
		}

		public void setRawlayerType(String rawlayerType)
		{
			this.rawlayerType = rawlayerType;
		}

		public String toString()
		{
			return pr.second;
		}

		Integer getCode()
		{
			return pr.first;
		}
	}
}
