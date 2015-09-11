package edu.mit.media.obm.liveobjects.driver.wifi;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.common.MapLocation;

import static org.testng.Assert.*;

/**
 * Created by arata on 9/11/15.
 */
public class PositionedSsidTranslatorTest {
    private PositionedSsidTranslator positionedSsidTranslator;
    private static String DEFAULT_PREFIX = "liveobj-";
    private static char DEFAULT_DELIMITER = '@';
    private static int DEFAULT_X_LENGTH_IN_HEX = 2;
    private static int DEFAULT_Y_LENGTH_IN_HEX = 2;
    private static int DEFAULT_ID_LENGTH_IN_HEX = 1;

    private static final String TEST_SSID = "liveobj-ObjForTest@2a3bc";

    @BeforeMethod
    public void setUp() throws Exception {
        positionedSsidTranslator = new PositionedSsidTranslator(DEFAULT_PREFIX, DEFAULT_DELIMITER,
                DEFAULT_X_LENGTH_IN_HEX, DEFAULT_Y_LENGTH_IN_HEX, DEFAULT_ID_LENGTH_IN_HEX);
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @DataProvider(name = "IsLegalTestSets")
    public static Object[][] provideIsLegalTestSets() {
        return new Object[][] {
                { TEST_SSID                          , true , "valid ssid" },
                { "liveobj+ObjForTest@2a3bc"         , false, "wrong prefix" },
                { "liveobj-LoooooooooooongSsid@2a3bc", false, "longer than the maximum 32 characters" },
                { "liveobj-@2a3bc"                   , false, "no name field contained" },
                { "liveobj-ObjForTest2a3bc"          , false, "no delimiter after name" },
                { "liveobj-ObjForTest@2a3b"          , false, "missing character in location" },
                { "liveobj-ObjForTest@2a3bc0"        , false, "unnecessary character trailing location" },
                { "0liveobj-ObjForTest@2a3bc"        , false, "unrecognized character preceding" }
        };
    }

    @Test(dataProvider = "IsLegalTestSets")
    public void shouldBeAbleToJudgeIfDeviceIdIsLegal(
            String deviceId, boolean expectedIsLegalSsid, String message) throws Exception {
        boolean isLegalSsid = positionedSsidTranslator.isLiveObject(deviceId);
        assertEquals(isLegalSsid, expectedIsLegalSsid, message);
    }

    @DataProvider(name = "DeviceIdAndLiveObjectPairs")
    public static Object[][] provideDeviceIdAndLiveObjectPairs() {
        return new Object[][] {
                { "liveobj-ObjForTest@2a3bc"         , "ObjForTest"        , 0x2a, 0x3b, 0xc },
                { "liveobj-O@2a3bc"                  , "O"                 , 0x2a, 0x3b, 0xc },
                { "liveobj-ObjForTest01234567@2a3bc" , "ObjForTest01234567", 0x2a, 0x3b, 0xc },
                { "liveobj-ObjForTest@003bc"         , "ObjForTest"        , 0x00, 0x3b, 0xc },
                { "liveobj-ObjForTest@ff3bc"         , "ObjForTest"        , 0xff, 0x3b, 0xc },
                { "liveobj-ObjForTest@2a00c"         , "ObjForTest"        , 0x2a, 0x00, 0xc },
                { "liveobj-ObjForTest@2affc"         , "ObjForTest"        , 0x2a, 0xff, 0xc },
                { "liveobj-ObjForTest@2a3b0"         , "ObjForTest"        , 0x2a, 0x3b, 0x0 },
                { "liveobj-ObjForTest@2a3bf"         , "ObjForTest"        , 0x2a, 0x3b, 0xf }
        };
    }

    @Test(dataProvider = "DeviceIdAndLiveObjectPairs")
    public void shouldTranslateLegalDeviceIdToLiveObject(
            String deviceId, String liveObjectName, int locationX, int locationY, int locationId) throws Exception {
        MapLocation mapLocation = new MapLocation(locationX, locationY, locationId);
        LiveObject liveObject = new LiveObject(liveObjectName, mapLocation);
        LiveObject translatedLiveObject = positionedSsidTranslator.translateToLiveObject(deviceId);

        assertEquals(translatedLiveObject, liveObject);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowWhenTryToTranslateIllegalDeviceId() throws Exception {
        positionedSsidTranslator.translateToLiveObject("IllegalDeviceID");
    }

    @Test(dataProvider = "DeviceIdAndLiveObjectPairs")
    public void shouldTranslateLiveObjectToDeviceId(
            String deviceId, String liveObjectName, int locationX, int locationY, int locationId) throws Exception {
        MapLocation mapLocation = new MapLocation(locationX, locationY, locationId);
        LiveObject liveObject = new LiveObject(liveObjectName, mapLocation);
        String  translatedDeviceId = positionedSsidTranslator.translateFromLiveObject(liveObject);

        assertEquals(translatedDeviceId, deviceId);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowIfTryToTranslateLiveObjectWithoutLocation() throws Exception {
        LiveObject liveObject = new LiveObject(TEST_SSID);
        positionedSsidTranslator.translateFromLiveObject(liveObject);
    }

    @DataProvider(name = "VarietyOfDeviceIdFormats")
    public static Object[][] provideVarietyOfDeviceIdFormats() {
        return new Object[][]{
                {
                        "test_prefix-", DEFAULT_DELIMITER,
                        DEFAULT_X_LENGTH_IN_HEX, DEFAULT_Y_LENGTH_IN_HEX, DEFAULT_ID_LENGTH_IN_HEX,
                        "test_prefix-ObjForTest@2a3bc",
                        "ObjForTest", 0x2a, 0x3b, 0xc
                },
                {
                        DEFAULT_PREFIX, '%',
                        DEFAULT_X_LENGTH_IN_HEX, DEFAULT_Y_LENGTH_IN_HEX, DEFAULT_ID_LENGTH_IN_HEX,
                        "liveobj-ObjForTest%2a3bc",
                        "ObjForTest", 0x2a, 0x3b, 0xc
                },
                {
                        DEFAULT_PREFIX, DEFAULT_DELIMITER,
                        4, DEFAULT_Y_LENGTH_IN_HEX, DEFAULT_ID_LENGTH_IN_HEX,
                        "liveobj-ObjForTest@2aff3bc",
                        "ObjForTest", 0x2aff, 0x3b, 0xc
                },
                {
                        DEFAULT_PREFIX, DEFAULT_DELIMITER,
                        DEFAULT_X_LENGTH_IN_HEX, 4, DEFAULT_ID_LENGTH_IN_HEX,
                        "liveobj-ObjForTest@2a3bffc",
                        "ObjForTest", 0x2a, 0x3bff, 0xc
                },
                {
                        DEFAULT_PREFIX, DEFAULT_DELIMITER,
                        DEFAULT_X_LENGTH_IN_HEX, DEFAULT_Y_LENGTH_IN_HEX, 2,
                        "liveobj-ObjForTest@2a3bcf",
                        "ObjForTest", 0x2a, 0x3b, 0xcf
                },
        };
    }

    @Test(dataProvider = "VarietyOfDeviceIdFormats")
    public void shouldSetDeviceIdFormat(
            String prefix, char delimiter, int xLengthInHex, int yLengthInHex, int idLengthInHex,
            String deviceId, String expectedName, int expectedX, int expectedY, int expectedId) throws Exception {
        PositionedSsidTranslator positionedSsidTranslator = new PositionedSsidTranslator(
                prefix, delimiter, xLengthInHex, yLengthInHex, idLengthInHex);

        assertTrue(positionedSsidTranslator.isLiveObject(deviceId));

        LiveObject liveObject = positionedSsidTranslator.translateToLiveObject(deviceId);
        MapLocation mapLocation = liveObject.getMapLocation();

        assertEquals(liveObject.getLiveObjectName(), expectedName);
        assertEquals(mapLocation.getX(), expectedX);
        assertEquals(mapLocation.getY(), expectedY);
        assertEquals(mapLocation.getId(), expectedId);
    }
}