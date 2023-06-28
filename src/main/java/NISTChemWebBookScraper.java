import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NISTChemWebBookScraper {
    public static Double getFormulaFromCAS(String chemicalCASNumber, List<String> notFound, Object[][] data, int rowToWriteTo) {
        String baseUrl = "https://webbook.nist.gov/cgi/cbook.cgi/";
        String queryParams = String.format("?ID=%s", chemicalCASNumber.replace(" ", "+"));

        try {
            // Connect to the NIST Chemistry Webbook website
            Document document = Jsoup.connect(baseUrl + queryParams).get();

            // Select the element that has the chemical formula string or mark the cas as not found
            if (document.select("#main > h1").toString().contains("Registry Number Not Found")) {
                data[rowToWriteTo][0] = chemicalCASNumber;
                notFound.add(chemicalCASNumber);
            }else {
//                System.out.println("CAS Number = " + chemicalCASNumber);
                Elements targetElement = document.select("#main > ul:nth-child(2) > li:nth-child(1)");

//                System.out.println("targetHTMLElement = " + targetElement);

            // Initialize Data Object[][]
                Object[] dataRow = new Object[11];
            // Use RegEx / Pattern Matching to parse number of carbons, number of C, H, O, N, S, F, Cl, Br, I, Si
                // First, use regex to isolate the formula from most unused text
                String unrefinedFormulaRegex = ".*?</strong>";
                String unrefinedFormula = targetElement.toString().replaceAll(unrefinedFormulaRegex, "");
//                System.out.println("unrefined formula = " + unrefinedFormula);
                // Second, use regex to remove the HTML tags
                String formulaRegex = "<sub>|</sub>|<li>|</li>";
                String formula = unrefinedFormula.replaceAll(formulaRegex, "");
//                System.out.println("formula = " + formula);
                System.out.println(formula);
                // Search for the letters that correspond to atoms common to organic molecules and search for 0 or more
                // digits as group2 (count value)
                Pattern regex3 = Pattern.compile("([CHONSFCBISP][rl]*)([0-9]*)");
                Matcher matcher = regex3.matcher(formula);
                data[rowToWriteTo][0] = chemicalCASNumber;
                data[rowToWriteTo][1] = 0;
                data[rowToWriteTo][2] = 0;
                data[rowToWriteTo][3] = 0;
                data[rowToWriteTo][4] = 0;
                data[rowToWriteTo][5] = 0;
                data[rowToWriteTo][6] = 0;
                data[rowToWriteTo][7] = 0;
                data[rowToWriteTo][8] = 0;
                data[rowToWriteTo][9] = 0;
                data[rowToWriteTo][10] = 0;
                int dataColumnIndex = -1;
                while (matcher.find()){
                    String atomName = matcher.group(1);
//                    System.out.print(atomName);
                    switch (atomName){
                        case "C": dataColumnIndex = 1;
                            break;
                        case "H": dataColumnIndex = 2;
                            break;
                        case "O": dataColumnIndex = 3;
                            break;
                        case "N": dataColumnIndex = 4;
                            break;
                        case "S": dataColumnIndex = 5;
                            break;
                        case "F": dataColumnIndex = 6;
                            break;
                        case "Cl": dataColumnIndex = 7;
                            break;
                        case "Br": dataColumnIndex = 8;
                            break;
                        case "I": dataColumnIndex = 9;
                            break;
                        case "P": dataColumnIndex = 10;
                            break;
                    }
                    String atomCount = matcher.group(2);
                    if (atomCount.equals("")) atomCount = "1";
//                    System.out.print(" " + atomCount);
//                    System.out.println();
                    data[rowToWriteTo][dataColumnIndex] = atomCount;
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static CSVParser getDataParser(){
        try {
            FileReader fileReader = new FileReader("src/main/java/casNumbers.csv");
            return CSVFormat.DEFAULT.parse(fileReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeCsvFile(String fileName, String[] header, Object[][] data) throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(header).build();
        // Create a BufferedWriter and CSVPrinter
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {

            // Write the data to the CSV file
            for (Object[] row : data) {
                csvPrinter.printRecord(row);
            }

            // Flush the stream and close the CSVPrinter
            csvPrinter.flush();
        }
    }


    public static void main(String[] args) {
        int desiredSize = 11;
        Object[][] data = new Object[desiredSize][11]; // replace desiredSize
        ArrayList<String> notFound = new ArrayList<>();
        CSVParser parser = getDataParser();

        int rowToWriteTo = 0;
        for (CSVRecord record : parser){
            if (record.get(0).equals("CAS")) continue;
            String cas = record.get(0);
            getFormulaFromCAS(cas, notFound, data, rowToWriteTo);
            data[rowToWriteTo][0] = cas;
            rowToWriteTo++;
        }

//        System.out.println(notFound);
//        System.out.println("notFound.size() = " + notFound.size());

        // Write the CSV file
        String fileName = "src/main/java/exampleOutput.csv";

        // Define the header for the CSV file
        String[] header = {"CAS", "C", "H", "O", "N","S","F","Cl","Br","I","P"};

        // Write the file
        try {
            writeCsvFile(fileName, header, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

