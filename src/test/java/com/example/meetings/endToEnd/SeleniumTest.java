package com.example.meetings.endToEnd;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.meetings.repository.UserRepository;
import com.example.meetings.model.User;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

// http://localhost:8080/h2-console
// DELETE FROM USERS;
// DELETE FROM MEETING_PARTICIPANTS;
// DELETE FROM MEETINGS;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) 
class SeleniumTest {
    private WebDriver driver;

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_USERNAME = "tester";
    private static final String TEST_PWD = "123";

    private static final String TEST_USERNAME2 = "tester2";
    private static final String TEST_PWD2 = "123";

    @BeforeEach
    void setup() {
        if (userRepository.findByUsername(TEST_USERNAME).isEmpty()) {
            User u = new User(TEST_USERNAME, "test@gamil.com", passwordEncoder.encode(TEST_PWD));
            userRepository.save(u);
        }

        if (userRepository.findByUsername(TEST_USERNAME2).isEmpty()) {
            User u2 = new User(TEST_USERNAME2, "test2@gamil.com", passwordEncoder.encode(TEST_PWD2));
            userRepository.save(u2);
        }

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
    void login() {
        String baseUrl = "http://localhost:" + port;

        driver.get(baseUrl + "/login");

        driver.findElement(By.name("username")).sendKeys(TEST_USERNAME);
        driver.findElement(By.name("password")).sendKeys(TEST_PWD);

        driver.findElement(By.tagName("button")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("/calendar"));
    }

    @Test
    void createMeeting() {
        String baseUrl = "http://localhost:" + port;
        String username = "user" + System.currentTimeMillis();

        driver.get(baseUrl + "/register");
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("email")).sendKeys(username + "@gmail.com");
        driver.findElement(By.id("password")).sendKeys("123");
        driver.findElement(By.tagName("button")).click();

        driver.get(baseUrl + "/login");
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys("123");
        driver.findElement(By.tagName("button")).click();
        //System.out.println(driver.getCurrentUrl());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("/calendar"));

        assertTrue(driver.getCurrentUrl().contains("/calendar"));

        driver.get(baseUrl + "/meetings/new");
        driver.findElement(By.id("title")).sendKeys("Meeting");
        driver.findElement(By.id("description")).sendKeys("Description");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.getElementById('start').value='2027-01-01T10:00'");
        js.executeScript("document.getElementById('end').value='2027-01-01T11:00'");
        driver.findElement(By.xpath("//button[text()='Propose']")).click();
        //System.out.println(driver.getCurrentUrl());
        wait.until(ExpectedConditions.urlContains("/calendar"));
        assertTrue(driver.getCurrentUrl().contains("/calendar"));
    }

    @Test
    void inviteAndAccept() {
        String baseUrl = "http://localhost:" + port;

        driver.get(baseUrl + "/login");
        driver.findElement(By.name("username")).sendKeys(TEST_USERNAME);
        driver.findElement(By.name("password")).sendKeys(TEST_PWD);
        driver.findElement(By.tagName("button")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("/calendar"));

        driver.get(baseUrl + "/meetings/new");

        driver.findElement(By.id("title")).sendKeys("Test Meeting");
        driver.findElement(By.id("description")).sendKeys("Description");
        LocalDateTime start = LocalDateTime.now().plusMinutes(10);
        LocalDateTime end = start.plusHours(1);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String startStr = start.format(fmt);
        String endStr = end.format(fmt);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.getElementById('start').value='" + startStr + "'");
        js.executeScript("document.getElementById('end').value='" + endStr + "'");
        driver.findElement(By.id("invitees")).sendKeys(TEST_USERNAME2);
        driver.findElement(By.xpath("//button[text()='Propose']")).click();
        wait.until(ExpectedConditions.urlContains("/calendar"));

        driver.findElement(By.xpath("//button[text()='Sign out']")).click();

        driver.get(baseUrl + "/login");
        driver.findElement(By.name("username")).sendKeys(TEST_USERNAME2);
        driver.findElement(By.name("password")).sendKeys(TEST_PWD2);
        driver.findElement(By.tagName("button")).click();
        wait.until(ExpectedConditions.urlContains("/calendar"));

        driver.findElement(By.xpath("//button[text()='Accept']")).click();
        wait.until(ExpectedConditions.urlContains("/calendar"));
        assertTrue(driver.getPageSource().contains("Test Meeting"));
    }
}
