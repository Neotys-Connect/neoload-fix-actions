package com.neotys.ps.fix.common;

import quickfix.*;
import quickfix.field.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FIXApplication implements Application {

    private FIXLogger logger;
    static private final TwoWayMap sideMap = new TwoWayMap();
    static private final TwoWayMap typeMap = new TwoWayMap();
    static private final TwoWayMap tifMap = new TwoWayMap();


    FIXApplication(FIXLogger logger) {
        this.logger = logger;
    }

    public FIXLogger getLogger() {
        return logger;
    }

    @Override
    public void onCreate(SessionID sessionId) {
        logger.info("<" + logger.getDateFormat().format(new Date(System.currentTimeMillis())) + ", " +
                sessionId + ", Session created>");
    }

    @Override
    public void onLogon(SessionID sessionId) {
        logger.info("<" + logger.getDateFormat().format(new Date(System.currentTimeMillis())) + ", " +
                sessionId + ", Session logged on>");
    }

    @Override
    public void onLogout(SessionID sessionId) {
        logger.info("<" + logger.getDateFormat().format(new Date(System.currentTimeMillis())) + ", " +
                sessionId + ", Session logged out>");
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        logger.info("<" + logger.getDateFormat().format(new Date(System.currentTimeMillis())) + ", " +
                sessionId + ", outgoing> " +
                FIXUtils.printFIXMessage(message.toString()));
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        logger.info("<" + logger.getDateFormat().format(new Date(System.currentTimeMillis())) + ", " +
                sessionId + ", incoming> " +
                FIXUtils.printFIXMessage(message.toString()));    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        logger.info("<" + logger.getDateFormat().format(new Date(System.currentTimeMillis())) + ", " +
                sessionId + ", outgoing> " +
                FIXUtils.printFIXMessage(message.toString()));
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        logger.info("<" + logger.getDateFormat().format(new Date(System.currentTimeMillis())) + ", " +
                sessionId + ", incoming> " +
                FIXUtils.printFIXMessage(message.toString()));
    }

    void send(Message message, SessionID sessionID) {
        try {
            Session.sendToTarget(message, sessionID);
        } catch (SessionNotFound e) {
            System.out.println(e);
        }
    }

    public Message buildOrder40(Order order) {
        quickfix.fix40.NewOrderSingle newOrderSingle = new quickfix.fix40.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()), new OrderQty(order.getQuantity()),
                typeToFIXType(order.getType()));

        return buildOrder(order, newOrderSingle);
    }

    public Message buildOrder41(Order order) {
        quickfix.fix41.NewOrderSingle newOrderSingle = new quickfix.fix41.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()), typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));

        return buildOrder(order, newOrderSingle);
    }

    public Message buildOrder42(Order order) {
        quickfix.fix42.NewOrderSingle newOrderSingle = new quickfix.fix42.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()), new TransactTime(), typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));

        return buildOrder(order, newOrderSingle);
    }

    public Message buildOrder43(Order order) {
        quickfix.fix43.NewOrderSingle newOrderSingle = new quickfix.fix43.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), sideToFIXSide(order.getSide()),
                new TransactTime(), typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));
        newOrderSingle.set(new Symbol(order.getSymbol()));
        return buildOrder(order, newOrderSingle);
    }

    public Message buildOrder44(Order order) {
        quickfix.fix44.NewOrderSingle newOrderSingle = new quickfix.fix44.NewOrderSingle(
                new ClOrdID(order.getID()), sideToFIXSide(order.getSide()),
                new TransactTime(), typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));
        newOrderSingle.set(new Symbol(order.getSymbol()));
        newOrderSingle.set(new HandlInst('1'));
        return buildOrder(order, newOrderSingle);
    }

    public Message buildOrder50(Order order) {
        quickfix.fix50.NewOrderSingle newOrderSingle = new quickfix.fix50.NewOrderSingle(
                new ClOrdID(order.getID()), sideToFIXSide(order.getSide()),
                new TransactTime(), typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));
        newOrderSingle.set(new Symbol(order.getSymbol()));
        newOrderSingle.set(new HandlInst('1'));

        return buildOrder(order, newOrderSingle);
    }

    public quickfix.Message buildOrder(Order order, quickfix.Message newOrderSingle) {

        OrderType type = order.getType();

        if (type == OrderType.LIMIT)
            newOrderSingle.setField(new Price(order.getLimit()));
        else if (type == OrderType.STOP) {
            newOrderSingle.setField(new StopPx(order.getStop()));
        } else if (type == OrderType.STOP_LIMIT) {
            newOrderSingle.setField(new Price(order.getLimit()));
            newOrderSingle.setField(new StopPx(order.getStop()));
        }

        if (order.getSide() == OrderSide.SHORT_SELL
                || order.getSide() == OrderSide.SHORT_SELL_EXEMPT) {
            newOrderSingle.setField(new LocateReqd(false));
        }

        newOrderSingle.setField(tifToFIXTif(order.getTIF()));
        return newOrderSingle;
    }
    public Side sideToFIXSide(OrderSide side) {
        return (Side) sideMap.getFirst(side);
    }

    public OrderSide FIXSideToSide(Side side) {
        return (OrderSide) sideMap.getSecond(side);
    }

    public OrdType typeToFIXType(OrderType type) {
        return (OrdType) typeMap.getFirst(type);
    }

    public OrderType FIXTypeToType(OrdType type) {
        return (OrderType) typeMap.getSecond(type);
    }

    public TimeInForce tifToFIXTif(OrderTIF tif) {
        return (TimeInForce) tifMap.getFirst(tif);
    }

    public OrderTIF FIXTifToTif(TimeInForce tif) {
        return (OrderTIF) typeMap.getSecond(tif);
    }

    static {
        sideMap.put(OrderSide.BUY, new Side(Side.BUY));
        sideMap.put(OrderSide.SELL, new Side(Side.SELL));
        sideMap.put(OrderSide.SHORT_SELL, new Side(Side.SELL_SHORT));
        sideMap.put(OrderSide.SHORT_SELL_EXEMPT, new Side(Side.SELL_SHORT_EXEMPT));
        sideMap.put(OrderSide.CROSS, new Side(Side.CROSS));
        sideMap.put(OrderSide.CROSS_SHORT, new Side(Side.CROSS_SHORT));
        typeMap.put(OrderType.MARKET, new OrdType(OrdType.MARKET));
        typeMap.put(OrderType.LIMIT, new OrdType(OrdType.LIMIT));
        typeMap.put(OrderType.STOP, new OrdType(OrdType.STOP_STOP_LOSS));
        typeMap.put(OrderType.STOP_LIMIT, new OrdType(OrdType.STOP_LIMIT));
        tifMap.put(OrderTIF.DAY, new TimeInForce(TimeInForce.DAY));
        tifMap.put(OrderTIF.IOC, new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL));
        tifMap.put(OrderTIF.OPG, new TimeInForce(TimeInForce.AT_THE_OPENING));
        tifMap.put(OrderTIF.GTC, new TimeInForce(TimeInForce.GOOD_TILL_CANCEL));
        tifMap.put(OrderTIF.GTX, new TimeInForce(TimeInForce.GOOD_TILL_CROSSING));

    }
}
