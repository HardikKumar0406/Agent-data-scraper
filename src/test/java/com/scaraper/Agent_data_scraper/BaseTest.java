package com.scaraper.Agent_data_scraper;

import java.time.Duration;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class BaseTest {

    protected WebDriver createDriver() {

        ChromeOptions options = new ChromeOptions();

        /*
         * HEADED MODE
         *
         * Browser window will actually open.
         */
        options.setPageLoadStrategy(
                PageLoadStrategy.EAGER
        );

        options.addArguments(
                "--start-maximized"
        );

        WebDriver driver =
                new ChromeDriver(options);

        driver.manage().timeouts()
                .implicitlyWait(
                        Duration.ofSeconds(0)
                );

        driver.manage().timeouts()
                .pageLoadTimeout(
                        Duration.ofSeconds(30)
                );

        driver.manage().timeouts()
                .scriptTimeout(
                        Duration.ofSeconds(30)
                );

        System.out.println(
                "Chrome browser started."
        );

        return driver;
    }


    protected void closeDriver(
            WebDriver driver) {

        if (driver != null) {

            try {

                driver.quit();

                System.out.println(
                        "Chrome browser closed."
                );

            } catch (Exception e) {

                System.out.println(
                        "Error while closing Chrome."
                );
            }
        }
    }
}