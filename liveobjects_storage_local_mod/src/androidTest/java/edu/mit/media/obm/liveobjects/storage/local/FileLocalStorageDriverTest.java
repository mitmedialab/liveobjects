package edu.mit.media.obm.liveobjects.storage.local;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class FileLocalStorageDriverTest extends ApplicationTestCase<Application> {
    FileLocalStorageDriver mLocalStorageDriver;
    private static final String MOCK_TEXT_FILENAME = "file.txt";
    private static final String MOCK_FOLDER_NAME = "folder";
    private static final String MOCK_TEXT_BODY = "body";

    public FileLocalStorageDriverTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLocalStorageDriver = new FileLocalStorageDriver(getContext());
    }


    public void testPrecondition() {
        assertNotNull(getContext());
    }

    public void testWriteNewRawFileFromString() {


    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
