package edu.mit.media.obm.liveobjects.storage.wifi;

import android.app.Application;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

import org.mockito.Mockito;

/**
 * @author Arata Miyamoto <arata@media.mit.edu>
 */
public class WifiStorageConfigTest extends ApplicationTestCase<Application> {
    private WifiStorageConfig.ContextWrapper mMockContextWrapper;
    private WifiManager mMockWifiManager;

    public WifiStorageConfigTest() {
        super(Application.class);
    }

    public void setUp() {
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());

        DhcpInfo mockDhcpInfo = Mockito.mock(DhcpInfo.class);
        mockDhcpInfo.gateway = 0x1e64a8c0;

        mMockWifiManager = Mockito.mock(WifiManager.class);
        Mockito.when(mMockWifiManager.getDhcpInfo()).thenReturn(mockDhcpInfo);

        mMockContextWrapper = Mockito.mock(WifiStorageConfig.ContextWrapper.class);
        Mockito.when(mMockContextWrapper.getSystemService(Context.WIFI_SERVICE)).thenReturn(mMockWifiManager);
        Mockito.when(mMockContextWrapper.getString(Mockito.anyInt())).thenReturn("base_dir");
    }

    public void testGetBasePath() throws RemoteException {
        String basePath = WifiStorageConfig.getMediaFolderPath(mMockContextWrapper);
        Assert.assertEquals("http://192.168.100.30/base_dir", basePath);
    }

    public void testGetBasePathWhenDhcpInfoUnavailable() {
        Mockito.when(mMockWifiManager.getDhcpInfo()).thenReturn(null);

        try {
            WifiStorageConfig.getMediaFolderPath(mMockContextWrapper);
            Assert.fail("RemoteException error should be thrown.");
        } catch (RemoteException exception) {
        }
    }
}
