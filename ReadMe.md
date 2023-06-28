### Description
Input -- A .csv file of CAS numbers to identify desired organic molecules (data that can be found a variety of places)

Output -- a .csv file with fields for CAS number and a count of each atom: C, H, O, N, S, F, Cl, Br, I, P.

Operation: Scrapes the NIST chemistry WebBook for the data. Uses regular expression to parse formula. Apache CSV library to read and write csvs.

### Get It Running

(1) Install intelliJ

(2) Open the project as a maven project (if intelliJ doesn't recognize the project as a maven project, right-click the pom.xml file in the intelliJ project window and select something like "Maven -> Reload Project"; this should help intelliJ to recognize the project as a maven project)

(3) Build the project using Maven to get the dependencies