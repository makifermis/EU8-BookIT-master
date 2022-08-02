package com.bookit.homework;

import com.bookit.pages.MySelfPage;
import com.bookit.pages.SelfPage;
import com.bookit.pages.SignInPage;
import com.bookit.utilities.*;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.apiguardian.api.API;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StudentInfo {

    @BeforeAll
    public static void init(){
        DBUtils.createConnection();
        baseURI = "https://cybertek-reservation-api-qa2.herokuapp.com";
    }
    @AfterAll
    public static void teardown(){
        DBUtils.destroyConnection();
        Driver.closeDriver();
    }

    @Test
    public void test1(){
        baseURI = "https://cybertek-reservation-api-qa2.herokuapp.com";
        JsonPath userInfoPath = given()
                .contentType(ContentType.JSON)
                .header("Authorization", BookItApiUtil.generateToken("wcanadinea@ihg.com","waverleycanadine"))
                .get("/api/students/me")
                .then().statusCode(200)
                .extract().response().jsonPath();
        int APIid = userInfoPath.getInt("id");
        String APIfirstName = userInfoPath.getString("firstName");
        String APIlastName = userInfoPath.getString("lastName");
        String APIrole = userInfoPath.getString("role");

        System.out.println("APIfirstName = " + APIfirstName);
        System.out.println("APIlastName = " + APIlastName);
        System.out.println("APIrole = " + APIrole);

        JsonPath batchInfoPath = given()
                .contentType(ContentType.JSON)
                .header("Authorization", BookItApiUtil.generateToken("wcanadinea@ihg.com","waverleycanadine"))
                .get("/api/batches/my")
                .then().statusCode(200)
                .extract().jsonPath();
        int APIbatchNumber = batchInfoPath.getInt("number");
        String APIteamName =batchInfoPath.getString("teams[2].name");
        System.out.println("APIbatchNumber = " + APIbatchNumber);
        System.out.println("APIeamName = " + APIteamName);

        JsonPath campusInfoPath = given()
                .contentType(ContentType.JSON)
                .header("Authorization", BookItApiUtil.generateToken("wcanadinea@ihg.com","waverleycanadine"))
                .get("/api/campuses/my")
                .then().statusCode(200)
                .extract().jsonPath();

        String APIcampusLocation = campusInfoPath.getString("location");
        System.out.println("APIcampusLocation = " + APIcampusLocation);

        // GET information from DB
        String queryByName = "select * from users\n" +
                "where firstname = 'Waverley';";
        Map<String, Object> userInfoDB = DBUtils.getRowMap(queryByName);
        String DBfirstName = (String) userInfoDB.get("firstname");
        String DBlastName = (String) userInfoDB.get("lastname");
        long DBId = (long) userInfoDB.get("id");
        System.out.println("DBfirstName = " + DBfirstName);
        System.out.println("DBlastName = " + DBlastName);
        System.out.println("DBId = " + DBId);

        //GET data from UI
        Driver.get().get(Environment.URL);
        Driver.get().manage().window().maximize();
        SignInPage signInPage = new SignInPage();
        signInPage.email.sendKeys("wcanadinea@ihg.com");
        signInPage.password.sendKeys("waverleycanadine");
        BrowserUtils.waitFor(1);
        signInPage.signInButton.click();

        BrowserUtils.waitFor(4);
        SelfPage selfPage = new SelfPage();
        selfPage.goToSelf();

        BrowserUtils.waitFor(4);
        String UI_fullName = selfPage.name.getText();
        String UI_role = selfPage.role.getText();
        String UI_batch = selfPage.batch.getText();
        String UI_campus = selfPage.campus.getText();
        String UI_teamName = selfPage.team.getAttribute("innerText");

        System.out.println("UI_fullName = " + UI_fullName);
        System.out.println("UI_role = " + UI_role);
        System.out.println("UI_batch = " + UI_batch);
        System.out.println("UI_campus = " + UI_campus);
        System.out.println("UI_teamName = " + UI_teamName);

        //Assertions
        assertThat(DBfirstName,equalTo(APIfirstName));
        assertThat(APIrole,equalTo(UI_role));
        assertThat((DBfirstName+" "+ DBlastName),equalTo(UI_fullName));

    }
}
