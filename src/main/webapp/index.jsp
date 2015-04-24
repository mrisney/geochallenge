<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">

<link rel="stylesheet" href="http://code.jquery.com/mobile/1.4.5/jquery.mobile-1.4.5.min.css">
<script src="http://code.jquery.com/jquery-1.11.2.min.js"></script>
<script src="http://code.jquery.com/mobile/1.4.5/jquery.mobile-1.4.5.min.js"></script>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
</head>
<body>
<div data-role="page">


  <div data-role="main" class="ui-content">
    <pre><span class="inner-pre" style="font-size: 11px">InAuth Coding Challenge:
Create a DB (whatever flavor) containing 10,000 random entries for valid latitude, longitude coordinates.  

Create 3 Web Services:
a) getAllDataSets() - GET method, returns all data in the DB
b) getData(latitude, longitude) - GET method, returns if the coordinates exist in the DB or not
c) addData(latitude, longitude) - POST method, adds the coordinate to the DB if it doesn't exist

From these web services, create a Java solution for the following:

1.)  Given the entry's coordinates, determine if those coordinates are within the United States.

2.)  If they're not within the United States, determine if the coordinates are within 500 miles of the following cities:

Tokyo, Japan
Sydney, Australia
Riyadh, Saudi Arabia
Zurich, Switzerland
Reykjavik, Iceland
Mexico City, Mexico
Lima, Peru

3.)  For each of the above, tell us how far away the entry's coordinates are from each city.</span></pre>
    <a href="./rest/generateRandomData" data-ajax="false">Generate Random Coordinate Points</a>
    <br>
    <a href="./rest/getAllDataSets" data-ajax="false">Get All Randomly Generated Data</a>
 	<br>
 	<a href="./rest/getData/0.0/0.0" data-ajax="false">getData/{latitude}/{longitude}</a>
 	<br>
 	<a href="./rest/findInUSA" data-ajax="false">Find All Generated Data Within United States (CONUS,Alaska,Hawaii)</a>
 	<br>
 	<a href="./rest/coordinatesWithin500Miles" data-ajax="false">Find All Generated Data Not Within United States, 500 miles from Cities</a>
 	<br>
 	<a href="./rest/removeAllDataSets" data-ajax="false">Remove All Generated Data</a>
  	
  </div>

  <div data-role="footer">
    <h1>Footer Text</h1>
  </div>
</div> 

</body>
</html>