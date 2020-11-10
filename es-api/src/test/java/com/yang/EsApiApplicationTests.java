package com.yang;

import com.alibaba.fastjson.JSON;
import com.yang.pojo.User;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class EsApiApplicationTests {
	//面向对象来操作
	@Autowired
	@Qualifier("restHighLevelClient")
	private RestHighLevelClient client;

	//索引的创建	Request,相当于PUT /yang_index7650
	@Test
	void testCreatedIndex() throws IOException {
		//1.创建索引请求
		CreateIndexRequest request = new CreateIndexRequest("yang_index");
		//2.执行创建请求,请求后获得相应
		CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
		System.out.println(createIndexResponse);
	}

	//获取索引,判断其是否存在
	@Test
	void testExistIndex() throws IOException {
		GetIndexRequest request = new GetIndexRequest("yang_index");
		boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
		System.out.println(exists);

	}

	//删除索引
	@Test
	void testDeleteIndex() throws IOException {
		DeleteIndexRequest request = new DeleteIndexRequest("yang_index");
		//删除
		AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
		System.out.println(delete.isAcknowledged());
	}

	//添加文档
	@Test
	void testAddDocument() throws IOException {
		//创建对象
		User user = new User("杨剑", 3);
		//创建请求
		IndexRequest request = new IndexRequest("yang_index");
		//规则	put /yang_index/_doc/1
		request.id("1");
		request.timeout(TimeValue.timeValueSeconds(1));
		request.timeout("1s");
		//将我们数据放入请求	json
		request.source(JSON.toJSONString(user), XContentType.JSON);
		//客户端发送请求,获取相应的结果
		IndexResponse index = client.index(request, RequestOptions.DEFAULT);
		System.out.println(index.toString());	//
		System.out.println(index.status());	//返回命令的状态 Created
	}

	// 获取文档，判断是否存在	get /index/_doc/1
	@Test
	void testExists() throws IOException {
		GetRequest getRequest = new GetRequest("yang_index", "1");
		//不获取返回的_source的上下文了
		getRequest.fetchSourceContext(new FetchSourceContext(false));
		getRequest.storedFields("_none_");
		boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
		System.out.println(exists);
	}

	//获取文档的信息
	@Test
	void testGetDocumentInfo() throws IOException {
		GetRequest getRequest = new GetRequest("yang_index", "1");
		GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
		String sourceAsString = getResponse.getSourceAsString();
		System.out.println(sourceAsString);	//打印文档的内容
		System.out.println(getResponse);	//这里返回的内容和命令是一样的
	}

	//更新文档信息
	@Test
	void testUpdateDocument() throws IOException{
		UpdateRequest updateRequest = new UpdateRequest("yang_index","1");
		updateRequest.timeout("1s");
		User user = new User("杨剑说", 18);
		updateRequest.doc(JSON.toJSONString(user),XContentType.JSON);
		UpdateResponse updateResponse = client.update(updateRequest,RequestOptions.DEFAULT);
		System.out.println(updateResponse.status());
	}

	//删除文档记录
	@Test
	void testDeleteDocument() throws IOException{
		DeleteRequest deleteRequest = new DeleteRequest("yang_index", "1");
		deleteRequest.timeout("1s");
		DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
		System.out.println(deleteResponse.status());

	}

	//特殊的，真实的项目一般都会批量插入数据
	@Test
	void testBulkRequest() throws IOException{
		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.timeout("10s");
		ArrayList<User> userList = new ArrayList<>();
		userList.add(new User("杨剑1",3));
		userList.add(new User("杨剑2",4));
		userList.add(new User("杨剑3",5));
		userList.add(new User("杨剑4",6));
		userList.add(new User("杨剑5",7));
		//批处理请求
		for (int i = 0; i < userList.size(); i++) {
			//批量更新和删除都可以在这里进行操作
			bulkRequest.add(new IndexRequest("yang_index")
					.id(""+(i+1))
					.source(JSON.toJSONString(userList.get(i)),XContentType.JSON)
			);
		}
		BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
		System.out.println(bulkResponse.hasFailures());	//是否失败，false代表插入成功
	}

	//搜索
	@Test
	void testSearch() throws IOException {
		SearchRequest searchRequest = new SearchRequest("yang_index");
		//构建搜索条件
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		//构建高亮
		//查询条件，我们可以使用QueryBuilders工具类进行快速匹配
		//精确匹配
//		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "杨剑1");
		//匹配所有
		MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
		sourceBuilder.query(matchAllQueryBuilder);
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(sourceBuilder);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse.getHits()));
		System.out.println("----------------------------");
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			System.out.println(hit.getSourceAsMap());
		}
	}
}
