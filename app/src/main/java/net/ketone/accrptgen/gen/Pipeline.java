package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.stats.StatisticsService;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.mail.Attachment;
import net.ketone.accrptgen.mail.EmailService;
import net.ketone.accrptgen.store.StorageService;
import net.ketone.accrptgen.tasks.TasksService;
import net.ketone.accrptgen.util.ZipUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * File generation batch processes pipeline
 * Done on a background thread
 */
@Service
@Scope("prototype")
public class Pipeline implements Runnable {

//    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);
    private static final Logger logger = Logger.getLogger(Pipeline.class.getName());

    @Autowired
    private GenerationService generationService;
    @Autowired
    private ParsingService parsingService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private TasksService tasksService;

    private String companyName;
    private Date generationTime;
    private String filename;

    public Pipeline(Date generationTime) {
        this.generationTime = generationTime;
    }

    @Override
    public void run() {
        String inputFileName = generationTime.getTime() + ".xlsm";
        logger.info("Opening file: " + inputFileName);
        try {
            byte[] workbookArr = storageService.load(inputFileName);
            XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(workbookArr));
            companyName = parsingService.extractCompanyName(workbook);
            filename = companyName + "-" + GenerationService.sdf.format(generationTime);

            byte[] preParseOutput = parsingService.preParse(workbook);

            Attachment inputXlsx = new Attachment(filename + "-plain.xlsm", workbookArr);
            // no need to use the template anymore, delete it.
            storageService.delete(inputFileName);

            logger.info("template Closing input file stream, " + preParseOutput.length + "_bytes");
            logger.info("Start parse operation for " + filename);
            AccountData data = parsingService.readFile(preParseOutput);
            data.setGenerationTime(generationTime);
            logger.info("template finished parsing, sections=" + data.getSections().size());

            // remove sheets and stringify contents
            XSSFWorkbook allDocs = new XSSFWorkbook(new ByteArrayInputStream(preParseOutput));
            Workbook allDocsFinal = parsingService.deleteSheets(
                parsingService.postProcess(allDocs), Arrays.asList(
                        "metadata", "Cover", "Contents", "Control", "Dir info", "Doc list",
                        "Section1", "Section2", "Section3", "Section4", "Section5", "Section6",
                            "Accounts (3)"));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            allDocsFinal.write(os);

            byte[] generatedDoc = generationService.generate(data);
            logger.info("Generated doc. " + generatedDoc.length + "_bytes");
            Attachment doc = new Attachment(filename + ".docx", generatedDoc);
            Attachment template = new Attachment(filename + "-allDocs.xlsm", os.toByteArray());
            List<Attachment> attachments = Arrays.asList(doc, template, inputXlsx);
            emailService.sendEmail(companyName, attachments);

            // zip files and store them just in case needed
            Map<String, byte[]> zipInput = attachments.stream()
                    .collect(Collectors.toMap(Attachment::getAttachmentName, Attachment::getData));
            storageService.store(ZipUtils.zipFiles(zipInput), filename + ".zip");

            AccountFileDto dto = new AccountFileDto();
            dto.setCompany(companyName);
            dto.setFilename(filename);
            dto.setGenerationTime(generationTime);
            dto.setStatus(AccountFileDto.Status.EMAIL_SENT.name());
            logger.info("Updating statistics for " + filename);
            statisticsService.updateTask(dto);
            logger.info("Operation complete for " + filename);

        } catch (Exception e) {
            logger.log(Level.WARNING, "Generation failed", e);
            AccountFileDto dto = new AccountFileDto();
            dto.setCompany(companyName);
            dto.setFilename(filename);
            dto.setGenerationTime(generationTime);
            dto.setStatus(AccountFileDto.Status.FAILED.name());
            try {
                statisticsService.updateTask(dto);
            } catch (IOException e1) {
                logger.log(Level.WARNING, "History file write failed", e1);
            }
            throw new RuntimeException(e);
        }

    }

}
