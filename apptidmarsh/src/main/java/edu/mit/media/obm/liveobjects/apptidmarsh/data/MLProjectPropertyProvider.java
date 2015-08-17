package edu.mit.media.obm.liveobjects.apptidmarsh.data;

import java.util.Map;

import edu.mit.media.obm.liveobjects.middleware.control.LiveObjectPropertyProvider;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class MLProjectPropertyProvider extends LiveObjectPropertyProvider {

    public MLProjectPropertyProvider(Map<String, Object> properties) {
        super(properties);
    }

    private Map<String, Object> config() {
        return (Map<String, Object>) getLiveObjectProperties().get(MLProjectContract.CONFIG);
    }

    public String getProjectTitle() {
        return (String) config().get(MLProjectContract.PROJECT_TITLE);
    }

    public String getProjectGroup() {
        return (String) config().get(MLProjectContract.GROUP);
    }

    public String getProjectWebsite() {
        return (String) config().get(MLProjectContract.PROJECT_URL);
    }

    public String getProjectDescription() {
        return (String) config().get(MLProjectContract.PROJECT_DESCRIPTION);
    }

    public String getIconFileName() {
        return (String) config().get(MLProjectContract.ICON);
    }

    private Map<String, Object> media() {
        return (Map<String, Object>) config().get(MLProjectContract.MEDIA);
    }

    public String getMediaType() {
        return (String) media().get(MLProjectContract.MEDIA_TYPE);
    }

    public String getMediaFileName() {
        return (String) media().get(MLProjectContract.MEDIA_FILENAME);
    }

    public boolean isFavorite() {
        return ((Integer) getLiveObjectProperties().get(MLProjectContract.IS_FAVORITE)).intValue() == 1;
    }

    public int getMapLocationX() {
        return (int) getLiveObjectProperties().get(MLProjectContract.MAP_LOCATION_X);
    }

    public int getMapLocationY() {
        return (int) getLiveObjectProperties().get(MLProjectContract.MAP_LOCATION_Y);
    }

    public int getMapId() {
        return (int) getLiveObjectProperties().get(MLProjectContract.MAP_ID);
    }
}
