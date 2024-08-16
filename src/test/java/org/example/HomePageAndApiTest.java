package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.RequestOptions;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomePageAndApiTest {
    private Playwright playwright;
    private Browser browser;
    private Page page;
    private APIRequestContext req;
    List<Locator> LocatorList = new ArrayList<>();

    @BeforeTest
    public void setUp() {
        // Initialize Playwright and launch the browser
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
        // Set up the API Request context if needed
        APIRequest apiRequest = playwright.request();
        req = apiRequest.newContext();
    }

    @Test(priority = 1)
    public void TestAiralo() {

        page.navigate("https://www.airalo.com/");
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        //There should be more error handling added below to make test more robust
        LocatorList.add(page.locator(HomePageAndOrderDetailPageIds.acceptCookiesButton));
        LocatorList.add(page.locator(HomePageAndOrderDetailPageIds.cancelNotificationButton));
        LocatorList.add(page.getByTestId(HomePageAndOrderDetailPageIds.closeButton));
        HandlePopups(LocatorList);
        page.getByTestId(HomePageAndOrderDetailPageIds.searchInput).click();
        page.getByTestId(HomePageAndOrderDetailPageIds.searchInput).fill("Japan");
        page.getByTestId(HomePageAndOrderDetailPageIds.japanOption).click();
        page.locator(HomePageAndOrderDetailPageIds.buyNowButton).click();
        performPageAssertions("Moshi Moshi","Japan","1 GB","7 Days","$4.50 USD");

    }

    @Test(priority = 2)
    public void ApiTestPostOrder() throws IOException {
        //Hashmap to feed Data to Post Request
        Map<String, Object> data = new HashMap<>();
        data.put("quantity", 6);
        data.put("package_id", "merhaba-7days-1gb");
        data.put("type", "sim");
        data.put("description", "Example description to identify the order");
        //Post request with Hashmap Data and Headers
        APIResponse postApiResponse = req.post("https://sandbox-partners-api.airalo.com/v2/orders",
                RequestOptions.create()
                        .setHeader("Accept", "application/json")
                        .setHeader("Authorization", "Bearer " + TestData.AuthToken)
                        .setData(data));
        //Assertion for Response from Order Creation Post request
        System.out.println(postApiResponse.status());
        Assert.assertEquals(postApiResponse.status(), 200);

        //Using Jackson Api to get the response body from the request above and converting into JsonNode Object
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode response = objectMapper.readTree(postApiResponse.body());
        int iccidTotalNumberOfOrders = 0;
        //Getting data as a Data Array
        JsonNode dataArray = response.get("data");
        String packageId = dataArray.get("package_id").asText();
        Assert.assertEquals(packageId, "merhaba-7days-1gb");
        int quantity = dataArray.get("quantity").asInt();
        Assert.assertEquals(quantity, 6);
        int validity = dataArray.get("validity").asInt();
        Assert.assertEquals(validity, 7);
        String dataAmount = dataArray.get("data").asText();
        Assert.assertEquals(dataAmount, "1 GB");
        JsonNode simsArray = dataArray.get("sims");
        // Navigate to "sims" object
        for (JsonNode dataNode : simsArray) {
            //Get Iccid to identify number of orders as all orders have unique iccid according to api documentation
            String iccid = dataNode.get("iccid").asText();
            if (iccid != null) {
                iccidTotalNumberOfOrders++;
            }
        }
        Assert.assertEquals(iccidTotalNumberOfOrders, 6);
        System.out.println("There are " + iccidTotalNumberOfOrders + " total orders based on iccids in POST ORDER Request");
        postApiResponse.dispose();

    }

    @Test(priority = 3)
    public void ApiTestGetSims() throws IOException {
        //Sending Get Request to get Esims
        APIResponse getApiResponse = req.get("https://sandbox-partners-api.airalo.com/v2/sims?include=order%2Corder.status%2Corder.user&limit=6",
                RequestOptions.create()
                        .setHeader("Accept", "application/json")
                        .setHeader("Authorization", "Bearer " + TestData.AuthToken));
        //Asserting Status Code
        System.out.println(getApiResponse.status());
        Assert.assertEquals(getApiResponse.status(), 200);
        //Using Jackson Api to get the response body from the request above and converting into JsonNode Object
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode response = objectMapper.readTree(getApiResponse.body());
        int countPakageIdWithEsimTypePrepaid = 0;
        int iccidTotalNumberOfOrders = 0;
        //Getting data as a Data Array
        JsonNode dataArray = response.get("data");
        for (JsonNode dataNode : dataArray) {   //Get Iccid to identify number of orders as all orders have unique iccid according to api documentation
            String iccid = dataNode.get("iccid").asText();
            if (iccid != null) {
                iccidTotalNumberOfOrders++;
            }
            // Navigate to "simable" object within each data node
            JsonNode simableNode = dataNode.get("simable");
            //Assertions for basic order Details
            int quantity = simableNode.get("quantity").asInt();
            Assert.assertEquals(quantity, 6);
            int validity = simableNode.get("validity").asInt();
            Assert.assertEquals(validity, 7);
            String dataAmount = simableNode.get("data").asText();
            Assert.assertEquals(dataAmount, "1 GB");
            String packageId = simableNode.get("package_id").asText();
            String esimType = simableNode.get("esim_type").asText();
            if ("merhaba-7days-1gb".equals(packageId) && esimType.equals("Prepaid") && iccid != null) {
                countPakageIdWithEsimTypePrepaid++;
            }
        }
        Assert.assertEquals(iccidTotalNumberOfOrders, 6);
        Assert.assertEquals(countPakageIdWithEsimTypePrepaid, 6);
        System.out.println("There are " + iccidTotalNumberOfOrders + " total orders based on iccids");
        System.out.println("All " + countPakageIdWithEsimTypePrepaid + " orders are Prepaid type Esims which have merhaba-7days-1gb package");
        getApiResponse.dispose();
    }

    public void performPageAssertions(String title, String country, String volume,String timePeriod,String price ) {
        Assert.assertEquals(page.getByTestId(HomePageAndOrderDetailPageIds.operatorTitleTestId).textContent().trim(), title);
        Assert.assertEquals(page.getByTestId(HomePageAndOrderDetailPageIds.infoListTestId).
                getByTestId(HomePageAndOrderDetailPageIds.coverageRowTestId).textContent().trim(), "COVERAGE");
        Assert.assertEquals(page.getByTestId(HomePageAndOrderDetailPageIds.infoListTestId).
                getByTestId(HomePageAndOrderDetailPageIds.coverageValueTestId).textContent().trim(), country);
        Assert.assertEquals(page.getByTestId(HomePageAndOrderDetailPageIds.infoListTestId).
                getByTestId(HomePageAndOrderDetailPageIds.dataRowTestId).textContent().trim(), "DATA");
        Assert.assertEquals(page.getByTestId(HomePageAndOrderDetailPageIds.infoListTestId).
                getByTestId(HomePageAndOrderDetailPageIds.dataValueTestId).textContent().trim(), volume);
        Assert.assertEquals(page.getByTestId(HomePageAndOrderDetailPageIds.infoListTestId).
                getByTestId(HomePageAndOrderDetailPageIds.validityRowTestId).textContent().trim(), "VALIDITY");
        Assert.assertEquals(page.getByTestId(HomePageAndOrderDetailPageIds.infoListTestId).
                getByTestId(HomePageAndOrderDetailPageIds.validityValueTestId).textContent().trim(), timePeriod);
        Assert.assertEquals(page.getByTestId(HomePageAndOrderDetailPageIds.infoListTestId).
                getByTestId(HomePageAndOrderDetailPageIds.priceRowTestId).textContent().trim(), "PRICE");
        Assert.assertEquals(page.getByTestId(HomePageAndOrderDetailPageIds.infoListTestId).
                getByTestId(HomePageAndOrderDetailPageIds.priceValueTestId).textContent().trim(), price);
    }
    //To handle popups and used a for loop in case we need to handle more popups in the future
    public void HandlePopups(List<Locator> list){
        for (Locator locator : list) {
            try {
                locator.click();
            } catch (Exception e) {
                System.out.println(locator + "Not Found");
            }
        }
    }
    @AfterTest
    public void tearDown() {
        page.context().browser().close();
    }
}
