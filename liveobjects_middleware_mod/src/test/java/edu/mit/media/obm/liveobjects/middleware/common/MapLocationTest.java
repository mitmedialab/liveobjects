package edu.mit.media.obm.liveobjects.middleware.common;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by arata on 9/11/15.
 */
public class MapLocationTest {
    private MapLocation testMapLocation;

    @BeforeMethod
    public void setUp() throws Exception {
        testMapLocation = new MapLocation(100, 200, 10);
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @DataProvider(name = "LegalLocations")
    public static Object[][] provideLegalLocations() {
        return new Object[][] {
                { 0, 0, 0 },
                { 255, 255, 15}
        };
    }

    @Test(dataProvider = "LegalLocations")
    public void constructorShouldConstructForLegalLocation(int x, int y, int id) throws Exception {
        MapLocation mapLocation = new MapLocation(x, y, id);
        assertEquals(mapLocation.getX(), x);
        assertEquals(mapLocation.getY(), y);
        assertEquals(mapLocation.getId(), id);
    }

    @DataProvider(name = "IllegalLocations")
    public static Object[][] provideIllegalLocations() {
        return new Object[][] {
                {  -1,   0,  0 },
                { 256,   0,  0 },
                {   0,  -1,  0 },
                {   0, 256,  0 },
                {   0,   0, -1 },
                {   0,   0, 16 }
        };
    }

    @Test(dataProvider = "IllegalLocations",
            expectedExceptions = IllegalArgumentException.class)
    public void constructorShouldThrowForIllegalLocation(int x, int y, int id) throws Exception {
        new MapLocation(x, y, id);
    }

    @Test
    public void getXShouldGetX() throws Exception {
        int x = testMapLocation.getX();
        assertEquals(x, 100);
    }

    @Test
    public void getYShouldGetY() throws Exception {
        int y = testMapLocation.getY();
        assertEquals(y, 200);

    }

    @Test
    public void getIdShouldGetId() throws Exception {
        int id = testMapLocation.getId();
        assertEquals(id, 10);
    }

    @Test
    public void toStringShouldConvertToString() throws Exception {
        String string = testMapLocation.toString();
        assertEquals(string, "(100, 200, 10)");
    }

    @DataProvider(name = "EqualTestSets")
    public static Object[][] provideEqualTestSets() {
        return new Object[][] {
                { 100, 200, 10, true },
                { 101, 200, 10, false },
                { 100, 201, 10, false },
                { 100, 200, 11, false }
        };
    }

    @Test(dataProvider = "EqualTestSets")
    public void equalsShouldReturnTrueIfTheObjectHasSameLocation(int x, int y, int id, boolean expectedIsEqual) throws Exception {
        boolean isEqual = testMapLocation.equals(new MapLocation(x, y, id));
        assertEquals(isEqual, expectedIsEqual);
    }
}