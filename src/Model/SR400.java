package Model;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SR400 {
    private String password;
    private final String cssSelector = "#loginPassword";

    private ArrayList<String> tabs;
    private ArrayList<Integer> tabsToOpen;

    private ChromeDriver driver;


    public SR400(String password, ArrayList<Integer> tabsToOpen){
        this.password = password;
        this.tabsToOpen = tabsToOpen;
    }

    public void runUnits(){
        ChromeOptions options = new ChromeOptions();
        options.setAcceptInsecureCerts(true);
        driver = new ChromeDriver(options);
        refreshUntilConnected();
        openAllTabs();
        pause();
        typePassword();
        pause();
        settingsCheck();
    }

    public void runAndFactoryReset(){
        ChromeOptions options = new ChromeOptions();
        options.setAcceptInsecureCerts(true);
        driver = new ChromeDriver(options);
        refreshUntilConnected();
        openAllTabs();
        pause();
        typePassword();
        pause();
        factoryReset();
        pause();
        typePassword();
        pause();
        settingsCheck();
    }

    private void pause(){
        try{
            Thread.sleep(10000);
        } catch (InterruptedException i){
            i.printStackTrace();
        }
    }

    private void smallPause(){
        try{
            Thread.sleep(500);
        } catch (InterruptedException i){
            i.printStackTrace();
        }
    }

    private void refreshUntilConnected(){
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        driver.get("http://10.0.109.1/");
        boolean displayed = false;
        do{
            try{
                displayed = driver.findElementByCssSelector(cssSelector).isDisplayed();
            } catch(NoSuchElementException e) {
                driver.get("http://10.0.109.1/");
            }
        } while(!displayed);
    }

    private void openAllTabs(){
        driver.manage().window().maximize();
        for(int tab: tabsToOpen) {
            String link = String.format("window.open('http://10.0.109.%d/')", ((tab * 4) - 3));
            driver.executeScript(link);
            smallPause();
        }
        tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(0));
        driver.close();
        tabs = new ArrayList<>(driver.getWindowHandles());
        Collections.reverse(tabs);
    }

    private void typePassword(){
        for(String tab: tabs) {
            driver.switchTo().window(tab);
            boolean displayed = false;
            int tries = 0;
            do{
                try{
                    displayed = driver.findElementByCssSelector(cssSelector).isDisplayed();
                    driver.findElementByCssSelector(cssSelector).sendKeys(password, Keys.ENTER);
                    smallPause();
                } catch(NoSuchElementException e) {
                    driver.get(driver.getCurrentUrl());
                    tries ++;
                }
                if (tries == 2 ){
                    displayed = true;
                }
            } while(!displayed);
        }
    }

    private void settingsCheck(){
        for(int i = 0; i < tabsToOpen.size(); i++) {
            driver.switchTo().window(tabs.get(i));
            driver.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            smallPause();
        }
    }

    private void factoryReset(){
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        int linkNum = 1;
        for(String tab: tabs) {
            try{
                driver.switchTo().window(tab);
                String link = String.format("http://10.0.109.%d/#!/srg-settings-configuration", linkNum);
                driver.get(link);
                driver.findElementByXPath("//*[@id=\"tabs-icons-text\"]/li[3]/a").click();
                smallPause();
                driver.findElementByXPath("//*[@id=\"panel\"]/div/div[2]/div[2]/div/div[2]/ng-transclude/div/srg-card[3]/div/div[2]/div/srg-config-line/div/div[1]/div/button").click();
                smallPause();
                driver.findElementByXPath("//*[@id=\"panel\"]/div/div[2]/div[2]/div/div[2]/ng-transclude/div/div[4]/div/div/div[3]/button[2]").click();
                linkNum += 4;
                smallPause();
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        try{
            Thread.sleep(200000);
        } catch (InterruptedException i){
            i.printStackTrace();
        }
    }
}
