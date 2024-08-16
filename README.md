Test Automation Airalo

This project contains automated test cases written in Java and powered by the Playwright framework. The instructions below will guide you through setting up your environment and running the tests.

## Prerequisites

Before you can run the tests, you need to have the following software installed on your system:

1. **Java Development Kit (JDK)**
2. **Node.js**
3. **Playwright**

### 1. Install Java Development Kit (JDK)

1. Download and install the JDK from the official Oracle website or your preferred package manager:
   - [Oracle JDK Download](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
   - [OpenJDK Download](https://openjdk.java.net/install/)
   
2.Install Node.js From this link or Google 
[NodeJS] https://nodejs.org/dist/v20.16.0/node-v20.16.0-x64.msi

3.After Installing Node.js download and install Playwright and its browsers 

You can use this command to download and install playwright and execute second command to install required browsers.
1. npm install playwright
2. npx playwright install

Note: Remove "" when installing with commands given above.

4.Clone the GitHub repository from the link.

GitHub Link: 


5.Open the project in any IDE of your choice if the code needs to be inspected which supports JAVA.
	
1.Navigate to test directory AiraloTest/src/test/java/org/example/HomePageAndApiTest.java

2.Run the tests.

##NOTE if the tests don't work and give maven or POM error try using these 3 commands in the project directory where you cloned the repo.
mvn clean install -U
mvn clean install
mvn dependency:resolve

##Approach for Test Automation Task 1:
The Task 1 was rather simple UI Automation Test, so ran the flow manually a couple of times to understand it and then I took the Test-id as locators and perform actions according to the requirement and then 
Added the assertions accordingly.

##Approach for Test Automation Test 2:
2.1:
So for the automation of API Tests, I also used Playwright. I mapped out everything using Postman that how everything was working and then started doing its Automation.
For the Post Request it was pretty simple I made a HashMap to feed data to my post request.
Then created a post request with required headers, Saved it into an API RESPONCE object from playwright,
From that response I extracted status code and body and performed assertions using Jackson API by parsing it into ObjectMapper for extraction and then performed an assertion using for each loop on the Data Array I created using Jackson built in Methods to validate number of orders that I got back from this array.

2.2:
For the Get Response API Test, Similar to above mapped out everything using Postman and then started with a get request, Asserted the Status 1st then parse the body again into Object Mapper to get the body response.
Then I created a condition where I checked total number of the orders then I filtered using an if condition the ESIM types and package id and increased my count based on the result I found.
Then Assert these in the end to the total number of orders and also Asserted the type of ESIM with specific package "merhaba-7days-1gb". 

##Further Improvements
The Getting of Token from the Airalo can also be automated but in my opinion the token is valid for one year, so I choose not to automate getting it everytime since it would be redundant to do so.
There can be added further Assertions according to the requirements and needs of the test execution. I have added hardcoded Basic assertions for the order details which can easily be changed to take values from the Order response which we can get after creating a post request for order and then Assert data according to its values. I have not added any sort of extensive error handling which in my opinion should be there and can be added later.
I also wanted to use try catch for some of the locators for 1st task which could have made the test more stable.
