package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
//import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


public class MovieRecommender {
    private String resource_path;
    private int totalproducts;
    private int totalreviews;
    private int totalusers;
    private BiMap<String,Integer> productHash;
    private BiMap<String,Integer> usersHash;
    public MovieRecommender(String resource_path)throws IOException{
        this.resource_path = resource_path;
        this.totalproducts = 0;
        this.totalreviews = 0;
        this.totalusers = 0; 
        this.productHash = HashBiMap.create();
        this.usersHash = HashBiMap.create();
        readReviewsFile(resource_path);
    }
    /*
        productId
        UserId
        profileName
        helpfulness
        score
        time
        summary
        text:

        productId
    */
    private void readReviewsFile(String path) throws IOException{
        File result = new File("resultados.csv");
        FileWriter fw = new FileWriter(result);
        int current_user = 0;
        int current_product = 0;
        InputStream stream = new GZIPInputStream(new FileInputStream(path));
        //BufferedReader br = new BufferedReader(new FileReader(path));
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String line;
        

        while((line = br.readLine()) != null){
            if(line.startsWith("product/productId:")){
                //System.out.println("hola mundo");
                String product_id = line.split(" ")[1];
                //System.out.println("xddd "+product_id);
                //System.out.println("asdasd  "+ this.productHash.containsKey(product_id));
                if(this.productHash.containsKey(product_id) != true){
                    this.totalproducts++;
                    this.productHash.put(product_id, this.totalproducts);
                    current_product = this.totalproducts;
                }else{
                    current_product = this.productHash.get(product_id);
                }
                continue;
            }
            if(line.startsWith("review/userId")){
                String user_id = line.split(" ")[1];
                if(!this.usersHash.containsKey(user_id)){
                    this.totalusers++;
                    this.usersHash.put(user_id,this.totalusers);
                    current_user = this.totalusers;
                }else{
                    current_user = this.usersHash.get(user_id);
                }
            }
            if(line.startsWith("review/score")){
                String score = line.split(" ")[1];
                fw.write(current_user+","+current_product+","+score+"\n");
                this.totalreviews++;
            }
        }
        fw.close();
        br.close();

        //B003AI2VGA
    }




	public int getTotalReviews() {
		return totalreviews;
	}
	public int getTotalProducts() {
		return totalproducts;
	}
	public int getTotalUsers() {
		return totalusers;
	}
	public List<String> getRecommendationsForUser(String user) throws IOException,TasteException {
		DataModel model = new FileDataModel(new File("resultados.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        int user_id = this.usersHash.get(user);

        List<RecommendedItem> recommendations = recommender.recommend(user_id,3);

        List<String> recommendationsString = new ArrayList<String>();
        
        BiMap<Integer, String> product_hash_inverse = this.productHash.inverse();

        for(RecommendedItem recommendation: recommendations){
            recommendationsString.add(product_hash_inverse.get((int)recommendation.getItemID()));
        }
        return recommendationsString;        
	}
}
