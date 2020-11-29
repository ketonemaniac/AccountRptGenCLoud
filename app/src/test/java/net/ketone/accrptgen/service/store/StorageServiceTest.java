package net.ketone.accrptgen.service.store;

import net.ketone.accrptgen.config.Constants;
import net.ketone.accrptgen.config.LocalStorageConfig;
import net.ketone.accrptgen.config.MongoConfig;
import net.ketone.accrptgen.domain.dto.AccountJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes={LocalStorageConfig.class})
@TestPropertySource(properties = {
        "storage.persistent.folder=target/test",
        "storage.temp.folder=target/test"
})
public class StorageServiceTest {

    @Autowired
    private StorageService persistentStorage;

    private static final String HISTORY_FILE = "target" + File.separator + Constants.HISTORY_FILE;

    @Test
    public void testSaveEmptyFile() throws IOException {
        persistentStorage.delete(HISTORY_FILE);
        doSave();
    }

    @Test
    public void testSaveExistingFile() throws IOException {
        testSaveEmptyFile();
        doSave();
    }

    private AccountJob doSave() throws IOException {
        AccountJob dto = new AccountJob();
        dto.setGenerationTime(LocalDateTime.now());
        dto.setCompany("ABC Company Limited");
        dto.setStatus(Constants.Status.EMAIL_SENT.name());
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        dto.setFilename("ABC-"+ df.format(dto.getGenerationTime()) + ".docx");
        return dto;
    }
}
