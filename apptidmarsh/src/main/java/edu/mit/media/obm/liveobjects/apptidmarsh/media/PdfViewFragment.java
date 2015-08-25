package edu.mit.media.obm.liveobjects.apptidmarsh.media;

import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnPageChangeListener;
import com.noveogroup.android.log.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.middleware.common.ContentId;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Created by arata on 8/24/15.
 */
public class PdfViewFragment extends Fragment implements OnPageChangeListener {
    public static final String PDF_FILE = "sample.pdf";

    @BindString(R.string.arg_live_object_name_id) String ARG_LIVE_OBJ_NAME_ID;
    @BindString(R.string.arg_content_index) String ARG_CONTENT_INDEX;
    @BindString(R.string.state_play_position) String STATE_CURRENT_PAGE;
    @BindString(R.string.dir_contents) String MEDIA_DIRECTORY_NAME;

    @Inject ContentController mContentController;
    @Inject DbController mDbController;

    @Bind(R.id.pdfView) PDFView mPdfView;
    private int mCurrentPage = 1;

    private ContentId mContentId = null;

    public PdfViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pdf_view, container, false);
        ButterKnife.bind(this, rootView);
        DependencyInjector.inject(this, getActivity());

        if (savedInstanceState != null) {
            mCurrentPage = savedInstanceState.getInt(STATE_CURRENT_PAGE);
        }

        if (getArguments() != null) {
            Bundle arguments = getArguments();

            String liveObjNameId = arguments.getString(ARG_LIVE_OBJ_NAME_ID);
            int contentIndex = arguments.getInt(ARG_CONTENT_INDEX);

            Map<String, Object> properties = mDbController.getProperties(liveObjNameId);
            MLProjectPropertyProvider propertyProvider = new MLProjectPropertyProvider(properties);

            String fileName = propertyProvider.getMediaFileName(contentIndex);

            mContentId = new ContentId(liveObjNameId, MEDIA_DIRECTORY_NAME, fileName);
        }

        openPdfFile();

        return rootView;
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        getActivity().setTitle(String.format("%s %s / %s", PDF_FILE, page, pageCount));
    }

    private void openPdfFile() {
        try {
            File pdfFile = File.createTempFile("tempPdf", "pdf");
            InputStream inputStream = mContentController.getInputStreamContent(mContentId);
            OutputStream outputStream = new FileOutputStream(pdfFile);
            IOUtils.copy(inputStream, outputStream);
            outputStream.close();

            mPdfView.fromFile(pdfFile)
                    .defaultPage(mCurrentPage)
                    .onPageChange(this)
                    .load();
        } catch (IOException e) {
            Log.e("Failed to open pdf file", e);
            throw new RuntimeException();
        } catch (RemoteException e) {
            Log.e("Failed to open pdf file", e);
            throw new RuntimeException();
        }
    }
}
