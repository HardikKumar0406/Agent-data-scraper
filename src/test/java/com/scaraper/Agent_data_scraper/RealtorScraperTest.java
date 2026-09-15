package com.scaraper.Agent_data_scraper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

public class RealtorScraperTest extends BaseTest {

    private static final String URL_FILE =
            System.getProperty("user.dir")
                    + "/src/url/agent_urls.txt";

    private static final String OUTPUT_DIRECTORY =
            System.getProperty("user.dir")
                    + "/output";

    private static final String CSV_FILE =
            OUTPUT_DIRECTORY
                    + "/agent_data.csv";

    // ==========================================
    // PERFORMANCE SETTINGS
    // ==========================================

    private static final int AGENT_WAIT_SECONDS = 5;

    private static final int PAGE_LOAD_SECONDS = 5;

    private static final int MAX_ATTEMPTS = 2;

    private static final String AGENT_NAME_XPATH =
            "(//span[@class='realtorCardName'])[1]";

    @Test
    public void scrapeAgentDetails() {

        List<String> agentUrls;

        System.out.println();
        System.out.println("==========================================");
        System.out.println("       REALTOR.CA AGENT SCRAPER");
        System.out.println("==========================================");

        System.out.println(
                "Reading URL file: " + URL_FILE
        );

        // ==========================================
        // READ URL FILE
        // ==========================================

        try {

            agentUrls = Files.readAllLines(
                    Paths.get(URL_FILE)
            );

        } catch (IOException e) {

            System.out.println(
                    "Unable to read URL file."
            );

            e.printStackTrace();

            return;
        }

        System.out.println(
                "Total URLs found: " + agentUrls.size()
        );

        // ==========================================
        // CREATE OUTPUT DIRECTORY
        // ==========================================

        File outputDirectory =
                new File(OUTPUT_DIRECTORY);

        if (!outputDirectory.exists()) {

            if (outputDirectory.mkdirs()) {

                System.out.println(
                        "Output directory created: "
                                + OUTPUT_DIRECTORY
                );
            }
        }

        // ==========================================
        // DELETE OLD CSV
        // ==========================================

        File csvFile =
                new File(CSV_FILE);

        if (csvFile.exists()) {

            if (csvFile.delete()) {

                System.out.println(
                        "Old CSV deleted."
                );

            } else {

                System.out.println(
                        "Unable to delete old CSV file."
                );

                return;
            }
        }

        // ==========================================
        // CREATE NEW CSV
        // ==========================================

        try (
                BufferedWriter writer =
                        new BufferedWriter(
                                new FileWriter(
                                        CSV_FILE,
                                        false
                                )
                        )
        ) {

            // ==========================================
            // CSV HEADERS
            // ==========================================

            String[] headers = {

                    "Agent Name",
                    "Title",
                    "Agent Phone 1",
                    "Agent Phone 2",

                    "Facebook",
                    "LinkedIn",
                    "Instagram",
                    "Twitter",

                    "Realtor Website",

                    "Office Name",
                    "Office Type",
                    "Office Address",

                    "Office Phone 1",
                    "Office Phone 2",

                    "Office Website",

                    "Agent URL"
            };

            writer.write(
                    createCsvLine(headers)
            );

            writer.newLine();

            writer.flush();

            System.out.println(
                    "New CSV created."
            );

            int agentNumber = 0;

            int successfulRecords = 0;

            int failedRecords = 0;

            // ==========================================
            // PROCESS EVERY URL
            // ==========================================

            for (String agentUrl : agentUrls) {

                agentUrl = agentUrl.trim();

                // Skip empty lines
                if (agentUrl.isEmpty()) {
                    continue;
                }

                agentNumber++;

                boolean dataSaved = false;

                // ==========================================
                // TRY URL MAXIMUM 2 TIMES
                // ==========================================

                for (
                        int attempt = 1;
                        attempt <= MAX_ATTEMPTS
                                && !dataSaved;
                        attempt++
                ) {

                    WebDriver driver = null;

                    try {

                        System.out.println();
                        System.out.println(
                                "=========================================="
                        );

                        System.out.println(
                                "Processing Agent "
                                        + agentNumber
                                        + " of "
                                        + agentUrls.size()
                        );

                        System.out.println(
                                "Attempt "
                                        + attempt
                                        + " of "
                                        + MAX_ATTEMPTS
                        );

                        System.out.println(
                                "=========================================="
                        );

                        System.out.println(
                                "Opening URL: "
                                        + agentUrl
                        );

                        // ==========================================
                        // CREATE NEW CHROME
                        // ==========================================

                        driver = createDriver();

                        // ==========================================
                        // OVERRIDE PAGE LOAD TIMEOUT
                        // ==========================================

                        driver.manage()
                                .timeouts()
                                .pageLoadTimeout(
                                        Duration.ofSeconds(
                                                PAGE_LOAD_SECONDS
                                        )
                                );

                        // ==========================================
                        // OPEN AGENT URL
                        // ==========================================

                        try {

                            driver.get(agentUrl);

                        } catch (Exception e) {

                            System.out.println(
                                    "Page navigation reached timeout. "
                                            + "Checking page data..."
                            );
                        }

                        // ==========================================
                        // WAIT FOR AGENT INFORMATION
                        // ==========================================

                        System.out.println();

                        System.out.println(
                                "Waiting for agent information..."
                        );

                        boolean agentDataFound =
                                waitForAgentData(driver);

                        // ==========================================
                        // AGENT DATA NOT FOUND
                        // ==========================================

                        if (!agentDataFound) {

                            System.out.println(
                                    "Agent data not found within "
                                            + AGENT_WAIT_SECONDS
                                            + " seconds."
                            );

                            if (attempt < MAX_ATTEMPTS) {

                                System.out.println(
                                        "Closing browser and retrying..."
                                );

                            } else {

                                System.out.println(
                                        "Maximum attempts reached."
                                );
                            }

                            continue;
                        }

                        // ==========================================
                        // AGENT DATA FOUND
                        // ==========================================

                        System.out.println(
                                "Agent data found."
                        );

                        System.out.println(
                                "Scraping immediately..."
                        );

                        // ==========================================
                        // SCRAPE AGENT DATA
                        // ==========================================

                        String[] scrapedData =
                                scrapeAgentData(
                                        driver,
                                        agentUrl
                                );

                        // ==========================================
                        // WRITE DATA TO CSV
                        // ==========================================

                        writer.write(
                                createCsvLine(
                                        scrapedData
                                )
                        );

                        writer.newLine();

                        // ==========================================
                        // FLUSH AFTER EVERY RECORD
                        // ==========================================

                        writer.flush();

                        successfulRecords++;

                        dataSaved = true;

                        System.out.println();

                        System.out.println(
                                "Data saved to CSV."
                        );

                    } catch (Exception e) {

                        // ==========================================
                        // URL FAILURE
                        // ==========================================

                        System.out.println();

                        System.out.println(
                                "Error processing Agent "
                                        + agentNumber
                        );

                        System.out.println(
                                "Attempt: "
                                        + attempt
                        );

                        System.out.println(
                                "URL: "
                                        + agentUrl
                        );

                        e.printStackTrace();

                    } finally {

                        // ==========================================
                        // ALWAYS CLOSE CHROME
                        // ==========================================

                        closeDriver(driver);
                    }
                }

                // ==========================================
                // SAVE N/A AFTER BOTH ATTEMPTS FAIL
                // ==========================================

                if (!dataSaved) {

                    String[] naRecord =
                            createNARecord(
                                    agentUrl
                            );

                    try {

                        writer.write(
                                createCsvLine(
                                        naRecord
                                )
                        );

                        writer.newLine();

                        writer.flush();

                        failedRecords++;

                        System.out.println();

                        System.out.println(
                                "Agent failed after "
                                        + MAX_ATTEMPTS
                                        + " attempts."
                        );

                        System.out.println(
                                "Saved N/A record."
                        );

                    } catch (IOException e) {

                        System.out.println(
                                "Unable to write N/A record."
                        );

                        e.printStackTrace();
                    }
                }
            }

            // ==========================================
            // COMPLETED
            // ==========================================

            System.out.println();

            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "       SCRAPING COMPLETED"
            );

            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "Total URLs     : "
                            + agentUrls.size()
            );

            System.out.println(
                    "Records saved  : "
                            + successfulRecords
            );

            System.out.println(
                    "Failed records : "
                            + failedRecords
            );

            System.out.println(
                    "CSV file       : "
                            + CSV_FILE
            );

        } catch (IOException e) {

            System.out.println(
                    "Unable to create/write CSV file."
            );

            e.printStackTrace();
        }

        System.out.println();

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "       ALL URLS PROCESSED"
        );

        System.out.println(
                "=========================================="
        );
    }

    // ==========================================
    // WAIT FOR AGENT DATA
    // ==========================================

    private boolean waitForAgentData(
            WebDriver driver
    ) {

        try {

            WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(
                                    AGENT_WAIT_SECONDS
                            )
                    );

            wait.until(d -> {

                try {

                    WebElement element =
                            d.findElement(
                                    By.xpath(
                                            AGENT_NAME_XPATH
                                    )
                            );

                    return element.isDisplayed()
                            && !element
                            .getText()
                            .trim()
                            .isEmpty();

                } catch (Exception e) {

                    return false;
                }
            });

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // ==========================================
    // SCRAPE AGENT DATA
    // ==========================================

    private String[] scrapeAgentData(
            WebDriver driver,
            String agentUrl
    ) {

        // ==========================================
        // AGENT DETAILS
        // ==========================================

        String agentName =
                getText(
                        driver,
                        "(//span[@class='realtorCardName'])[1]"
                );

        String agentTitle =
                getText(
                        driver,
                        "(//div[@class='realtorCardTitle'])[1]"
                );

        // ==========================================
        // AGENT PHONE 1
        // ==========================================

        String agentPhone1 =
                getText(
                        driver,
                        "(//span[@class='realtorCardContactNumber TelephoneNumber'])[1]"
                );

        // ==========================================
        // AGENT PHONE 2
        // ==========================================

        String agentPhone2 =
                getText(
                        driver,
                        "(//span[@class='realtorCardContactNumber TollFreeNumber'])[1]"
                );

        // ==========================================
        // SOCIAL MEDIA
        // ==========================================

        String facebookUrl =
                getHrefFromAncestor(
                        driver,
                        "(//img[@src='https://static.realtor.ca/images/common/icons/svg/facebook.svg'])[1]"
                );

        String linkedinUrl =
                getHrefFromAncestor(
                        driver,
                        "(//img[@src='https://static.realtor.ca/images/common/icons/svg/linkedin.svg'])[1]"
                );

        String instagramUrl =
                getHrefFromAncestor(
                        driver,
                        "(//img[@src='https://static.realtor.ca/images/common/icons/svg/instagram.svg'])[1]"
                );

        String twitterUrl =
                getHref(
                        driver,
                        "(//a[@aria-label='Twitter Link'])[1]"
                );

        // ==========================================
        // REALTOR WEBSITE
        // ==========================================

        String realtorWebsite =
                getHrefFromAncestor(
                        driver,
                        "(//span[@class='realtorCardContactNumber'])[1]"
                );

        // ==========================================
        // OFFICE DETAILS
        // ==========================================

        String officeName =
                getText(
                        driver,
                        "(//div[@class='officeCardName'])[1]"
                );

        String officeType =
                getText(
                        driver,
                        "(//div[@class='officeCardType'])[1]"
                );

        String officeAddress =
                getText(
                        driver,
                        "(//div[@class='officeCardAddress'])[1]"
                );

        // ==========================================
        // OFFICE PHONE 1
        // TELEPHONE
        // ==========================================

        String officePhone1 =
                getText(
                        driver,
                        "(//div[@data-type='Telephone']//span[@class='officeCardContactNumber'])[1]"
                );

        // ==========================================
        // OFFICE PHONE 2
        // FAX
        // ==========================================

        String officePhone2 =
                getText(
                        driver,
                        "(//div[@data-type='Fax']//span[@class='officeCardContactNumber'])[1]"
                );

        // ==========================================
        // OFFICE WEBSITE
        // ==========================================

        String officeWebsite =
                getHrefFromAncestor(
                        driver,
                        "(//span[normalize-space()='Office Website'])[1]"
                );

        // ==========================================
        // PRINT SCRAPED DATA
        // ==========================================

        System.out.println();

        System.out.println(
                "------------------------------------------"
        );

        System.out.println(
                "SCRAPED DATA"
        );

        System.out.println(
                "------------------------------------------"
        );

        System.out.println(
                "Agent Name      : "
                        + agentName
        );

        System.out.println(
                "Title           : "
                        + agentTitle
        );

        System.out.println(
                "Agent Phone 1   : "
                        + agentPhone1
        );

        System.out.println(
                "Agent Phone 2   : "
                        + agentPhone2
        );

        System.out.println(
                "Facebook        : "
                        + facebookUrl
        );

        System.out.println(
                "LinkedIn        : "
                        + linkedinUrl
        );

        System.out.println(
                "Instagram       : "
                        + instagramUrl
        );

        System.out.println(
                "Twitter         : "
                        + twitterUrl
        );

        System.out.println(
                "Realtor Website : "
                        + realtorWebsite
        );

        System.out.println();

        System.out.println(
                "Office Name     : "
                        + officeName
        );

        System.out.println(
                "Office Type     : "
                        + officeType
        );

        System.out.println(
                "Office Address  : "
                        + officeAddress
        );

        System.out.println(
                "Office Phone 1  : "
                        + officePhone1
        );

        System.out.println(
                "Office Phone 2  : "
                        + officePhone2
        );

        System.out.println(
                "Office Website  : "
                        + officeWebsite
        );

        // ==========================================
        // RETURN 16 CSV VALUES
        // ==========================================

        return new String[] {

                agentName,
                agentTitle,

                agentPhone1,
                agentPhone2,

                facebookUrl,
                linkedinUrl,
                instagramUrl,
                twitterUrl,

                realtorWebsite,

                officeName,
                officeType,
                officeAddress,

                officePhone1,
                officePhone2,

                officeWebsite,

                agentUrl
        };
    }

    // ==========================================
    // CREATE N/A RECORD
    // ==========================================

    private String[] createNARecord(
            String agentUrl
    ) {

        return new String[] {

                "N/A",
                "N/A",

                "N/A",
                "N/A",

                "N/A",
                "N/A",
                "N/A",
                "N/A",

                "N/A",

                "N/A",
                "N/A",
                "N/A",

                "N/A",
                "N/A",

                "N/A",

                agentUrl
        };
    }

    // ==========================================
    // GET TEXT
    // ==========================================

    private String getText(
            WebDriver driver,
            String xpath
    ) {

        try {

            WebElement element =
                    driver.findElement(
                            By.xpath(xpath)
                    );

            String text =
                    element.getText();

            if (text == null
                    || text.trim().isEmpty()) {

                return "N/A";
            }

            return text.trim();

        } catch (Exception e) {

            return "N/A";
        }
    }

    // ==========================================
    // GET HREF
    // ==========================================

    private String getHref(
            WebDriver driver,
            String xpath
    ) {

        try {

            WebElement element =
                    driver.findElement(
                            By.xpath(xpath)
                    );

            String href =
                    element.getAttribute("href");

            if (href == null
                    || href.trim().isEmpty()) {

                return "N/A";
            }

            return href.trim();

        } catch (Exception e) {

            return "N/A";
        }
    }

    // ==========================================
    // GET HREF FROM PARENT ANCHOR
    // ==========================================
    //
    // Used for:
    // Facebook
    // LinkedIn
    // Instagram
    // Realtor Website
    // Office Website
    //
    // Finds the target element and then
    // gets href from its nearest <a> parent.
    // ==========================================

    private String getHrefFromAncestor(
            WebDriver driver,
            String xpath
    ) {

        try {

            WebElement element =
                    driver.findElement(
                            By.xpath(xpath)
                    );

            WebElement anchor =
                    element.findElement(
                            By.xpath(
                                    "./ancestor::a[1]"
                            )
                    );

            String href =
                    anchor.getAttribute("href");

            if (href == null
                    || href.trim().isEmpty()) {

                return "N/A";
            }

            return href.trim();

        } catch (Exception e) {

            return "N/A";
        }
    }

    // ==========================================
    // CREATE CSV LINE
    // ==========================================
    //
    // Handles:
    // commas
    // quotes
    // new lines
    //
    // so CSV columns remain correct.
    // ==========================================

    private String createCsvLine(
            String[] values
    ) {

        StringBuilder line =
                new StringBuilder();

        for (int i = 0;
                i < values.length;
                i++) {

            if (i > 0) {

                line.append(",");
            }

            String value =
                    values[i];

            if (value == null) {

                value = "";
            }

            // ==========================================
            // ESCAPE DOUBLE QUOTES
            // ==========================================

            value =
                    value.replace(
                            "\"",
                            "\"\""
                    );

            // ==========================================
            // SURROUND EVERY VALUE WITH QUOTES
            // ==========================================

            line.append("\"")
                    .append(value)
                    .append("\"");
        }

        return line.toString();
    }
}
