package com.example.meetings.endToEnd;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

// http://localhost:8080/h2-console
// DELETE FROM USERS;
// DELETE FROM MEETING_PARTICIPANTS;
// DELETE FROM MEETINGS;
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class SeleniumTest {
    private WebDriver driver;

    @BeforeEach
    void setup() {
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void createMeeting() {
        String username = "user" + System.currentTimeMillis();

        driver.get("http://localhost:8080/register");
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("email")).sendKeys(username + "@gmail.com");
        driver.findElement(By.id("password")).sendKeys("123");
        driver.findElement(By.tagName("button")).click();

        driver.get("http://localhost:8080/login");
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys("123");
        driver.findElement(By.tagName("button")).click();
        System.out.println(driver.getCurrentUrl());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("/calendar"));

        assertTrue(driver.getCurrentUrl().contains("/calendar"));

        driver.get("http://localhost:8080/meetings/new");
        driver.findElement(By.id("title")).sendKeys("Meeting");
        driver.findElement(By.id("description")).sendKeys("Description");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.getElementById('start').value='2027-01-01T10:00'");
        js.executeScript("document.getElementById('end').value='2027-01-01T11:00'");
        driver.findElement(By.xpath("//button[text()='Propose']")).click();
        System.out.println(driver.getCurrentUrl());
        WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait2.until(ExpectedConditions.urlContains("/calendar"));
        assertTrue(driver.getCurrentUrl().contains("/calendar"));
    }

}
