package com.github.nidorx.jtrade.broker.impl.metatrader;

import com.github.nidorx.jtrade.OHLC;
import com.github.nidorx.jtrade.TimeFrame;
import com.github.nidorx.jtrade.broker.Account;
import com.github.nidorx.jtrade.broker.Broker;
import com.github.nidorx.jtrade.broker.Instrument;
import com.github.nidorx.jtrade.broker.exception.TradeException;
import com.github.nidorx.jtrade.broker.trading.Order;
import com.github.nidorx.jtrade.broker.trading.Position;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Observable;

/**
 * Integração com o Metatrader usando socket
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class MetatraderBroker extends Broker {

    private final SocketClient client;

    public static void main(String[] args) throws IOException {
        MetatraderBroker metatraderBroker = new MetatraderBroker("127.0.0.1", 23456);
    }

    public MetatraderBroker(String host, int port) throws IOException {
        this.client = new SocketClient(host, port);
        
        /**
         * Ouve mensagens do servidor
         */
        client.addObserver((Observable o, Object arg) -> {
            System.out.println(arg);
        });
        
        client.connect();
        
//        client.send("quote");;
    }

    @Override
    public String getName() {
        return "Metatrader";
    }

    @Override
    public Account getAccount() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Account getAccountSummary(Instrument instrument) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected List<OHLC> requestTimeSeries(Instrument instrument, TimeFrame timeFrame, Instant start, Instant end) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Position getPosition(Instrument instrument) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Instrument> getInstruments() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Instrument getInstrument(String symbol) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double bid(Instrument instrument) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double ask(Instrument instrument) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double stopLevel(Instrument instrument) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double freezeLevel(Instrument instrument) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Order buy(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Order sell(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Order buyLimit(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Order sellLimit(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Order buyStop(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Order sellStop(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void modify(Order order, double price, double volume, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(Order order) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void modify(Position position, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close(Position position, double price, long deviation) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closePartial(Position position, double price, double volume, long deviation) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}