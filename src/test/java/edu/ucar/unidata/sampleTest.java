package edu.ucar.unidata;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class sampleTest {
    private String rosettaUrl = "http://rosetta.unidata.ucar.edu/";

    @Test
    public void testTitle() throws Exception {
        // Create a new instance of the Chrome driver
        // Notice that the remainder of the code relies on the interface, 
        // not the implementation.
        WebDriver driver = new FirefoxDriver();

        // And now use this to visit Rosetta
        //driver.get("http://rosetta.unidata.ucar.edu/");
        // Alternatively the same thing can be done like this
        driver.navigate().to(rosettaUrl);

        // Check the title of the page
        System.out.println("Page title is: " + driver.getTitle());
        
        assertEquals(driver.getTitle().toLowerCase(), "rosetta");
        assertTrue(driver.getTitle().toLowerCase().startsWith("rosetta"));

        //Close the browser
        driver.quit();
    }
}
