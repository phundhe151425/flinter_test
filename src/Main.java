import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Map<String, CampaignAgg> campaignMap = new TreeMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader("ad_data.csv"))) {

            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {

                String[] cols = line.split(",");

                String campaignId = cols[0];
                long impressions = Long.parseLong(cols[2]);
                long clicks = Long.parseLong(cols[3]);
                double spend = Double.parseDouble(cols[4]);
                long conversions = Long.parseLong(cols[5]);

                campaignMap
                        .computeIfAbsent(campaignId, k -> new CampaignAgg())
                        .add(impressions, clicks, spend, conversions);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // total Aggregate data by campaign_id
        printCampaignAnalytics(campaignMap);
        writeCampaignToCSV(campaignMap, "all.csv");


        // top 10 CTR
        Map<String, CampaignAgg> getTop10HighestCTR = getTop10HighestCTR(campaignMap);
        printCampaignAnalytics(getTop10HighestCTR);
        writeCampaignToCSV(getTop10HighestCTR, "top10_ctr.csv");

        // top 10 CTR
        Map<String, CampaignAgg> getTop10LowestCPA = getTop10LowestCPA(campaignMap);
        printCampaignAnalytics(getTop10LowestCPA);
        writeCampaignToCSV(getTop10LowestCPA, "top10_cpa.csv");
    }



    public static Map<String, CampaignAgg> getTop10HighestCTR(Map<String, CampaignAgg> campaignMap) {

        return campaignMap.entrySet()
                .stream()
                .sorted((e1, e2) -> Double.compare(
                        e2.getValue().getCTR(),
                        e1.getValue().getCTR()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }


    public static Map<String, CampaignAgg> getTop10LowestCPA(Map<String, CampaignAgg> campaignMap) {

        return campaignMap.entrySet()
                .stream()
                .filter(e -> e.getValue().getCPA() != null) // bỏ CPA null
                .sorted((e1, e2) -> Double.compare(
                        e1.getValue().getCPA(),
                        e2.getValue().getCPA()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }


    public static void writeCampaignToCSV(Map<String, CampaignAgg> campaignMap, String filePath) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            // header
            writer.write("campaign_id,total_impressions,total_clicks,total_spend,total_conversions,CTR,CPA");
            writer.newLine();

            for (Map.Entry<String, CampaignAgg> entry : campaignMap.entrySet()) {

                String campaignId = entry.getKey();
                CampaignAgg agg = entry.getValue();

                writer.write(
                        campaignId + "," +
                                agg.impressions + "," +
                                agg.clicks + "," +
                                agg.spend + "," +
                                agg.conversions + "," +
                                agg.getCTR() + "," +
                                agg.getCPA()
                );

                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printCampaignAnalytics(Map<String, CampaignAgg> campaignMap) {

        long totalRecords = 0;
        for (Map.Entry<String, CampaignAgg> entry : campaignMap.entrySet()) {

            String campaignId = entry.getKey();
            CampaignAgg agg = entry.getValue();
            totalRecords += agg.records;
            System.out.println(
                    campaignId +
                            " impressions=" + agg.impressions +
                            " clicks=" + agg.clicks +
                            " spend=" + agg.spend +
                            " conversions=" + agg.conversions +
                            " CTR=" + agg.getCTR() +
                            " CPA=" + agg.getCPA() +
                            " records=" + agg.getRecords()
            );
        }
        System.out.println("totalRecords=" + totalRecords);
    }
}