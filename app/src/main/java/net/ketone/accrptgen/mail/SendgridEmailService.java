/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ketone.accrptgen.mail;

import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;
import net.ketone.accrptgen.admin.CredentialsService;
import net.ketone.accrptgen.gen.GenerationServiceApachePOI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Sendgrid service
 * @see https://cloud.google.com/compute/docs/tutorials/sending-mail/using-sendgrid
 */
@Service
public class SendgridEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(SendgridEmailService.class);

    @Autowired
    private CredentialsService credentialsService;

    private String SENDGRID_API_KEY;

    @Value("${mail.enabled}")
    private boolean SENDGRID_ENABLE;

    @Value("${mail.sender}")
    private String SENDGRID_SENDER;

    @PostConstruct
    public void init() {
        Properties props = credentialsService.getCredentials();
        SENDGRID_API_KEY = props.getProperty(CredentialsService.SENDGRID_API_KEY_PROP);
    }

    public void sendEmail(String companyName, String attachmentName, InputStream attachment) throws Exception {
        if(!SENDGRID_ENABLE) return;
        SendGrid sendgrid = new SendGrid(SENDGRID_API_KEY);
        SendGrid.Email email = new SendGrid.Email();
        email.addTo(credentialsService.getCredentials().getProperty(CredentialsService.SEND_TO_PROP));
        email.setFrom(SENDGRID_SENDER);
        email.setSubject("Accounting Report For " + companyName);
        email.setText("Please find the accounting report for " + companyName + " as attached.");

        email.addAttachment(attachmentName, attachment);

        SendGrid.Response response = sendgrid.send(email);
        if (response.getCode() != 200) {
            logger.warn(String.format("An error occured: %s", response.getMessage()));
            return;
        }
        logger.info("Email with attachment " + attachmentName + " sent.");
    }

}
