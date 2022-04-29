
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;

import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.function.Power;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication {

  // TODO: CRIO_TASK_MODULE_REST_API
  // Find out the closing price of each stock on the end_date and return the list
  // of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  // and deserialize the results in List<Candle>
  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  /*
   * public static List<String> mainReadQuotes(String[] args) throws IOException,
   * URISyntaxException {
   * 
   * File f = resolveFileFromResources(args[0]); ObjectMapper om =
   * getObjectMapper(); PortfolioTrade[] trade = om.readValue(f,
   * PortfolioTrade[].class); String
   * token="73a4b6b45b2a6a554a0c20d91bc0fdfa0b858727"; //DateTimeFormatter
   * DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd"); LocalDate
   * localDate = LocalDate.parse(args[1]); RestTemplate rt = new RestTemplate();
   * List<TotalReturnsDto> ls = new ArrayList<TotalReturnsDto>();
   * for(PortfolioTrade tade:trade){ String
   * my_url=prepareUrl(tade,localDate,token); TiingoCandle[] tingo
   * =rt.getForObject(my_url, TiingoCandle[].class);
   * 
   * if(tingo==null){ continue; } // candle helper object to sort symbols
   * according to their current prices -> TotalReturnsDto temp = new
   * TotalReturnsDto(tade.getSymbol(),tingo[tingo.length-1].getClose());
   * ls.add(temp);
   * 
   * } Collections.sort(ls, new Comparator<TotalReturnsDto>() {
   * 
   * @Override public int compare(TotalReturnsDto p1, TotalReturnsDto p2) { return
   * (int)(p1.getClosingPrice().compareTo(p2.getClosingPrice())); } });
   * List<String> ans = new ArrayList<>(); for(int i=0;i<ls.size();i++) {
   * ans.add(ls.get(i).getSymbol()); } return ans;
   * 
   * }
   */

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    File f = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();
    om.registerModule(new JavaTimeModule());
    PortfolioTrade[] trades = om.readValue(f, PortfolioTrade[].class);
    RestTemplate rt = new RestTemplate();
    List<TotalReturnsDto> ls = new ArrayList<TotalReturnsDto>();
    for (PortfolioTrade pf : trades) {
      // LocalDate start = pf.getPurchaseDate();
      String sym = pf.getSymbol();
      LocalDate localDate = LocalDate.parse(args[1]);
      String Url = prepareUrl(pf, localDate, "77b1883ae3ed138812797b8b34b39689812c474a");
      TiingoCandle[] tc = rt.getForObject(Url, TiingoCandle[].class);
      if (tc == null) {
        continue;
      } // candle helper object to sort symbols according to their current prices ->
      TotalReturnsDto temp = new TotalReturnsDto(sym, tc[tc.length - 1].getClose());
      ls.add(temp);
    }
    Collections.sort(ls, new Comparator<TotalReturnsDto>() {
      @Override
      public int compare(TotalReturnsDto p1, TotalReturnsDto p2) {
        return (int) (p1.getClosingPrice().compareTo(p2.getClosingPrice()));
      }
    });
    List<String> ans = new ArrayList<>();
    for (int i = 0; i < ls.size(); i++) {
      ans.add(ls.get(i).getSymbol());
    }
    return ans;
  }

  // TODO:
  // After refactor, make sure that the tests pass by using these two commands
  // ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  // ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    File f = resolveFileFromResources(filename);
    ObjectMapper obj = getObjectMapper();
    List<PortfolioTrade> trade = obj.readValue(f, new TypeReference<List<PortfolioTrade>>() {
    });
    return trade;
  }

  // TODO:
  // Build the Url using given parameters and use this function in your code to
  // cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {

    return String.format("https://api.tiingo.com/tiingo/daily/%s/prices?startDate=%s&endDate=%s&token=%s",
        trade.getSymbol(), trade.getPurchaseDate(), endDate.toString(), token);
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Now that you have the list of PortfolioTrade and their data, calculate
  // annualized returns
  // for the stocks provided in the Json.
  // Use the function you just wrote #calculateAnnualizedReturns.
  // Return the list of AnnualizedReturns sorted by annualizedReturns in
  // descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

  // TODO:
  // Ensure all tests are passing using below command
  // ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();

  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    List<Double> ans = new ArrayList<>();
    for (Candle cd : candles) {
      ans.add(cd.getClose());
    }
    Collections.sort(ans);
    return ans.get(ans.size() - 1);
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token)
      throws JsonProcessingException {
    RestTemplate rt = new RestTemplate();
    String Url = prepareUrl(trade,endDate,token);
    TiingoCandle[] tc = rt.getForObject( Url, TiingoCandle[].class);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    String s=objectMapper.writeValueAsString(tc);
    //System.out.println(s);
    return Arrays.asList(objectMapper.readValue(s, TiingoCandle[].class));
    //return Collections.emptyList();
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args) throws IOException, URISyntaxException {
    File f = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();
    om.registerModule(new JavaTimeModule());
    PortfolioTrade[] trades = om.readValue(f, PortfolioTrade[].class);
    RestTemplate rt = new RestTemplate();
    List<AnnualizedReturn> ls = new ArrayList<AnnualizedReturn>();
  
    for(PortfolioTrade pf:trades)
    {
      //  LocalDate start = pf.getPurchaseDate();
       String sym = pf.getSymbol();
       LocalDate localDate = LocalDate.parse(args[1]);
       String Url = prepareUrl(pf,localDate,"77b1883ae3ed138812797b8b34b39689812c474a");
       TiingoCandle[] tc = rt.getForObject( Url, TiingoCandle[].class);
       if(tc==null){
         continue;
       } 
       AnnualizedReturn m= calculateAnnualizedReturns(localDate,pf, tc[0].getOpen(), tc[tc.length-1].getClose());
       ls.add(m);
    }
  
    Collections.sort(ls,new Comparator<AnnualizedReturn>() {         
  @Override          
  public int compare(AnnualizedReturn a1,AnnualizedReturn a2){        
    return a2.getAnnualizedReturn().compareTo(a1.getAnnualizedReturn());
  }       
 });
  
  return ls;
  
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      Double totalReturns= (sellPrice - buyPrice)/buyPrice;
      Double noYears = trade.getPurchaseDate().until(endDate,ChronoUnit.DAYS)/365.24;  
      Double annualized_returns = Math.pow(1+totalReturns, (1/noYears))-1;
      return new AnnualizedReturn(trade.getSymbol(), annualized_returns,totalReturns);
  }
















  /*public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    printJsonObject(mainReadFile(args));

    //mainReadQuotes(args);
  }*/

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File f=resolveFileFromResources(args[0]);
    ObjectMapper obj=getObjectMapper();
    PortfolioTrade[] trade=obj.readValue(f, PortfolioTrade[].class);
    List<String> ans=new ArrayList<String>(); 
    for(PortfolioTrade tade:trade){
      ans.add(tade.getSymbol());
    }
    return ans;
    //return Collections.emptyList();
  }


  public static List<String> debugOutputs() {

   
    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "/opt/criodo/qmoney/me_qmoney/qmoney/"
        + "build/resources/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@7350471";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "29";
   

    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }













  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       List<PortfolioTrade> trade = objectMapper.readValue(contents, new TypeReference<List<PortfolioTrade>>() {
      });
       //List<PortfolioTrade> trade=readTradesFromJson(file);
       PortfolioManagerFactory portfolioManager = new PortfolioManagerFactory();
       RestTemplate restTemplate = new RestTemplate();
       return portfolioManager.getPortfolioManager(restTemplate).calculateAnnualizedReturn(trade, endDate);
        //return Collections.emptyList();
  }


  private static String readFileAsString(String filename) throws URISyntaxException, IOException {
    return new String(Files.readAllBytes(resolveFileFromResources(filename).toPath()),
        "UTF-8");
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }

public static String getToken() {
  return "73a4b6b45b2a6a554a0c20d91bc0fdfa0b858727";
  //return "77b1883ae3ed138812797b8b34b39689812c474a";
}
}

