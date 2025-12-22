package com.toufik.tws.wrapper;

import com.ib.client.*;
import com.ib.client.protobuf.ErrorMessageProto;
import com.ib.client.protobuf.ExecutionDetailsEndProto;
import com.ib.client.protobuf.ExecutionDetailsProto;
import com.ib.client.protobuf.OpenOrderProto;
import com.ib.client.protobuf.OpenOrdersEndProto;
import com.ib.client.protobuf.OrderStatusProto;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class TwsWrapper implements EWrapper {

    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicReference<ErrorInfo> lastError = new AtomicReference<>();
    private CountDownLatch connectionLatch;

    // Historical data storage
    private final Map<Integer, List<Bar>> historicalDataMap = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<Integer, CountDownLatch> historicalDataLatches = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<Integer, String> historicalDataStartDates = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<Integer, String> historicalDataEndDates = new java.util.concurrent.ConcurrentHashMap<>();

    // Scanner data storage
    private final Map<Integer, List<ContractDetails>> scannerDataMap = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<Integer, CountDownLatch> scannerLatches = new java.util.concurrent.ConcurrentHashMap<>();

    public void setConnectionLatch(CountDownLatch latch) {
        this.connectionLatch = latch;
    }

    public boolean isConnected() {
        return connected.get();
    }

    public ErrorInfo getLastError() {
        return lastError.get();
    }

    public void clearError() {
        lastError.set(null);
    }

    public void prepareHistoricalDataRequest(int reqId) {
        historicalDataMap.put(reqId, new ArrayList<>());
        historicalDataLatches.put(reqId, new CountDownLatch(1));
    }

    public List<Bar> getHistoricalData(int reqId) {
        return historicalDataMap.get(reqId);
    }

    public CountDownLatch getHistoricalDataLatch(int reqId) {
        return historicalDataLatches.get(reqId);
    }

    public String getHistoricalDataStartDate(int reqId) {
        return historicalDataStartDates.get(reqId);
    }

    public String getHistoricalDataEndDate(int reqId) {
        return historicalDataEndDates.get(reqId);
    }

    public void clearHistoricalData(int reqId) {
        historicalDataMap.remove(reqId);
        historicalDataLatches.remove(reqId);
        historicalDataStartDates.remove(reqId);
        historicalDataEndDates.remove(reqId);
    }

    // Scanner data methods
    public void prepareScannerRequest(int reqId) {
        scannerDataMap.put(reqId, new ArrayList<>());
        scannerLatches.put(reqId, new CountDownLatch(1));
    }

    public List<ContractDetails> getScannerData(int reqId) {
        return scannerDataMap.get(reqId);
    }

    public CountDownLatch getScannerLatch(int reqId) {
        return scannerLatches.get(reqId);
    }

    public void clearScannerData(int reqId) {
        scannerDataMap.remove(reqId);
        scannerLatches.remove(reqId);
    }

    public static class ErrorInfo {
        private final int id;
        private final long errorTime;
        private final int errorCode;
        private final String errorMsg;
        private final String advancedOrderRejectJson;

        public ErrorInfo(int id, long errorTime, int errorCode, String errorMsg, String advancedOrderRejectJson) {
            this.id = id;
            this.errorTime = errorTime;
            this.errorCode = errorCode;
            this.errorMsg = errorMsg;
            this.advancedOrderRejectJson = advancedOrderRejectJson;
        }

        public int getId() {
            return id;
        }

        public long getErrorTime() {
            return errorTime;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public String getAdvancedOrderRejectJson() {
            return advancedOrderRejectJson;
        }

        @Override
        public String toString() {
            return "ErrorInfo{" +
                    "id=" + id +
                    ", errorTime=" + errorTime +
                    ", errorCode=" + errorCode +
                    ", errorMsg='" + errorMsg + '\'' +
                    ", advancedOrderRejectJson='" + advancedOrderRejectJson + '\'' +
                    '}';
        }
    }

    @Override
    public void tickPrice(int tickerId, int field, double price, TickAttrib attribs) {}

    @Override
    public void tickSize(int tickerId, int field, Decimal size) {}

    @Override
    public void tickOptionComputation(int tickerId, int field, int tickAttrib, double impliedVol, double delta,
                                       double optPrice, double pvDividend, double gamma, double vega, double theta,
                                       double undPrice) {}

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {}

    @Override
    public void tickString(int tickerId, int tickType, String value) {}

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints,
                        double impliedFuture, int holdDays, String futureLastTradeDate, double dividendImpact,
                        double dividendsToLastTradeDate) {}

    @Override
    public void orderStatus(int orderId, String status, Decimal filled, Decimal remaining, double avgFillPrice,
                           long permId, int parentId, double lastFillPrice, int clientId, String whyHeld,
                           double mktCapPrice) {}

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {}

    @Override
    public void openOrderEnd() {}

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {}

    @Override
    public void updatePortfolio(Contract contract, Decimal position, double marketPrice, double marketValue,
                               double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {}

    @Override
    public void updateAccountTime(String timeStamp) {}

    @Override
    public void accountDownloadEnd(String accountName) {}

    @Override
    public void nextValidId(int orderId) {
        log.info("Connected to TWS. Next valid order ID: {}", orderId);
        connected.set(true);
        if (connectionLatch != null) {
            connectionLatch.countDown();
        }
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {}

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {}

    @Override
    public void contractDetailsEnd(int reqId) {}

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {}

    @Override
    public void execDetailsEnd(int reqId) {}

    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, Decimal size) {}

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side,
                                 double price, Decimal size, boolean isSmartDepth) {}

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {}

    @Override
    public void managedAccounts(String accountsList) {}

    @Override
    public void receiveFA(int faDataType, String xml) {}

    @Override
    public void historicalData(int reqId, Bar bar) {
        log.debug("Historical data received for reqId {}: {}", reqId, bar.time());
        List<Bar> bars = historicalDataMap.get(reqId);
        if (bars != null) {
            bars.add(bar);
        }
    }

    @Override
    public void historicalDataEnd(int reqId, String startDate, String endDate) {
        log.info("Historical data complete for reqId {}. Start: {}, End: {}", reqId, startDate, endDate);
        historicalDataStartDates.put(reqId, startDate);
        historicalDataEndDates.put(reqId, endDate);
        CountDownLatch latch = historicalDataLatches.get(reqId);
        if (latch != null) {
            latch.countDown();
        }
    }

    @Override
    public void scannerParameters(String xml) {
        log.debug("Scanner parameters received");
    }

    @Override
    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance,
                           String benchmark, String projection, String legsStr) {
        log.debug("Scanner data received for reqId {}: rank={}, symbol={}", 
                 reqId, rank, contractDetails != null && contractDetails.contract() != null ? 
                 contractDetails.contract().symbol() : "null");
        List<ContractDetails> data = scannerDataMap.get(reqId);
        if (data != null && contractDetails != null) {
            data.add(contractDetails);
        }
    }

    @Override
    public void scannerDataEnd(int reqId) {
        log.info("Scanner data complete for reqId {}", reqId);
        CountDownLatch latch = scannerLatches.get(reqId);
        if (latch != null) {
            latch.countDown();
        }
    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high, double low, double close,
                           Decimal volume, Decimal wap, int count) {}

    @Override
    public void currentTime(long time) {}

    @Override
    public void currentTimeInMillis(long timeInMillis) {}

    @Override
    public void fundamentalData(int reqId, String data) {}

    @Override
    public void deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract) {}

    @Override
    public void tickSnapshotEnd(int reqId) {}

    @Override
    public void marketDataType(int reqId, int marketDataType) {}

    @Override
    public void commissionAndFeesReport(CommissionAndFeesReport commissionAndFeesReport) {}

    @Override
    public void position(String account, Contract contract, Decimal pos, double avgCost) {}

    @Override
    public void positionEnd() {}

    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {}

    @Override
    public void accountSummaryEnd(int reqId) {}

    @Override
    public void verifyMessageAPI(String apiData) {}

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {}

    @Override
    public void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {}

    @Override
    public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {}

    @Override
    public void displayGroupList(int reqId, String groups) {}

    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {}

    @Override
    public void error(Exception ex) {
        log.error("Exception error: ", ex);
        lastError.set(new ErrorInfo(-1, System.currentTimeMillis(), -1, ex.getMessage(), null));
    }

    @Override
    public void error(String str) {
        log.error("String error: {}", str);
        lastError.set(new ErrorInfo(-1, System.currentTimeMillis(), -1, str, null));
    }

    @Override
    public void error(int id, long errorTime, int errorCode, String errorMsg, String advancedOrderRejectJson) {
        log.error("Error - ID: {}, Time: {}, Code: {}, Message: {}, AdvancedReject: {}",
                 id, errorTime, errorCode, errorMsg, advancedOrderRejectJson);
        lastError.set(new ErrorInfo(id, errorTime, errorCode, errorMsg, advancedOrderRejectJson));
    }

    @Override
    public void connectionClosed() {
        log.info("Connection to TWS closed");
        connected.set(false);
    }

    @Override
    public void connectAck() {}

    @Override
    public void positionMulti(int reqId, String account, String modelCode, Contract contract, Decimal pos,
                             double avgCost) {}

    @Override
    public void positionMultiEnd(int reqId) {}

    @Override
    public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value,
                                  String currency) {}

    @Override
    public void accountUpdateMultiEnd(int reqId) {}

    @Override
    public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId,
                                                    String tradingClass, String multiplier,
                                                    Set<String> expirations, Set<Double> strikes) {}

    @Override
    public void securityDefinitionOptionalParameterEnd(int reqId) {}

    @Override
    public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {}

    @Override
    public void familyCodes(FamilyCode[] familyCodes) {}

    @Override
    public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {}

    @Override
    public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {}

    @Override
    public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline,
                        String extraData) {}

    @Override
    public void smartComponents(int reqId, Map<Integer, Map.Entry<String, Character>> theMap) {}

    @Override
    public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {}

    @Override
    public void newsProviders(NewsProvider[] newsProviders) {}

    @Override
    public void newsArticle(int requestId, int articleType, String articleText) {}

    @Override
    public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {}

    @Override
    public void historicalNewsEnd(int requestId, boolean hasMore) {}

    @Override
    public void headTimestamp(int reqId, String headTimestamp) {}

    @Override
    public void histogramData(int reqId, List<HistogramEntry> items) {}

    @Override
    public void historicalDataUpdate(int reqId, Bar bar) {}

    @Override
    public void rerouteMktDataReq(int reqId, int conId, String exchange) {}

    @Override
    public void rerouteMktDepthReq(int reqId, int conId, String exchange) {}

    @Override
    public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {}

    @Override
    public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {}

    @Override
    public void pnlSingle(int reqId, Decimal pos, double dailyPnL, double unrealizedPnL, double realizedPnL,
                         double value) {}

    @Override
    public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean last) {}

    @Override
    public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {}

    @Override
    public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {}

    @Override
    public void tickByTickAllLast(int reqId, int tickType, long time, double price, Decimal size,
                                  TickAttribLast tickAttribLast, String exchange, String specialConditions) {}

    @Override
    public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, Decimal bidSize,
                                 Decimal askSize, TickAttribBidAsk tickAttribBidAsk) {}

    @Override
    public void tickByTickMidPoint(int reqId, long time, double midPoint) {}

    @Override
    public void orderBound(long permId, int clientId, int orderId) {}

    @Override
    public void completedOrder(Contract contract, Order order, OrderState orderState) {}

    @Override
    public void completedOrdersEnd() {}

    @Override
    public void replaceFAEnd(int reqId, String text) {}

    @Override
    public void wshMetaData(int reqId, String dataJson) {}

    @Override
    public void wshEventData(int reqId, String dataJson) {}

    @Override
    public void historicalSchedule(int reqId, String startDateTime, String endDateTime, String timeZone,
                                   List<HistoricalSession> sessions) {}

    @Override
    public void userInfo(int reqId, String whiteBrandingId) {}

    @Override
    public void orderStatusProtoBuf(OrderStatusProto.OrderStatus orderStatusProto) {}

    @Override
    public void openOrderProtoBuf(OpenOrderProto.OpenOrder openOrderProto) {}

    @Override
    public void openOrdersEndProtoBuf(OpenOrdersEndProto.OpenOrdersEnd openOrdersEnd) {}

    @Override
    public void errorProtoBuf(ErrorMessageProto.ErrorMessage errorMessageProto) {}

    @Override
    public void execDetailsProtoBuf(ExecutionDetailsProto.ExecutionDetails executionDetailsProto) {}

    @Override
    public void execDetailsEndProtoBuf(ExecutionDetailsEndProto.ExecutionDetailsEnd executionDetailsEndProto) {}
}
