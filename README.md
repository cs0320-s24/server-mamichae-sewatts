> **GETTING STARTED:** You must start from some combination of the CSV Sprint code that you and your partner ended up with. Please move your code directly into this repository so that the `pom.xml`, `/src` folder, etc, are all at this base directory.

> **IMPORTANT NOTE**: In order to run the server, run `mvn package` in your terminal then `./run` (using Git Bash for Windows users). This will be the same as the first Sprint. Take notice when transferring this run sprint to your Sprint 2 implementation that the path of your Server class matches the path specified in the run script. Currently, it is set to execute Server at `edu/brown/cs/student/main/server/Server`. Running through terminal will save a lot of computer resources (IntelliJ is pretty intensive!) in future sprints.

# Project Details
    Web API Server
    Our Web API Server handles HTTP requests and returns data to clients through. 
    It manages different endpoints, and processes and fulfills requests.
    Team Members: Sylvie Watts (sewatts), Melanie Michael (mamichae)
    Hours Spent: 15
    Repo: https://github.com/cs0320-s24/server-mamichae-sewatts

# Design Choices
    The server interacts with CSV files and external data sources 
    through the AccessCSV and CensusDataSource interfaces. 
    Having interfaces standardize the access and use of data. 
    
    The server defines endpoints to handle different types of requests 
    (e.g., loadcsv, viewcsv, searchcsv) and associates each endpoint with a corresponding handler class 
    (e.g., LoadCSVHandler, ViewCSVHandler, SearchCSVHandler). 
    These handlers work to process requests and generate responses (both success and error responses).
    
    The CachingCensusDataSource class uses caching to improve performance by storing 
    previously fetched data in memory. This reduces the need for repeated requests to the API, 
    allowing for faster response times for frequently accessed data.

    The Moshi library is used for JSON serialization and deserialization, 
    allowing for the conversion of Java objects to JSON format. 
    This allows for exchange of data between the server, API, and clients in an acceptable format.

# Errors/Bugs
    I don't know if this is required functionality but AccessCountyCodes is run everytime, so it looks through the countyCodes data
    with the given state, and saves it to the hashMap everytime. We did have the states save, so during one run of server you only call
    accessStateCodes once, but because you need to run accessCountyCodes with the state you are searching that time, it runs everytime.

# Tests
    We tested each of the handlers within our test suites. 
    The TestLoadHandler tests for a successful file load, and bad file loads in instances 
    of invalid queries, missing parameters, and malformed data.
    The TestSearchHandler tests for overall search success, with columnID and columnName, 
    and bad search queries with files not found, empty files, search queries with invalid value and column inputs, 
    malformed data, and missing parameters. 
    The ViewSearchHandler tests for overall view success, view success showing parsed output, 
    and failure with bad filepath and empty files. 
    The TestBroadband tests for success with queries to the API census, using a mocked data source. 

# How to    
    To run the program, run the main method
    In local host with the browser
    To run LoadCSV, add /loadcsv?filepath={input filepath}&headers={boolean true/false}
    To run ViewCSV, add /viewcsv
    To run SearchCSV, add /searchcsv?value={search value}&columnID={number or String name}
    For SearchCSV, addition of &columnID={number or String name} is optional
    To run broadband, add /broadband?state={String state name}&county={String county name}
    If the user inputs an incorrect State or county, the Server lets them know that
