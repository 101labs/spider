package org.decaywood.test;

import java.io.*;
import java.util.*;
import org.decaywood.collector.StockScopeHotRankCollector;
import org.decaywood.collector.StockSlectorBaseCollector;
import org.decaywood.entity.Stock;
import org.decaywood.entity.selectorQuota.BasicQuota;
import org.decaywood.entity.selectorQuota.MarketQuotationsQuota;
import org.decaywood.entity.selectorQuota.XueQiuQuota;
import org.junit.Assert;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.URLEncoder;
import java.sql.ResultSet;
import org.decaywood.utils.DatabaseAccessor;
import java.sql.Connection;
import java.sql.Statement;


import org.decaywood.mapper.stockFirst.StockCommentMapper;

/**
 * @author: decaywood
 * @date: 2015/11/25 10:49
 */
public class Stock_yyb {





  public static void main(String [] args) 
  {
   try{

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




 //Connection connection = DatabaseAccessor.Holder.ACCESSOR.getConnection();
        
      try{
        List<String> ids= new ArrayList<String>();  
        //String sql = "select stock_id from stock_entity where stock_id not in (select distinct stock_id FROM stock_comment)";
        String sql = "select stock_id from stock_entity";
        Statement statement = connection.createStatement();
        ResultSet result=statement.executeQuery(sql);
        while(result.next())
        {
         String id=result.getString("stock_id");
         //System.out.println(id.substring(0,2));
         if(id.substring(0,2).contains("60"))
         {
           id="SH"+id;
            ids.add(id);
         }
         if(id.substring(0,2).contains("00"))
         {
           id="SZ"+id;
           ids.add(id);
         }

        
        }

        da.returnConnection(connection);
      
       int flag=0;
       for(String stock_id:ids)
       {
        if(stock_id.equals("SZ002001"))
          flag=1;
        if(flag>0)
        {
        System.out.println("stock id : "+stock_id);
        StockCommentMapper a = new StockCommentMapper();
        Stock s =new Stock(stock_id,stock_id);
        a.apply(s);

        }


       }

}catch(Exception e)
{
  e.printStackTrace();
}










/*
       StockScopeHotRankCollector collector1 = new StockScopeHotRankCollector(StockScopeHotRankCollector.Scope.SH_SZ _WITHIN_1_HOUR);
        StockScopeHotRankCollector collector24;
        collector24 = new StockScopeHotRankCollector(StockScopeHotRankCollector.Scope.SH_SZ_WITHIN_24_HOUR);
         List<Stock> stocks1 = collector1.get();
        List<Stock> stocks24 = collector24.get();
        
       for (Stock stock : stocks1) {
         System.out.println(stock.getStockName()+":"+stock.getChange());
       }
       System.out.println("111111");
      for (Stock stock : stocks24) {
           System.out.println(stock.getStockName()+":"+stock.getChange());
       }



    
      String stock_id="BAIDU";
      int page=2;
      // String url="https://xueqiu.com/statuses/search.json?count=10&comment=10&symbol="+stock_id+"&hl=0&source=user&sort=time&page="+page+"";
       String url="https://xueqiu.com/statuses/search.json?count=10&comment=10&symbol=FENG&hl=0&source=user&sort=time&page=2";
       URLEncoder.encode(url,"UTF-8");
       Document doc2 = Jsoup.connect(url).get();
       System.out.println("ssssss");
       System.out.println(doc2.toString());

*/


      }
      catch(Exception e)
      {
      	e.printStackTrace();
      }


  }




}
