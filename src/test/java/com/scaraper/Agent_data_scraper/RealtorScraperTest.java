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

        /*
         * ==========================================
         * READ URL FILE
         * ==========================================
         */

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

        /*
         * ==========================================
         * CREATE OUTPUT DIRECTORY
         * ==========================================
         */

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

        /*
         * ==========================================
         * CHECK CSV FILE
         * ==========================================
         *
         * Header will only be written if:
         *
         * 1. File does not exist
         * OR
         * 2. File exists but is empty
         *
         * Existing records will NEVER be overwritten.
         */

        File csvFile = new File(CSV_FILE);

        boolean writeHeader =
                !csvFile.exists()
                        || csvFile.length() == 0;

        /*
         * ==========================================
         * OPEN CSV IN APPEND MODE
         * ==========================================
         *
         * true = APPEND
         *
         * This is important.
         *
         * FileWriter(CSV_FILE)
         * would overwrite the existing CSV.
         *
         * FileWriter(CSV_FILE, true)
         * appends new records.
         */

        try (
                BufferedWriter writer =
                        new BufferedWriter(
                                new FileWriter(
                                        CSV_FILE,
                                        true
                                )
                        )
        ) {

            /*
             * ==========================================
             * CSV HEADERS
             * ==========================================
             */

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

            /*
             * ==========================================
             * PROCESS EVERY URL
             * ==========================================
             */

            for (String agentUrl : agentUrls) {

                agentUrl = agentUrl.trim();

                /*
                 * Skip empty lines
                 */

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

                    /*
                     * ==========================================
                     * CREATE NEW CHROME FOR EVERY URL
                     * ==========================================
                     */

                    driver = createDriver();

                    /*
                     * ==========================================
                     * OPEN AGENT URL
                     * ==========================================
                     */

                    try {

                        driver.get(agentUrl);

                    } catch (Exception e) {

                        System.out.println(
                                "Page load timeout. Continuing..."
                        );
                    }

                    System.out.println(
                            "Current URL: "
                                    + driver.getCurrentUrl()
                    );

                    System.out.println(
                            "Page Title: "
                                    + driver.getTitle()
                    );

                    /*
                     * ==========================================
                     * WAIT FOR AGENT INFORMATION
                     * ==========================================
                     */

                    WebDriverWait wait =
                            new WebDriverWait(
                                    driver,
                                    Duration.ofSeconds(30)
                            );

                    System.out.println();
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
                                "Agent page loaded."
                        );

                    } catch (Exception e) {

                        System.out.println(
                                "Agent name locator not found."
                        );

                        System.out.println(
                                "Continuing with available information..."
                        );
                    }

                    /*
                     * ==========================================
                     * PAGE DIAGNOSTIC
                     * ==========================================
                     */

                    System.out.println();
                    System.out.println(
                            "=========================================="
                    );

                    System.out.println(
                            "PAGE DIAGNOSTIC"
                    );

                    System.out.println(
                            "=========================================="
                    );

                    System.out.println(
                            "Agent Name elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//span[@class='realtorCardName'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "Title elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//div[@class='realtorCardTitle'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "Agent Phone 1 elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//span[@class='realtorCardContactNumber TelephoneNumber'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "Agent Phone 2 elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//span[@class='realtorCardContactNumber TollFreeNumber'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "Realtor Website elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//span[@class='realtorCardContactNumber'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "Office Name elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//div[@class='officeCardName'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "Office Type elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//div[@class='officeCardType'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "Office Address elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//div[@class='officeCardAddress'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "Office Phone elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//span[@class='officeCardContactNumber'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "Office Fax elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//div[@data-type='Fax']//span[@class='officeCardContactNumber'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "Office Telephone elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//div[@data-type='Telephone']//span[@class='officeCardContactNumber'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "Office Website elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//span[normalize-space()='Office Website'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "Facebook elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//img[@src='https://static.realtor.ca/images/common/icons/svg/facebook.svg'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "LinkedIn elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//img[@src='https://static.realtor.ca/images/common/icons/svg/linkedin.svg'])[1]"
                                            )
                                    ).size()
                    );

                    System.out.println(
                            "Instagram elements: "
                                    + driver.findElements(
                                            By.xpath(
                                                    "(//img[@src='https://static.realtor.ca/images/common/icons/svg/instagram.svg'])[1]"
                                            )
                                    ).size()
                    );

                    /*
                     * ==========================================
                     * AGENT DETAILS
                     * ==========================================
                     */

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

                    /*
                     * Agent Phone 1
                     */

                    String agentPhone1 =
                            getText(
                                    driver,
                                    "(//span[@class='realtorCardContactNumber TelephoneNumber'])[1]"
                            );

                    /*
                     * Agent Phone 2
                     */

                    String agentPhone2 =
                            getText(
                                    driver,
                                    "(//span[@class='realtorCardContactNumber TollFreeNumber'])[1]"
                            );

                    /*
                     * ==========================================
                     * SOCIAL MEDIA
                     * ==========================================
                     */

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

                    /*
                     * ==========================================
                     * REALTOR WEBSITE
                     * ==========================================
                     */

                    String realtorWebsite =
                            getText(
                                    driver,
                                    "(//span[@class='realtorCardContactNumber'])[1]"
                            );

                    /*
                     * ==========================================
                     * OFFICE DETAILS
                     * ==========================================
                     */

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

                    /*
                     * Office Phone 1
                     */

                    String officePhone1 =
                            getText(
                                    driver,
                                    "(//span[@class='officeCardContactNumber'])[1]"
                            );

                    /*
                     * Office Phone 2
                     */

                    String officePhone2 =
                            getText(
                                    driver,
                                    "(//span[@class='officeCardContactNumber'])[2]"
                            );

                    /*
                     * Office Fax
                     */

                    String officeFax =
                            getText(
                                    driver,
                                    "(//div[@data-type='Fax']//span[@class='officeCardContactNumber'])[1]"
                            );

                    /*
                     * Office Telephone
                     */

                    String officeTelephone =
                            getText(
                                    driver,
                                    "(//div[@data-type='Telephone']//span[@class='officeCardContactNumber'])[1]"
                            );

                    /*
                     * Office Website
                     */

                    String officeWebsite =
                            getText(
                                    driver,
                                    "(//span[normalize-space()='Office Website'])[1]"
                            );

                    /*
                     * ==========================================
                     * PRINT SCRAPED DATA
                     * ==========================================
                     */

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

                    /*
                     * ==========================================
                     * WRITE DATA TO CSV
                     * ==========================================
                     */

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

                    /*
                     * Because the BufferedWriter was opened
                     * in APPEND mode, this record is added
                     * to the existing CSV.
                     */

                    writer.write(
                            createCsvLine(data)
                    );

                    writer.newLine();

                    /*
                     * Flush after every record.
                     *
                     * This ensures the record is physically
                     * written even if the scraper stops later.
                     */

                    writer.flush();

                    successfulRecords++;

                    System.out.println();
                    System.out.println(
                            "Data appended to CSV."
                    );

                } catch (Exception e) {

                    /*
                     * ==========================================
                     * URL FAILURE
                     * ==========================================
                     *
                     * If one URL fails,
                     * continue with next URL.
                     */

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

                    /*
                     * ==========================================
                     * ALWAYS CLOSE CHROME
                     * ==========================================
                     */

                    closeDriver(driver);
                }
            }

            /*
             * ==========================================
             * COMPLETED
             * ==========================================
             */

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
                    "Total URLs     : " + agentUrls.size()
            );

            System.out.println(
                    "Records saved  : " + successfulRecords
            );

            System.out.println(
                    "CSV file       : " + CSV_FILE
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

    /*
     * ==========================================
     * GET TEXT
     * ==========================================
     */

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

    /*
     * ==========================================
     * GET HREF
     * ==========================================
     */

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

    /*
     * ==========================================
     * GET HREF FROM PARENT ANCHOR
     * ==========================================
     *
     * Used for:
     *
     * Facebook
     * LinkedIn
     * Instagram
     */

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

    /*
     * ==========================================
     * CREATE CSV LINE
     * ==========================================
     *
     * Handles:
     *
     * commas
     * quotes
     * new lines
     *
     * so CSV columns remain correct.
     */

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

            /*
             * Escape double quotes.
             */

            value =
                    value.replace(
                            "\"",
                            "\"\""
                    );

            /*
             * Surround every value
             * with double quotes.
             */

            line.append("\"")
                    .append(value)
                    .append("\"");
        }

        return line.toString();
    }
}
