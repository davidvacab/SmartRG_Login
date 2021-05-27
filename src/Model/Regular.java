package Model;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class Regular {
    private String username;
    private String password;
    private int HTTPProtocol;

    private ArrayList<String> tabs;
    private ChromeDriver driver;
    private ArrayList<Integer> tabsToOpen;

    public Regular(String username,String password, ArrayList<Integer> tabsToOpen, int HTTPProtocol){
        this.username = username;
        this.password = password;
        this.HTTPProtocol = HTTPProtocol;
        this.tabsToOpen = tabsToOpen;
    }

    public void runUnits(){
        ChromeOptions options = new ChromeOptions();
        options.setAcceptInsecureCerts(true);
        driver = new ChromeDriver(options);
        openTabs();
        pause(3000);
        typePassword();
    }

    private void openTabs(){
        driver.manage().window().maximize();
        String link;
        for(int tab: tabsToOpen) {
            switch (HTTPProtocol){
                case 0:
                    link = String.format("window.open('http://10.0.109.%d/')",((tab * 4) - 3));
                    break;
                case 1:
                    link = String.format("window.open('http://10.0.109.%d/admin/')",((tab * 4) - 3));
                    break;
                case 2:
                    link = String.format("window.open('https://10.0.109.%d/')",((tab * 4) - 3));
                    break;
                case 3:
                    link = String.format("window.open('https://10.0.109.%d/admin/')",((tab * 4) - 3));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + HTTPProtocol);
            }
            driver.executeScript(link);
        }
        tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(0));
        driver.close();
        tabs = new ArrayList<>(driver.getWindowHandles());
        Collections.reverse(tabs);
    }

    private void typePassword(){
        Keyboard keyboard = new Keyboard();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        for(String tab: tabs) {
            driver.switchTo().window(tab);
            keyboard.type(username + '\t');
            pause(500);
            keyboard.type(password);
            pause(100);
            keyboard.type("\n");
        }
    }

    private void pause(int time){
        try{
            Thread.sleep(time);
        } catch (InterruptedException i){
            i.printStackTrace();
        }
    }
}
