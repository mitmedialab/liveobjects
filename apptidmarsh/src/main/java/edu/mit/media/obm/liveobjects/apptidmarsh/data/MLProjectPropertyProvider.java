package edu.mit.media.obm.liveobjects.apptidmarsh.data;

import java.util.List;
import java.util.Map;

import edu.mit.media.obm.liveobjects.middleware.control.LiveObjectPropertyProvider;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class MLProjectPropertyProvider extends LiveObjectPropertyProvider {

    public MLProjectPropertyProvider(Map<String, Object> properties) {
        super(properties);
    }

    public int getNumContents() {
        List<Map<String, Object>> contents = (List<Map<String, Object>>) getLiveObjectProperties().get(MLProjectContract.CONTENTS);
        return contents.size();
    }

    private Map<String, Object> config(int index) {
        if (index < 0 || index > getNumContents()) {
            throw new ArrayIndexOutOfBoundsException();
        }

        List<Map<String, Object>> contents = (List<Map<String, Object>>) getLiveObjectProperties().get(MLProjectContract.CONTENTS);
        Map<String, Object> content = contents.get(index);

        return (Map<String, Object>) content.get(MLProjectContract.CONFIG);
    }

    public String getProjectTitle(int index) {
        return (String) config(index).get(MLProjectContract.PROJECT_TITLE);
    }

    public String getProjectGroup(int index) {
        return (String) config(index).get(MLProjectContract.GROUP);
    }

    public String getProjectWebsite(int index) {
        return (String) config(index).get(MLProjectContract.PROJECT_URL);
    }

    public String getProjectDescription(int index) {
        return (String) config(index).get(MLProjectContract.PROJECT_DESCRIPTION);
    }

    public String getIconFileName(int index) {
        return (String) config(index).get(MLProjectContract.ICON);
    }

    private Map<String, Object> media(int index) {
        return (Map<String, Object>) config(index).get(MLProjectContract.MEDIA);
    }

    public String getMediaType(int index) {
        return (String) media(index).get(MLProjectContract.MEDIA_TYPE);
    }

    public String getMediaFileName(int index) {
        return (String) media(index).get(MLProjectContract.MEDIA_FILENAME);
    }

    public boolean isFavorite() {
        return ((Integer) getLiveObjectProperties().get(MLProjectContract.IS_FAVORITE)).intValue() == 1;
    }

    public int getMapLocationX() {
        Object propertyValue = getLiveObjectProperties().get(MLProjectContract.MAP_LOCATION_X);
        return ((Integer) propertyValue).intValue();
    }

    public int getMapLocationY() {
        Object propertyValue = getLiveObjectProperties().get(MLProjectContract.MAP_LOCATION_Y);
        return ((Integer) propertyValue).intValue();
    }

    public int getMapId() {
        Object propertyValue = getLiveObjectProperties().get(MLProjectContract.MAP_ID);
        return ((Integer) propertyValue).intValue();
    }
}
