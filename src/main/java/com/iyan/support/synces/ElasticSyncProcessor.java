package com.iyan.support.synces;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.iyan.support.utils.ReadMysqlToESResource;
import com.alibaba.otter.canal.protocol.Message;

/**
 * 
* @ClassName: ElasticSyncProcessor
* @Description: Mysql同步到Elastic业务处理
* @author zhangxj
* @date 2018年11月12日 下午4:53:29
* @version V3.0.1
 */
public class ElasticSyncProcessor {
	protected static Logger logger = LoggerFactory.getLogger(ElasticSyncProcessor.class);
//	es的索引名
	private static String esName ="order_main";
//	同步过滤匹配
	private static String subscribe = "iyan_db.*\\.*";
//	canal服务ip地址
	private static String canalIp ="127.0.0.1";
//	canal服务端口后
	private static int canalPort =11111;
//	canal服务目的
	private static String canalDestinations ="52cx";
//	canal用户名，密码
	private static String username ="";
	private static String password ="";
//	es的ip地址和端口
	private static String esIP ="172.168.10.52:9300,172.168.10.53:9300";
//	es的集群名
	private static String clusterName ="cluster.name";
	private static String esClusterName ="iyan-es-cluster";
	
	
	private static TransportClient client;
	
	private static  Map<String,SyncRule> syncRules = new HashMap<String,SyncRule>();
	
	
	private static void initMysqlToESConfigPro(){
		logger.debug("------读取配置文件信息----start---");
		esName = ReadMysqlToESResource.get("iyan.mysql_to_es.esname", "order_main");
		canalIp = ReadMysqlToESResource.get("iyan.mysql_to_es.canal.ip", "127.0.0.1");
		canalPort = ReadMysqlToESResource.getInt("iyan.mysql_to_es.canal.port", 12345);
		canalDestinations = ReadMysqlToESResource.get("iyan.mysql_to_es.destinations", "iyan");
		username = ReadMysqlToESResource.get("iyan.mysql_to_es.username", "");
		password = ReadMysqlToESResource.get("iyan.mysql_to_es.password", "");
		esIP = ReadMysqlToESResource.get("iyan.mysql_to_es.es.ip", "172.168.10.52:9300,172.168.10.53:9300");
		clusterName = ReadMysqlToESResource.get("iyan.mysql_to_es.es.cluster_name", "cluster.name");
		esClusterName = ReadMysqlToESResource.get("iyan.mysql_to_es.es.es_cluster_name", "52cx-es-cluster");
		subscribe = ReadMysqlToESResource.get("iyan.mysql_to_es.subscribe", "iyan_db.o_order_main");
		String esIndex = "", esType = "", esField = "", dbTables ="";
		String temp ="";
		String[] esNameStrs = esName.split(",");
		logger.info("============ [ " + esName + " ]");
		SyncRule rule;
		for(int i = 0; i < esNameStrs.length; i++){
			temp = "."+esNameStrs[i];
			logger.info("=====[" + "iyan.mysql_to_es.esIndex"+temp + " ]");
			esIndex = ReadMysqlToESResource.get("iyan.mysql_to_es.esIndex"+temp, "nulls");
//			判断es同步是否有未配置，需要进行下一条读取
			if("nulls".equals(esIndex)){
				logger.error("iyan.mysql_to_es.esIndex"+temp+";数据未进行配置");
				continue;
			}
			esType = ReadMysqlToESResource.get("iyan.mysql_to_es.esType"+temp, "order_main");
			esField = ReadMysqlToESResource.get("iyan.mysql_to_es.esField"+temp, "id");
			rule = new SyncRule(esIndex.toLowerCase() , esType.toLowerCase() , esField.toLowerCase() );
			dbTables = ReadMysqlToESResource.get("iyan.mysql_to_es.table"+temp, "w2cx_db#o_order_main");
			String[] dbTablesStr = dbTables.split(",");
			for(int j = 0; j < dbTablesStr.length; j++){
				syncRules.put(dbTablesStr[j], rule);
			}
		}
		logger.debug("------读取配置文件信息----end---");
	}

	public static void main(String args[]) throws Exception {
//		初始化配置信息
		initMysqlToESConfigPro();
		Settings settings = Settings.builder().put(clusterName, esClusterName).build();
		client = new PreBuiltTransportClient(settings);
//		获取es的ip地址，进行加载es地址
		String[] esIpStr = esIP.split(",");
		for(int i = 0; i < esIpStr.length; i++){
			if(esIpStr[i].contains(":")){
				client.addTransportAddress(new InetSocketTransportAddress(
						InetAddress.getByName(esIpStr[i].split(":")[0]), Integer.parseInt(esIpStr[i].split(":")[1])));
			}
		}
		// 创建链接
		CanalConnector connector = CanalConnectors
				.newSingleConnector(new InetSocketAddress(canalIp, canalPort), canalDestinations, username, password);
		int batchSize = 5000;
		int emptyCount = 0;
		try {
			logger.debug("-----subscribe----:"+subscribe);
			connector.connect();
			connector.subscribe(subscribe);
			connector.rollback();
			while (true) {
				Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
				long batchId = message.getId();
				int size = message.getEntries().size();
				if (batchId == -1 || size == 0) {
					emptyCount++;
					logger.debug("empty count : " + emptyCount);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					emptyCount = 0;
					syncElasticDocuments( message.getEntries() );
				}
				connector.ack(batchId); // 提交确认
				// connector.rollback(batchId); // 处理失败, 回滚数据
			}
		} finally {
			connector.disconnect();
			client.close();
//			超时连不上时  可以考虑短信通知
			logger.debug("empty too many times, exit");
		}
	}

	/**
	 * 同步Mysql binLog日志
	 * @param entrys
	 */
	private static void syncElasticDocuments(List<Entry> entrys) {
		for (Entry entry : entrys) {
			if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN
					|| entry.getEntryType() == EntryType.TRANSACTIONEND) {
				continue;
			}

			RowChange rowChage = null;
			try {
				rowChage = RowChange.parseFrom(entry.getStoreValue());
			} catch (Exception e) {
				throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
						e);
			}
			EventType eventType = rowChage.getEventType();
			logger.debug(String.format("================> binlog[%s:%s] , name[%s,%s] , eventType : %s",
					entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
					entry.getHeader().getSchemaName(), entry.getHeader().getTableName(), eventType));

			for (RowData rowData : rowChage.getRowDatasList()) {
//				获取规则，判断该变更数据是否已经配置规则
				try {
					SyncRule syncRule = syncRules.get(entry.getHeader().getSchemaName() + "-" + entry.getHeader().getTableName());
					if(syncRule == null) {
						logger.error("SyncRule Can Not Be Null!");
						break;
					}
					if (eventType == EventType.DELETE) {
						Map<String, Object> node = getDeleteNode(rowData.getBeforeColumnsList());

						DeleteResponse deleteReponse = deleteElasticDocument(client, syncRule, (String)node.get(syncRule.getField()));
						if(deleteReponse != null) {
							logger.debug("----deleteReponse----:"+deleteReponse.toString());
							logger.debug(""+deleteReponse.status().getStatus());
							logger.debug(deleteReponse.getId());
						}
					} else if (eventType == EventType.INSERT) {
						Map<String, Object> node = getInsertNode(rowData.getAfterColumnsList());
						IndexResponse indexResponse = crerateElasticDocument(client, syncRule,  (String)node.get(syncRule.getField()), node);
						if(indexResponse != null) {
							logger.debug("----indexResponse----:"+indexResponse.toString());
							logger.debug(""+indexResponse.status().getStatus());
							logger.debug(indexResponse.getId());
						}
					} else  if(eventType == EventType.UPDATE) {
						Map<String, Object> node = getUpdateNode(rowData.getAfterColumnsList());
						UpdateResponse updateResponse = updateElasticDocument(client, syncRule, (String)node.get(syncRule.getField()), node);
						if(updateResponse != null) {
							logger.debug("----updateResponse----:"+updateResponse.toString());
							logger.debug(""+updateResponse.status().getStatus());
							logger.debug(updateResponse.getId());
						}
					}
				}catch (Exception e){
					logger.error("修改信息报错" + e.toString());
					continue;
				}

			}
		}
	}
	
	private static Map<String,Object> getInsertNode(List<Column> columns) {
		Map<String, Object> node = new HashMap<String, Object>();
		for (Column column : columns) {
			node.put(column.getName(), column.getValue());
		}
		return node;
	}
	
	private static Map<String,Object> getDeleteNode(List<Column> columns) {
		Map<String, Object> node = new HashMap<String, Object>();
		for (Column column : columns) {
			if(column.getIsKey()) {
				node.put(column.getName(), column.getValue());
			}
		}
		return node;
	}
	
	private static Map<String,Object> getUpdateNode(List<Column> columns) {
		Map<String, Object> node = new HashMap<String, Object>();
		for (Column column : columns) {
			if(column.getUpdated() || column.getIsKey()) {
				node.put(column.getName(), column.getValue());
			}
		}
		return node;
	}
	/**
	 * 创建文档
	 * @param client
	 * @param syncRule
	 * @param docID
	 * @param values
	 * @return
	 */
	private static IndexResponse crerateElasticDocument(TransportClient client, SyncRule syncRule, String docID, Map<String, Object> values) {
		try {
			return client.prepareIndex().setIndex(syncRule.getIndex()).setType(syncRule.getType()).setId(docID).setSource(jsonBuilder().map(values)).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 删除文档
	 * @param client
	 * @param syncRule
	 * @param docID
	 * @return
	 */
	private static DeleteResponse deleteElasticDocument(TransportClient client, SyncRule syncRule, String docID) {
		return client.prepareDelete(syncRule.getIndex(), syncRule.getType(), docID).get();
	}
	
	/**
	 * 更新文档
	 * @param client
	 * @param syncRule
	 * @param docID
	 * @param values
	 * @return
	 */
	private static UpdateResponse updateElasticDocument(TransportClient client, SyncRule syncRule, String docID, Map<String, Object> values) {
		try {
			return client.update( new UpdateRequest().index( syncRule.getIndex() ).type( syncRule.getType() ).id( docID ).doc(jsonBuilder().map( values )) ).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

}
