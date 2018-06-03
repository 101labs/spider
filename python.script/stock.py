#coding:utf8
import sys
reload(sys)
sys.setdefaultencoding('utf-8')
import tushare as ts
import MySQLdb



stock_info=ts.get_stock_basics()



stock_id=[]
stock_name=[]
for i in stock_info.index:
        stock_id.append(i)
for j in stock_info.name:
		stock_name.append(j)



db = MySQLdb.connect(host='localhost',port=3306,user='root',passwd='sangouqifei',db='STOCK',charset='utf8')
cursor = db.cursor()
#command="insert into stock_entity(stock_id,stock_name) VALUES(\"123456\",\"洋\")"





for i in range(0,len(stock_id)):
	print stock_id[i],"  ",stock_name[i]
	command="insert into stock_entity (stock_id,stock_name) VALUES(\""+stock_id[i]+"\",\""+stock_name[i]+"\")"
	#print command
	cursor.execute(command)
	
	
	#print "success"






db.commit()
db.close()


