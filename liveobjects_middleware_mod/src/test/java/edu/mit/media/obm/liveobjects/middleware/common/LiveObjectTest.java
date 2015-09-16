package edu.mit.media.obm.liveobjects.middleware.common;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by arata on 9/11/15.
 */
public class LiveObjectTest {
    private static final String TEST_NAME = "testName";
    private static final MapLocation testMapLocation = new MapLocation(100, 200, 10);
    private LiveObject testLiveObjectWithLocation;
    private LiveObject testLiveObjectWithoutLocation;

    @BeforeMethod
    public void setUp() throws Exception {
        testLiveObjectWithLocation = new LiveObject(TEST_NAME, testMapLocation);
        testLiveObjectWithoutLocation = new LiveObject(TEST_NAME);
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @DataProvider(name = "oneConstructorArguments")
    public Object[][] provideOneConstructorArguments() {
        return new Object[][] {
                { TEST_NAME, true },
                { null, false },
        };
    }

    @Test(dataProvider = "oneConstructorArguments")
    public void oneArgumentsConstructorShouldInitializeFields(String name, boolean constructable) throws Exception {
        try {
            LiveObject liveObject = new LiveObject(name);

            if (!constructable) {
                fail("should have thrown an exception");
            }

            assertEquals(liveObject.getName(), name);
            assertEquals(liveObject.getMapLocation(), null);
        } catch (IllegalArgumentException exception) {
            if (constructable) {
                fail("should not have thrown an exception");
            }
        }
    }

    @DataProvider(name = "twoConstructorArguments")
    public Object[][] provideTwoConstructorArguments() {
        return new Object[][] {
                { TEST_NAME , testMapLocation, true },
                { TEST_NAME ,            null, false },
                { null      , testMapLocation, false },
                { null      ,            null, false },
        };
    }

    @Test(dataProvider = "twoConstructorArguments")
    public void twoArgumentsConstructorShouldInitializeFields(String name, MapLocation mapLocation, boolean constructable) throws Exception {
        try {
            LiveObject liveObject = new LiveObject(name, mapLocation);

            if (!constructable) {
                fail("should have thrown an exception");
            }

            assertEquals(liveObject.getName(), name);
            assertEquals(liveObject.getMapLocation(), mapLocation);
        } catch (IllegalArgumentException exception) {
            if (constructable) {
                fail("should not have thrown an exception");
            }
        }
    }

    @Test
    public void toStringShouldConvertToStringWithLocation() throws Exception {
        String string = testLiveObjectWithLocation.toString();
        assertEquals(string, "testName (100, 200, 10)");
    }

    @Test
    public void toStringShouldConvertToStringWithoutLocation() throws Exception {
        String string = testLiveObjectWithoutLocation.toString();
        assertEquals(string, "testName");
    }

    @DataProvider(name = "EqualTestSetsWithoutLocation")
    public static Object[][] provideEqualTestWithSets() {
        return new Object[][] {
                { "testName" , true },
                { "testName?", false }
        };
    }

    @Test(dataProvider = "EqualTestSetsWithoutLocation")
    public void equalsShouldReturnTrueIfTheObjectHasName(
            String name, boolean expectedIsEqual) throws Exception {
        LiveObject liveObject = new LiveObject(name);

        boolean isEqual = liveObject.equals(testLiveObjectWithoutLocation);
        assertEquals(isEqual, expectedIsEqual);
    }

    @DataProvider(name = "EqualTestSetsWithLocation")
    public static Object[][] provideEqualTestSetsWithLocation() {
        return new Object[][] {
                { "testName" , 100, 200, 10, true },
                { "testName?", 100, 200, 10, false },
                { "testName" , 101, 200, 10, false },
                { "testName" , 100, 201, 10, false },
                { "testName" , 100, 200, 11, false }
        };
    }

    @Test(dataProvider = "EqualTestSetsWithLocation")
    public void equalsShouldReturnTrueIfTheObjectHasSameNameAndLocation(
            String name, int x, int y, int id, boolean expectedIsEqual) throws Exception {
        MapLocation mapLocation = new MapLocation(x, y, id);
        LiveObject liveObject = new LiveObject(name, mapLocation);

        boolean isEqual = liveObject.equals(testLiveObjectWithLocation);
        assertEquals(isEqual, expectedIsEqual);
    }

    @Test
    public void equalsShouldReturnFalseIfCompareOneWithLocationToOneWithoutLocation() throws Exception {
        boolean isEqual = testLiveObjectWithLocation.equals(testLiveObjectWithoutLocation);
        assertEquals(isEqual, false);
    }
}