package net.ketone.accrptgen.domain.gen;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
public class AccountData {

    public static List<String> SECTION_LIST = Arrays.asList("Cover", "Contents", "Section1",
            "Section2", "Section3", "Section4", "Section5", "Section6");

    private String companyName;
    private LocalDateTime generationTime;
    private List<Section> sections;

    public void addSection(Section s) {
        if(sections == null) {
            sections = new ArrayList<>();
        }
        sections.add(s);
    }
}
