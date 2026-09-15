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

    /*
     * Maximum time we wait for agent data on one browser attempt.
     */
    private static final int AGENT_WAIT_SECONDS = 5;

    /*
     * Maximum time allowed for page navigation.
     */
    private static final int PAGE_LOAD_SECONDS = 5;

    /*
     * Retry the URL one additional time if agent data
     * is not found on the first browser.
     */
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
        // CHECK CSV FILE
        // ==========================================

        File csvFile =
                new File(CSV_FILE);

        boolean writeHeader =
                !csvFile.exists()
                        || csvFile.length() == 0;

        // ==========================================
        // OPEN CSV IN APPEND MODE
        // ==========================================

        try (
                BufferedWriter writer =
                        new BufferedWriter(
                                new FileWriter(
                                        CSV_FILE,
                                        true
                                )
                        )
        ) {

            // ==========================================
            // CSV HEADERS
            // ==========================================

            if (writeHeader) {

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
                        "Office Fax",
                        "Office Telephone",
                        "Office Website",
                        "Agent URL"
                };

                writer.write(
                        createCsvLine(headers)
                );

                writer.newLine();
                writer.flush();

                System.out.println(
                        "CSV header created."
                );

            } else {

                System.out.println(
                        "Existing CSV found. Appending new records."
                );
            }

            int agentNumber = 0;
            int successfulRecords = 0;

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
                        "=========================================="
                );

                System.out.println(
                        "URL: " + agentUrl
                );

                boolean dataSaved = false;

                // ==========================================
                // MAX 2 ATTEMPTS
                // ==========================================

                for (int attempt = 1;
                     attempt <= MAX_ATTEMPTS && !dataSaved;
                     attempt++) {

                    WebDriver driver = null;

                    long startTime =
                            System.currentTimeMillis();

                    try {

                        System.out.println();
                        System.out.println(
                                "Browser attempt "
                                        + attempt
                                        + " of "
                                        + MAX_ATTEMPTS
                        );

                        // ==========================================
                        // CREATE FRESH CHROME
                        // ==========================================

                        driver = createDriver();

                        /*
                         * Override the 30-second timeout from BaseTest
                         * for this scraper.
                         */
                        driver.manage()
                                .timeouts()
                                .pageLoadTimeout(
                                        Duration.ofSeconds(
                                                PAGE_LOAD_SECONDS
                                        )
                                );

                        // ==========================================
                        // OPEN URL
                        // ==========================================

                        try {

                            driver.get(agentUrl);

                        } catch (Exception e) {

                            /*
                             * A 5-second page-load timeout does not
                             * necessarily mean the page is useless.
                             * Selenium may already have loaded enough
                             * DOM content for us to scrape.
                             */
                            System.out.println(
                                    "Page navigation reached timeout. Checking page data..."
                            );
                        }

                        // ==========================================
                        // WAIT FOR AGENT DATA
                        // ==========================================

                        boolean agentDataFound =
                                waitForAgentData(
                                        driver
                                );

                        if (!agentDataFound) {

                            System.out.println(
                                    "Agent data not found within "
                                            + AGENT_WAIT_SECONDS
                                            + " seconds."
                            );

                            System.out.println(
                                    "Closing browser and retrying..."
                            );

                            continue;
                        }

                        System.out.println(
                                "Agent data found. Scraping immediately..."
                        );

                        // ==========================================
                        // SCRAPE DATA
                        // ==========================================

                        String[] scrapedData =
                                scrapeAgentData(
                                        driver
                                );

                        // ==========================================
                        // CHECK WHETHER DATA WAS ACTUALLY FOUND
                        // ==========================================

                        int naCount =
                                countNA(scrapedData);

                        System.out.println(
                                "N/A fields: " + naCount
                        );

                        /*
                         * If almost everything is N/A, consider this
                         * attempt unsuccessful and retry once.
                         */
                        if (naCount >= 10
                                && attempt < MAX_ATTEMPTS) {

                            System.out.println(
                                    "Too much missing data. "
                                            + "Retrying with a fresh browser..."
                            );

                            continue;
                        }

                        // ==========================================
                        // PRINT SCRAPED DATA
                        // ==========================================

                        printScrapedData(
                                scrapedData
                        );

                        // ==========================================
                        // ADD AGENT URL
                        // ==========================================

                        String[] data =
                                new String[18];

                        System.arraycopy(
                                scrapedData,
                                0,
                                data,
                                0,
                                scrapedData.length
                        );

                        /*
                         * scrapedData contains 17 fields.
                         * Agent URL becomes field 18.
                         */
                        data[17] = agentUrl;

                        // ==========================================
                        // WRITE TO CSV
                        // ==========================================

                        writer.write(
                                createCsvLine(data)
                        );

                        writer.newLine();

                        /*
                         * Flush after every record so that data
                         * remains saved even if the next URL fails.
                         */
                        writer.flush();

                        successfulRecords++;
                        dataSaved = true;

                        long endTime =
                                System.currentTimeMillis();

                        long seconds =
                                (endTime - startTime) / 1000;

                        System.out.println();
                        System.out.println(
                                "Data saved successfully."
                        );

                        System.out.println(
                                "Processing time: "
                                        + seconds
                                        + " seconds"
                        );

                    } catch (Exception e) {

                        System.out.println();
                        System.out.println(
                                "Error during browser attempt "
                                        + attempt
                        );

                        System.out.println(
                                "URL: " + agentUrl
                        );

                        e.printStackTrace();

                    } finally {

                        // ==========================================
                        // ALWAYS CLOSE THIS BROWSER
                        // ==========================================

                        closeDriver(driver);
                    }
                }

                // ==========================================
                // IF BOTH ATTEMPTS FAILED
                // ==========================================

                if (!dataSaved) {

                    System.out.println();
                    System.out.println(
                            "Both attempts failed."
                    );

                    System.out.println(
                            "Saving N/A record and continuing..."
                    );

                    String[] failedData =
                            createNARecord(
                                    agentUrl
                            );

                    writer.write(
                            createCsvLine(failedData)
                    );

                    writer.newLine();
                    writer.flush();
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
                    "Total URLs    : "
                            + agentUrls.size()
            );

            System.out.println(
                    "Records saved : "
                            + successfulRecords
            );

            System.out.println(
                    "CSV file      : "
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

            wait.until(
                    d -> {

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
                    }
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // ==========================================
    // SCRAPE AGENT DATA
    // ==========================================

    private String[] scrapeAgentData(
            WebDriver driver
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
        // Grab href behind the span
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
        // ==========================================

        String officePhone1 =
                getText(
                        driver,
                        "(//span[@class='officeCardContactNumber'])[1]"
                );

        // ==========================================
        // OFFICE PHONE 2
        // ==========================================

        String officePhone2 =
                getText(
                        driver,
                        "(//span[@class='officeCardContactNumber'])[2]"
                );

        // ==========================================
        // OFFICE FAX
        // ==========================================

        String officeFax =
                getText(
                        driver,
                        "(//div[@data-type='Fax']//span[@class='officeCardContactNumber'])[1]"
                );

        // ==========================================
        // OFFICE TELEPHONE
        // ==========================================

        String officeTelephone =
                getText(
                        driver,
                        "(//div[@data-type='Telephone']//span[@class='officeCardContactNumber'])[1]"
                );

        // ==========================================
        // OFFICE WEBSITE
        // Grab href behind "Office Website"
        // ==========================================

        String officeWebsite =
                getHrefFromAncestor(
                        driver,
                        "(//span[normalize-space()='Office Website'])[1]"
                );

        // ==========================================
        // RETURN DATA
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
                officeFax,
                officeTelephone,
                officeWebsite
        };
    }

    // ==========================================
    // PRINT SCRAPED DATA
    // ==========================================

    private void printScrapedData(
            String[] data
    ) {

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
                "Agent Name      : " + data[0]
        );

        System.out.println(
                "Title           : " + data[1]
        );

        System.out.println(
                "Agent Phone 1   : " + data[2]
        );

        System.out.println(
                "Agent Phone 2   : " + data[3]
        );

        System.out.println(
                "Facebook        : " + data[4]
        );

        System.out.println(
                "LinkedIn        : " + data[5]
        );

        System.out.println(
                "Instagram       : " + data[6]
        );

        System.out.println(
                "Twitter         : " + data[7]
        );

        System.out.println(
                "Realtor Website : " + data[8]
        );

        System.out.println();

        System.out.println(
                "Office Name     : " + data[9]
        );

        System.out.println(
                "Office Type     : " + data[10]
        );

        System.out.println(
                "Office Address  : " + data[11]
        );

        System.out.println(
                "Office Phone 1  : " + data[12]
        );

        System.out.println(
                "Office Phone 2  : " + data[13]
        );

        System.out.println(
                "Office Fax      : " + data[14]
        );

        System.out.println(
                "Office Telephone: " + data[15]
        );

        System.out.println(
                "Office Website  : " + data[16]
        );
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
                "N/A",
                "N/A",
                agentUrl
        };
    }

    // ==========================================
    // COUNT N/A
    // ==========================================

    private int countNA(
            String[] data
    ) {

        int count = 0;

        for (String value : data) {

            if (value == null
                    || value.trim().isEmpty()
                    || value.equals("N/A")) {

                count++;
            }
        }

        return count;
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
    // GET HREF FROM ANCESTOR
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

            // Escape double quotes
            value =
                    value.replace(
                            "\"",
                            "\"\""
                    );

            // Surround every value with quotes
            line.append("\"")
                    .append(value)
                    .append("\"");
        }

        return line.toString();
    }
}
