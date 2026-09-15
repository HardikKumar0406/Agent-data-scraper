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
import org.openqa.selenium.support.ui.ExpectedConditions;
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
        // CREATE FRESH CSV
        // ==========================================

        File csvFile = new File(CSV_FILE);

        try {

            // Delete old CSV before every run
            if (csvFile.exists()) {

                if (csvFile.delete()) {

                    System.out.println(
                            "Old CSV deleted."
                    );

                } else {

                    System.out.println(
                            "Unable to delete old CSV."
                    );

                    return;
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Error while deleting old CSV."
            );

            e.printStackTrace();

            return;
        }

        // ==========================================
        // OPEN CSV
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
                    "New CSV created."
            );

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
                            "=========================================="
                    );

                    System.out.println(
                            "Opening URL: "
                                    + agentUrl
                    );

                    // ==========================================
                    // CREATE NEW CHROME FOR EVERY URL
                    // ==========================================

                    driver = createDriver();

                    // ==========================================
                    // OPEN AGENT URL
                    // ==========================================

                    try {

                        driver.get(agentUrl);

                    } catch (Exception e) {

                        System.out.println(
                                "Page load timeout. Continuing..."
                        );
                    }

                    // ==========================================
                    // WAIT FOR PAGE TO RENDER
                    // ==========================================

                    WebDriverWait wait =
                            new WebDriverWait(
                                    driver,
                                    Duration.ofSeconds(30)
                            );

                    System.out.println(
                            "Waiting for agent information..."
                    );

                    try {

                        wait.until(
                                ExpectedConditions
                                        .presenceOfElementLocated(
                                                By.xpath(
                                                        "(//span[@class='realtorCardName'])[1]"
                                                )
                                        )
                        );

                        System.out.println(
                                "Agent element found."
                        );

                    } catch (Exception e) {

                        System.out.println(
                                "Agent name element not found after 30 seconds."
                        );
                    }

                    // ==========================================
                    // EXTRA RENDER WAIT
                    // ==========================================

                    sleep(2000);

                    // ==========================================
                    // SCRAPE DATA
                    // ==========================================

                    String[] scrapedData =
                            scrapeAgentData(driver);

                    // ==========================================
                    // RETRY IF MOST DATA IS N/A
                    // ==========================================

                    int naCount =
                            countNA(scrapedData);

                    if (naCount >= 10) {

                        System.out.println(
                                "Most fields are N/A."
                        );

                        System.out.println(
                                "Waiting and retrying page data..."
                        );

                        sleep(3000);

                        scrapedData =
                                scrapeAgentData(driver);
                    }

                    // ==========================================
                    // ASSIGN DATA
                    // ==========================================

                    String agentName = scrapedData[0];
                    String agentTitle = scrapedData[1];
                    String agentPhone1 = scrapedData[2];
                    String agentPhone2 = scrapedData[3];
                    String facebookUrl = scrapedData[4];
                    String linkedinUrl = scrapedData[5];
                    String instagramUrl = scrapedData[6];
                    String twitterUrl = scrapedData[7];
                    String realtorWebsite = scrapedData[8];
                    String officeName = scrapedData[9];
                    String officeType = scrapedData[10];
                    String officeAddress = scrapedData[11];
                    String officePhone1 = scrapedData[12];
                    String officePhone2 = scrapedData[13];
                    String officeFax = scrapedData[14];
                    String officeTelephone = scrapedData[15];
                    String officeWebsite = scrapedData[16];

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
                            "Office Fax      : "
                                    + officeFax
                    );

                    System.out.println(
                            "Office Telephone: "
                                    + officeTelephone
                    );

                    System.out.println(
                            "Office Website  : "
                                    + officeWebsite
                    );

                    // ==========================================
                    // WRITE DATA TO CSV
                    // ==========================================

                    String[] data = {

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
                            officeWebsite,
                            agentUrl
                    };

                    writer.write(
                            createCsvLine(data)
                    );

                    writer.newLine();

                    // Flush after every record
                    writer.flush();

                    successfulRecords++;

                    System.out.println();
                    System.out.println(
                            "Data written to CSV."
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
                            "URL: " + agentUrl
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
    // SCRAPE ALL AGENT DATA
    // ==========================================

    private String[] scrapeAgentData(
            WebDriver driver
    ) {

        String[] data = new String[17];

        // Agent Name
        data[0] =
                getTextWithWait(
                        driver,
                        "(//span[@class='realtorCardName'])[1]"
                );

        // Title
        data[1] =
                getTextWithWait(
                        driver,
                        "(//div[@class='realtorCardTitle'])[1]"
                );

        // Agent Phone 1
        data[2] =
                getTextWithWait(
                        driver,
                        "(//span[@class='realtorCardContactNumber TelephoneNumber'])[1]"
                );

        // Agent Phone 2
        data[3] =
                getTextWithWait(
                        driver,
                        "(//span[@class='realtorCardContactNumber TollFreeNumber'])[1]"
                );

        // Facebook
        data[4] =
                getHrefFromAncestor(
                        driver,
                        "(//img[@src='https://static.realtor.ca/images/common/icons/svg/facebook.svg'])[1]"
                );

        // LinkedIn
        data[5] =
                getHrefFromAncestor(
                        driver,
                        "(//img[@src='https://static.realtor.ca/images/common/icons/svg/linkedin.svg'])[1]"
                );

        // Instagram
        data[6] =
                getHrefFromAncestor(
                        driver,
                        "(//img[@src='https://static.realtor.ca/images/common/icons/svg/instagram.svg'])[1]"
                );

        // Twitter
        data[7] =
                getHref(
                        driver,
                        "(//a[@aria-label='Twitter Link'])[1]"
                );

        // Realtor Website
        data[8] =
                getHrefFromAncestor(
                        driver,
                        "(//span[@class='realtorCardContactNumber'])[1]"
                );

        // Office Name
        data[9] =
                getTextWithWait(
                        driver,
                        "(//div[@class='officeCardName'])[1]"
                );

        // Office Type
        data[10] =
                getTextWithWait(
                        driver,
                        "(//div[@class='officeCardType'])[1]"
                );

        // Office Address
        data[11] =
                getTextWithWait(
                        driver,
                        "(//div[@class='officeCardAddress'])[1]"
                );

        // Office Phone 1
        data[12] =
                getTextWithWait(
                        driver,
                        "(//span[@class='officeCardContactNumber'])[1]"
                );

        // Office Phone 2
        data[13] =
                getTextWithWait(
                        driver,
                        "(//span[@class='officeCardContactNumber'])[2]"
                );

        // Office Fax
        data[14] =
                getTextWithWait(
                        driver,
                        "(//div[@data-type='Fax']//span[@class='officeCardContactNumber'])[1]"
                );

        // Office Telephone
        data[15] =
                getTextWithWait(
                        driver,
                        "(//div[@data-type='Telephone']//span[@class='officeCardContactNumber'])[1]"
                );

        // Office Website
        data[16] =
                getHrefFromAncestor(
                        driver,
                        "(//span[normalize-space()='Office Website'])[1]"
                );

        return data;
    }

    // ==========================================
    // GET TEXT WITH WAIT
    // ==========================================

    private String getTextWithWait(
            WebDriver driver,
            String xpath
    ) {

        try {

            WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(10)
                    );

            WebElement element =
                    wait.until(
                            ExpectedConditions
                                    .presenceOfElementLocated(
                                            By.xpath(xpath)
                                    )
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

            WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(10)
                    );

            WebElement element =
                    wait.until(
                            ExpectedConditions
                                    .presenceOfElementLocated(
                                            By.xpath(xpath)
                                    )
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

            WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(10)
                    );

            WebElement element =
                    wait.until(
                            ExpectedConditions
                                    .presenceOfElementLocated(
                                            By.xpath(xpath)
                                    )
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
    // COUNT N/A
    // ==========================================

    private int countNA(
            String[] data
    ) {

        int count = 0;

        for (String value : data) {

            if (value == null
                    || value.equals("N/A")
                    || value.trim().isEmpty()) {

                count++;
            }
        }

        return count;
    }

    // ==========================================
    // SLEEP
    // ==========================================

    private void sleep(
            long milliseconds
    ) {

        try {

            Thread.sleep(milliseconds);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
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

        for (int i = 0; i < values.length; i++) {

            if (i > 0) {
                line.append(",");
            }

            String value = values[i];

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
