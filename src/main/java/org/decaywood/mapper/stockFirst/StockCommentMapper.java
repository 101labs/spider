package org.decaywood.mapper.stockFirst;

import com.fasterxml.jackson.databind.JsonNode;
import org.decaywood.entity.Stock;
import org.decaywood.entity.trend.StockTrend;
import org.decaywood.entity.trend.StockTrend.Period;
import org.decaywood.entity.trend.StockTrend.TrendBlock;
import org.decaywood.mapper.AbstractMapper;
import org.decaywood.test.Stock_yyb;
import org.decaywood.timeWaitingStrategy.TimeWaitingStrategy;
import org.decaywood.utils.EmptyObject;
import org.decaywood.utils.RequestParaBuilder;
import org.decaywood.utils.URLMapper;

import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;
import java.lang.Thread;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.decaywood.utils.DatabaseAccessor;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author: decaywood
 * @date: 2015/11/24 15:23
 */
public class StockCommentMapper extends AbstractMapper<Stock, Stock> {


    private Period period;
    private Date from;
    private Date to;

    public StockCommentMapper() throws RemoteException {
        this(Period.DAY, null, null);
    }


    public StockCommentMapper(Date from, Date to) throws RemoteException {
        this(Period.DAY, from, to);
    }

    public StockCommentMapper(Period period, Date from, Date to) throws RemoteException {
        this(null, period, from, to);
    }

    public StockCommentMapper(TimeWaitingStrategy strategy,
                                            Period period,
                                            Date from,
                                            Date to) throws RemoteException {
        super(strategy);
        if (from == null || to == null) {
            Calendar calendar = Calendar.getInstance();
            this.to = new Date();
            calendar.setTime(this.to);
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 5);
            this.from = calendar.getTime();
        } else {
            this.from = from;
            this.to = to;
        }
        if(this.to.before(this.from)) throw new IllegalArgumentException();
        this.period = period;
    }

    @Override
    public Stock mapLogic(Stock stock) throws Exception {

       // if(stock == null || stock == EmptyObject.emptyStock) return EmptyObject.emptyStock;

        String target = URLMapper.Comment_JSON.toString();
        String stockNo=stock.getStockNo();



        
        RequestParaBuilder builder_sum = new RequestParaBuilder(target)
                .addParameter("symbol",stockNo)
                .addParameter("count", "20")
                .addParameter("comment", "20")
                .addParameter("hl", "0")
                .addParameter("sort","time")
                .addParameter("page",1);

              

        URL url_sum = new URL(builder_sum.build());
        String json_sum = request(url_sum);
        JsonNode node_sum = mapper.readTree(json_sum);

        String total_count= node_sum.get("count").asText();
        int sum_count = Integer.parseInt(total_count);
        
        int page_number=100;

        System.out.println("total_page"+page_number);
        System.out.println("all need time :"+page_number*3/60+" minutes");


        for(int i=1;i<page_number;i++)
       {	
        String page=i+"";
        RequestParaBuilder builder = new RequestParaBuilder(target)
                .addParameter("symbol",stockNo)
                .addParameter("count", "20")
                .addParameter("comment", "20")
                .addParameter("hl", "0")
                .addParameter("sort","time")
                .addParameter("page",page);

              

        URL url = new URL(builder.build());
		String json = request(url);
        JsonNode node = mapper.readTree(json);
        String count= node.get("count").asText();
        //System.out.println(count);
       
        processStock(stock, node);
        

        
        Thread.currentThread().sleep(2500);
        System.out.println("page: "+i);

         //System.out.println("Collector: Network busy Retrying -> " + loopTime + " times");
                        updateCookie(webSite);
           //             this.strategy.waiting(loopTime++);

    }
        return stock;

    }


    private void processStock(Stock stock, JsonNode node) {

       
    try{
        //Connection connection = DatabaseAccessor.Holder.ACCESSOR.getConnection();
        InputStream inStream = Stock_yyb.class.getClassLoader().getResourceAsStream("database.properties");
        Properties prop = new Properties();
        prop.load(inStream);
        String database= prop.getProperty("database");
        String user = prop.getProperty("user");
        String password=prop.getProperty("password");


        DatabaseAccessor da= new DatabaseAccessor("com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/"+database+"?useUnicode=true&characterEncoding=utf-8",
                user,
                password);
        Connection connection=da.getConnection();

        StringBuilder builder = new StringBuilder();
         builder.append("insert IGNORE into stock_comment ")
                .append("(stock_id,create_time,herf,comment) ")
                .append("values (?, ?, ?, ?)");
                
        String sql = builder.toString();
        PreparedStatement statement = connection.prepareStatement(sql);
        
        



        JsonNode comments= node.get("list");
        for (JsonNode comment : comments) {
        	JsonNode text= comment.get("text");
            JsonNode created_at= comment.get("created_at");
            JsonNode id=comment.get("id");

            

            String unix_time=created_at.asText().substring(0,10);
            String raw_txt=text.asText();



            Document document = Jsoup.parse(raw_txt);
            String stock_id=stock.getStockNo().replace("SH","").replace("SZ","");
            String body=document.body().text();
            String herf="";

            //System.out.println("stock id: "+stock_id);
            System.out.println("created_time: "+ unix_time) ;
            //System.out.println("body text: "+body);
 


            Elements links_txt = document.select("a[href]");
             for (Element link_txt : links_txt) 
    {
          if(link_txt.text().contains("$"))
              herf=herf+" "+link_txt.text();

    }
           //System.out.println("herf: "+herf);
           

       
        statement.setString(1, stock_id);
        statement.setString(2, unix_time);
        statement.setString(3, herf);
        statement.setString(4, body);
        statement.execute();

      

    }

    DatabaseAccessor.Holder.ACCESSOR.returnConnection(connection);


       
    }catch(Exception e)
    {
        e.printStackTrace();
    }
   }
   
   





}
