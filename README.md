# GeoChallenge
InAuth coding exercise
April 24, 2015 4:55 PM PST

1. Install redis, on macosx "brew install redis".
2. After installation, start a local instance, from a shell issue the command "redis-server" 
  - that's it, the methods with the application will populate the redis hashset
  
3. Deploy geochallenge.war located in the /bin folder in this repository into Apache Tomcat 7.0
4. By going to the URL the REST API's are accessible @ http://localhost:8080/geochallenge/index.jsp
5. Create the Generate Random Coordinate Points  service first, followed by Find All Generated Data Within United States (CONUS,Alaska,Hawaii) 
6. After these 2 REST API's are issued - the "Find All Generated Data Within United States (CONUS,Alaska,Hawaii)" is asynchrounous
  the "3rd Find All Generated Data Not Within United States, 500 miles from Cities" can be issued.

7. Generate Random Coordinate Points  will flush out the redis-db, and restart the generation of the 10K randonmly generated LAtLong Points
8. A convenience methods "Remove All Generated Data", does the same.

