package net.ketone.accrptgen.store;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.common.base.Stopwatch;
import org.apache.commons.io.input.NullInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class GCloudStorageService implements StorageService {


    // private static final Logger logger = LoggerFactory.getLogger(GCloudStorageService.class);
    private static final Logger logger = Logger.getLogger(GCloudStorageService.class.getName());

    Storage storage;

    @Value("${gcloud.storage.bucket}")
    private String BUCKET_NAME;

    @PostConstruct
    public void init() throws IOException {
//        GoogleCredentials credentials = GoogleCredentials.fromStream(new ClassPathResource("gcloud-tests-4337e92868b4.json").getInputStream())
//                .createScoped(Arrays.asList("https://www.googleapis.com/auth/cloud-platform"));
//        StorageOptions.Builder optionsBuilder = StorageOptions.newBuilder().setCredentials(credentials);
        StorageOptions.Builder optionsBuilder = StorageOptions.newBuilder();
        storage = optionsBuilder.build().getService();
    }


    @Override
    public String store(byte[] input, String filename) throws IOException {
        logger.info("storing " + filename);
        Stopwatch stopwatch = Stopwatch.createStarted();
        String contentType = null;
        BlobInfo.Builder blobInfoBuilder =
                BlobInfo.newBuilder(BUCKET_NAME, filename);
        if(filename.endsWith(".docx")) {
            blobInfoBuilder.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else if(filename.endsWith(".txt")) {
            blobInfoBuilder.setContentType("text/plain");
        } else if(filename.endsWith(".xlsx") || filename.endsWith(".xlsm")) {
            blobInfoBuilder.setContentType("application/vnd.ms-excel");
        }
        storage.create(blobInfoBuilder.build(), input);
        logger.info("stored " + filename + " in " + stopwatch.toString());
        return filename;
    }

    @Override
    public InputStream loadAsInputStream(String filename) {
        return new ByteArrayInputStream(load(filename));
    }

    @Override
    public byte[] load(String filename) {
        logger.info("loading " + filename);
        Stopwatch stopwatch = Stopwatch.createStarted();
        BlobId blobId = BlobId.of(BUCKET_NAME, filename);
        Blob blob = storage.get(blobId);
        if (blob == null) {
            logger.warning("No such object");
            return new byte[0];
        }
        byte[] content = blob.getContent();
        logger.info("loaded " + filename + " in " + stopwatch.toString());
        return content;
    }

    @Override
    public List<String> list() {

        List<String> filenames = new ArrayList<>();

        Bucket bucket = storage.get(BUCKET_NAME);
        if (bucket == null) {
            System.out.println("No such bucket");
            return new ArrayList<>();
        }
        Page<Blob> blobs = bucket.list();
        blobs.iterateAll().forEach(b -> filenames.add(b.getName()));
        return filenames;
    }

    @Override
    public void delete(String filename) {
        BlobId blobId = BlobId.of(BUCKET_NAME, filename);
        if(blobId != null) {
            logger.info("Deleting file " + filename);
            storage.delete(blobId);
        }
    }

}
