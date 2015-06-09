package edu.mit.media.obm.liveobjects.middleware.common;

/**
 * This immutable class defines how a generic content is identified.
 * The content id is identified by:
 *  1) the liveObject id
 *  2) a path for the directory
 *  3) a file name
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 *
 */
public final class ContentId {
    private final String liveObjectId;
    private final String directoryPath;
    private final String filename;

    /**
     * Creates a new instance of a content id
     * @param liveObjectId id of the liveobject
     * @param directoryPath directory path
     * @param filename name of the file
     */
    public ContentId(String liveObjectId, String directoryPath, String filename) {
        this.liveObjectId = liveObjectId;
        this.directoryPath = directoryPath;
        this.filename = filename;
    }

    /**
     * Returns the live object id of the content
     * @return the live object id
     */
    public String getLiveObjectId() {
        return liveObjectId;
    }

    /**
     * Returns the directory path of the content
     * @return the directory path
     */
    public String getDirectoryPath() {
        return directoryPath;
    }

    /**
     * Returns the file name of the content
     * @return the file name
     */
    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return liveObjectId + "/" +
                directoryPath + "/" +
                filename;
    }

    public String getRelativePath() {
        return this.toString();
    }

}
