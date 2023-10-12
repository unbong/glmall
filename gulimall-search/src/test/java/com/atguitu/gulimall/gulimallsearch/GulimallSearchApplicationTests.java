package com.atguitu.gulimall.gulimallsearch;

//import org.junit.jupiter.api.Test;
import com.alibaba.fastjson.JSON;
import com.atguitu.gulimall.gulimallsearch.config.GulimallElasticSearchConfig;
import com.atguitu.gulimall.gulimallsearch.entity.Users;
import lombok.extern.slf4j.Slf4j;
//import org.apache.lucene.index.Terms;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketCollector;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j

public class GulimallSearchApplicationTests  {

    @Autowired
    private RestHighLevelClient client;


    @Test
    public void searchDemo() throws IOException {

        // _search
        SearchRequest  searchRequest = new SearchRequest("customer");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // query
        //searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        //
        searchRequest.source(searchSourceBuilder);


        String[] includeFileds = new String[] {"firstname","balance" };
        String[] excludeFileds = new String[] {"lastname"};

//        searchSourceBuilder.fetchSource(includeFileds,excludeFileds);

        searchSourceBuilder.aggregation(AggregationBuilders.terms("ageTerm").field("age")
        .subAggregation(AggregationBuilders.terms("genTerm").field("gender.keyword")));


        SuggestionBuilder  termSugBuilder = SuggestBuilders.termSuggestion("address").text("mill");
        SuggestBuilder sugBuild = new SuggestBuilder();
        sugBuild.addSuggestion("suggest_addr",termSugBuilder);
        searchSourceBuilder.suggest(sugBuild);

        SearchResponse res = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        searchSourceBuilder.sort(new FieldSortBuilder("balance").order(SortOrder.ASC));


        for (SearchHit hit : res.getHits()) {
            Map<String, Object> ress = hit.getSourceAsMap();
            log.info(ress.get("age").toString());
        }
//
//       = res.getAggregations().get("ageTerm");
//        Terms.Bucket buk = sdfsd.getBucketByKey("31");
//        log.info("ageTerm", buk.getKeyAsNumber().toString());
//        Terms ss = buk.getAggregations().get("genTerm");
//        log.info( "genTerm",ss.getBucketByKey("M").toString() );
//
        Map<String, Aggregation> aggMap = res.getAggregations().getAsMap();
        Terms ageTerm = (Terms) aggMap.get("ageTerm");
        log.info("ageTerm", ageTerm.getBucketByKey("31").getKeyAsNumber().intValue());
    }

    @Test
    public void indexDemo() throws IOException {
        IndexRequest request = new IndexRequest("users");
        request.id("1");
        Users user = new Users();
        user.setAge(20);
        user.setUserName("zhangshan");
        user.setGender("男");
        String json = JSON.toJSONString(user);

        request.source(json, XContentType.JSON);

        client.index(request, GulimallElasticSearchConfig.COMMON_OPTIONS);
    }


    @Test
    public void getDemo() throws IOException {
        GetRequest request = new GetRequest("users", "1");


        GetResponse res = client.get(request, GulimallElasticSearchConfig.COMMON_OPTIONS);

        System.out.println(res.toString());
    }


    @Test
    public void contextLoads() {
        System.out.println(client);

    }

}
