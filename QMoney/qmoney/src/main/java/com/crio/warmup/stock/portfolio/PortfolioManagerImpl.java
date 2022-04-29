
package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import static java.time.temporal.ChronoUnit.DAYS;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;
  private ObjectMapper objectMapper = getObjectMapper();
  // Caution: Do not delete or modify the constructor, or else your build will
  // break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
  
  // TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from
  // main anymore.
  // Copy your code from Module#3
  // PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and ensure it follows the
  // method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required
  // further as our
  // clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command
  // below:
  // ./gradlew test --tests PortfolioManagerTest

  // CHECKSTYLE:OFF

  public PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService=stockQuotesService;
  }


private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Extract the logic to call Tiingo third-party APIs to a separate function.
  // Remember to fill out the buildUri function and use that.
  // static Double getOpeningPriceOnStartDate(List<Candle> candles) {
  //   return candles.get(0).getOpen();

  // }

  // public static Double getClosingPriceOnEndDate(List<Candle> candles) {
  //   List<Double> ans = new ArrayList<>();
  //   for (Candle cd : candles) {
  //     ans.add(cd.getClose());
  //   }
  //   Collections.sort(ans);
  //   return ans.get(ans.size() - 1);
  // }
  
  public List<TiingoCandle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
        String url=buildUri( symbol,from, to);
        String result = (restTemplate.getForObject(url,String.class));
        List<TiingoCandle> candleList = objectMapper.readValue(result,new TypeReference<List<TiingoCandle>>() {});
        return candleList;
  }

  public static String getToken() {
    //return "77b1883ae3ed138812797b8b34b39689812c474a";
    return "73a4b6b45b2a6a554a0c20d91bc0fdfa0b858727";
  }

  protected static String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    // String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
    // + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    return String.format("https://api.tiingo.com/tiingo/daily/%s/prices?startDate=%s&endDate=%s&token=%s", symbol,
        startDate, endDate, getToken());
  }

  // public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token)
  //     throws JsonProcessingException {

  //   String Url = buildUri(trade.getSymbol(), trade.getPurchaseDate(), endDate);
  //   TiingoCandle[] tc = restTemplate.getForObject(Url, TiingoCandle[].class);
  //   ObjectMapper objectMapper = new ObjectMapper();
  //   objectMapper.registerModule(new JavaTimeModule());
  //   String s=objectMapper.writeValueAsString(tc);
  //   //System.out.println(s);
  //   // if(tc == null ){
  //   //   return Collections.emptyList();
  //   // }
  //   return Arrays.asList(objectMapper.readValue(s, TiingoCandle[].class));
  //   //return Collections.emptyList();
  // }
  
  
  
  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate)
      throws StockQuoteServiceException
       {
        List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
        for (PortfolioTrade obj : portfolioTrades) {
          List<Candle> candleList = new ArrayList<>();
          try {
            candleList = stockQuotesService.getStockQuote(obj.getSymbol(),obj.getPurchaseDate(), endDate);
          }  catch (StockQuoteServiceException | JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          TiingoCandle candleObj = (TiingoCandle) candleList.get(candleList.size() - 1);
          Double buyPrice = candleList.get(0).getOpen();
          Double sellPrice = candleObj.getClose();
          Double totalReturn = (sellPrice - buyPrice) / buyPrice; 
          double totalNoOfYears = ChronoUnit.DAYS.between(obj.getPurchaseDate(),endDate) / 365.0;
          Double annualizedReturn = Math.pow((1 + totalReturn),(1.0 / totalNoOfYears)) - 1;
          AnnualizedReturn anRet =  new AnnualizedReturn(obj.getSymbol(),annualizedReturn,totalReturn);
          annualizedReturns.add(anRet);
        }
        Collections.sort(annualizedReturns,getComparator());
        return annualizedReturns;
}

@Override
public List<AnnualizedReturn> calculateAnnualizedReturnParallel(List<PortfolioTrade> portfolioTrades, LocalDate endDate,
    int numThreads) throws InterruptedException, StockQuoteServiceException {
  // TODO Auto-generated method stub
  List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
  List<Callable<List<Object>>> callableTasks = new ArrayList<>();

  
  ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);

  for (PortfolioTrade trade : portfolioTrades) {
    String symbol = trade.getSymbol();
    LocalDate startDate = trade.getPurchaseDate();

    callableTasks.add(() -> {
      List<Candle> quotes = stockQuotesService.getStockQuote(symbol, startDate, endDate);

      return Arrays.asList(quotes, symbol, startDate);
    });
  }

  List<Future<List<Object>>> futureTasks = threadPool.invokeAll(callableTasks);

  for (Future<List<Object>> task : futureTasks) {
    LocalDate startDate = LocalDate.now();
    String symbol = "";
    List<Candle> quotes = new ArrayList<>();

    try {
      quotes = (List<Candle>) task.get().get(0);
      symbol = (String) task.get().get(1);
      startDate = (LocalDate) task.get().get(2);
    } catch (ExecutionException e) {
      throw new StockQuoteServiceException(e.getMessage());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    double totalNumOfYears = (double) startDate.until(endDate, DAYS) / 365.24;
    double buyPrice = quotes.get(0).getOpen();
    double sellPrice = quotes.get(quotes.size() - 1).getClose();

    Double totalReturns = (sellPrice - buyPrice) / buyPrice;
    Double annualizedReturn = Math.pow((1 + totalReturns), (1 / totalNumOfYears)) - 1;

    annualizedReturns.add(new AnnualizedReturn(symbol, annualizedReturn, totalReturns));
  }

  Collections.sort(annualizedReturns, this.getComparator());

  threadPool.shutdown();
  try {
    if (!threadPool.awaitTermination(800, TimeUnit.MILLISECONDS)) {
      threadPool.shutdownNow();
    }
  } catch (InterruptedException e) {
    threadPool.shutdownNow();
  }

  return annualizedReturns;
}
}
    
