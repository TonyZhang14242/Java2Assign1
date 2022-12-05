import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MovieAnalyzer {
    static class Movie{
        //Poster_Link,Series_Title,Released_Year,Certificate,Runtime,Genre,IMDB_Rating,Overview,Meta_score,Director,Star1,Star2,Star3,Star4,No_of_Votes,Gross
        String Poster_link;
        String Series_Title;
        int Released_Year;
        String Certificate;
        int Runtime;
        List<String> Genre;
        float IMDB_Rating;
        String Overview;
        int Meta_score;
        String Director;
        String star1,star2,star3,star4;
        int No_of_votes;
        long Gross;
        Movie(String Poster_link, String Series_Title, int Released_Year, String Certificate, int Runtime, List<String> Genre, float IMDB_Rating, String Overview, int Meta_score,
        String Director, String star1, String star2, String star3, String star4, int No_of_votes, long Gross){
            this.Poster_link = Poster_link;
            this.Series_Title = Series_Title;
            this.Released_Year = Released_Year;
            this.Certificate = Certificate;
            this.Runtime = Runtime;
            this.Genre = Genre;
            this.IMDB_Rating = IMDB_Rating;
            this.Overview = Overview;
            this.Meta_score = Meta_score;
            this.Director = Director;
            this.star1 = star1;
            this.star2 = star2;
            this.star3 = star3;
            this.star4 = star4;
            this.No_of_votes = No_of_votes;
            this.Gross = Gross;
        }

        int getReleased_Year(){
            return Released_Year;
        }



    }

    static List<Movie> movieList = new ArrayList<>();
    public MovieAnalyzer(String dataset_path){
        try (BufferedReader br = Files.newBufferedReader(Paths.get(dataset_path))) {
            //String DELIMITER = ",";
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] columns = line.trim().split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                //System.out.println(Arrays.toString(columns));
                try {
                    String Poster_link = (columns[0] == null || columns[0].equals("")) ? "" : columns[0].replaceAll("\"", "");
                    String Series_Title = (columns[1] == null || columns[1].equals("")) ? "" : columns[1].replaceAll("\"", "");
                    int Released_Year = Integer.parseInt(columns[2]);
                    String Certificate = (columns[1] == null || columns[1].equals("")) ? "" : columns[3];
                    int Runtime = Integer.parseInt(columns[4].replace(" min", ""));
                    List<String> Genre = new ArrayList<>();
                    String[] geners = columns[5].replaceAll("\"", "").split(", ");
                    Genre.addAll(Arrays.asList(geners));
                    float IMDB_Rating = Float.parseFloat(columns[6]);
                    String Overview = columns[7]; //.replaceAll("\"","");
                    if (Overview.startsWith("\"")){
                        Overview = Overview.substring(1);
                    }
                    if (Overview.endsWith("\"")){
                        Overview = Overview.substring(0, Overview.length() - 1);
                    }
                    //Overview=Overview.replaceAll("\"\"","\"");
                    int Meta_score = (columns[8] == null || columns[8].equals("")) ? -1 : Integer.parseInt(columns[8]);
                    String Director = columns[9];
                    String star1 = columns[10];
                    String star2 = columns[11];
                    String star3 = columns[12];
                    String star4 = columns[13];
                    int No_of_votes = Integer.parseInt(columns[14]);
                    long Gross = (columns[15] == null || columns[15].equals("")) ? -1 : Long.parseLong(columns[15].replaceAll(",", "").replaceAll("\"", ""));
                    Movie movie = new Movie(Poster_link, Series_Title, Released_Year, Certificate, Runtime, Genre, IMDB_Rating, Overview, Meta_score, Director, star1, star2, star3, star4, No_of_votes, Gross);
                    movieList.add(movie);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public Map<Integer, Integer> getMovieCountByYear(){
        Stream<Movie> movieStream = movieList.stream();
        Map<Integer, List<Movie>> movbyyear = movieStream.collect(Collectors.groupingBy(Movie::getReleased_Year));
        Map<Integer, Integer> res = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        for (Map.Entry<Integer, List<Movie>> entry : movbyyear.entrySet()){
            res.put(entry.getKey(), entry.getValue().size());
        }
        return res;
    }

    public Map<String, Integer> getMovieCountByGenre(){
        List<String> geners = new ArrayList<>();
        for (int i = 0; i < movieList.size(); i++){
            for (int j = 0; j < movieList.get(i).Genre.size(); j++){
                if (!geners.contains(movieList.get(i).Genre.get(j)))
                    geners.add(movieList.get(i).Genre.get(j));
            }
        }
        //System.out.println(Arrays.toString(geners.toArray()));
        Map<String, Integer> res = new LinkedHashMap<>();
        Comparator<Map.Entry<String, Integer>> valueComparator = new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o2.getValue() - o1.getValue() == 0) ? o1.getKey().compareTo(o2.getKey()) : o2.getValue() - o1.getValue();
            }
        };
        Map<String, List<Movie>> reslist = new HashMap<>();
        for (int i = 0; i < geners.size(); i++){
            reslist.put(geners.get(i), new ArrayList<>());
        }
        for (int i = 0; i < movieList.size(); i++){
            for (int j = 0; j < movieList.get(i).Genre.size(); j++){
                reslist.get(movieList.get(i).Genre.get(j)).add(movieList.get(i));
            }
        }

        for (Map.Entry<String, List<Movie>> entry : reslist.entrySet()){
            res.put(entry.getKey(), entry.getValue().size());
        }
        List<Map.Entry<String, Integer>> tmp = new ArrayList<>(res.entrySet());
        Collections.sort(tmp, valueComparator);
        res.clear();
        for (Map.Entry<String, Integer> entry : tmp){
            res.put(entry.getKey(), entry.getValue());
        }
        res.entrySet().forEach(System.out::println);
        return res;
    }

    public Map<List<String>, Integer> getCoStarCount(){
        Map<List<String>, Integer> res = new LinkedHashMap<>();
        for (int i = 0; i < movieList.size(); i++){
            List<String> list = new ArrayList<>();
            list.add(movieList.get(i).star1);
            list.add(movieList.get(i).star2);
            Collections.sort(list);
            if (res.containsKey(list)){
                res.replace(list, res.get(list)+1);
            }
            else res.put(list, 1);
            list = new ArrayList<>();
            list.add(movieList.get(i).star1);
            list.add(movieList.get(i).star3);
            Collections.sort(list);
            if (res.containsKey(list)){
                res.replace(list, res.get(list)+1);
            }
            else res.put(list, 1);
            list = new ArrayList<>();
            list.add(movieList.get(i).star1);
            list.add(movieList.get(i).star4);
            Collections.sort(list);
            if (res.containsKey(list)){
                res.replace(list, res.get(list) + 1);
            }
            else res.put(list, 1);
            list = new ArrayList<>();
            list.add(movieList.get(i).star2);
            list.add(movieList.get(i).star3);
            Collections.sort(list);
            if (res.containsKey(list)){
                res.replace(list, res.get(list) + 1);
            }
            else res.put(list, 1);
            list = new ArrayList<>();
            list.add(movieList.get(i).star2);
            list.add(movieList.get(i).star4);
            Collections.sort(list);
            if (res.containsKey(list)){
                res.replace(list, res.get(list) + 1);
            }
            else res.put(list, 1);
            list = new ArrayList<>();
            list.add(movieList.get(i).star3);
            list.add(movieList.get(i).star4);
            Collections.sort(list);
            if (res.containsKey(list)){
                res.replace(list, res.get(list)+1);
            }
            else res.put(list, 1);
        }
        //res.entrySet().forEach(System.out::println);
        //res.entrySet().forEach(e->{if (e.getValue()==4) System.out.println(e);});
        return res;
    }

    public List<String> getTopMovies(int top_k, String by){
        List<Movie> tmp = new ArrayList<>();
        tmp.addAll(movieList);
        if (by.equals("runtime")){
            Comparator<Movie> movieComparator = new Comparator<Movie>() {
                @Override
                public int compare(Movie o1, Movie o2) {
                    return (o2.Runtime - o1.Runtime == 0) ? o1.Series_Title.compareTo(o2.Series_Title) : o2.Runtime - o1.Runtime;
                }
            };
            Collections.sort(tmp, movieComparator);
            List<String> res1 = new ArrayList<>();
            for (int i = 0; i < top_k; i++) {
                res1.add(tmp.get(i).Series_Title);
            }
            return res1;
        }
        else if (by.equals("overview")){
            Comparator<Movie> movieComparator = new Comparator<Movie>() {
                @Override
                public int compare(Movie o1, Movie o2) {

                    return (o2.Overview.length() - o1.Overview.length() == 0) ? o1.Series_Title.compareTo(o2.Series_Title) : o2.Overview.length() - o1.Overview.length();
                }
            };
            Collections.sort(tmp,movieComparator);
            List<String> res1 = new ArrayList<>();
            for (int i = 0; i < top_k; i++) {
                res1.add(tmp.get(i).Series_Title);
            }
            for (int i = 0; i < res1.size(); i++){
                System.out.println(res1.get(i) + tmp.get(i).Overview.length());
            }
            return res1;
        }
        return null;
    }

    public List<String> getTopStars(int top_k, String by){
        Map<String, List<Movie>> startomovies = new LinkedHashMap<>();
        for (int i = 0; i < movieList.size(); i++){
            if (startomovies.containsKey(movieList.get(i).star1)){
                startomovies.get(movieList.get(i).star1).add(movieList.get(i));
            }
            else {
                startomovies.put(movieList.get(i).star1, new ArrayList<>());
                startomovies.get(movieList.get(i).star1).add(movieList.get(i));
            }
            if (startomovies.containsKey(movieList.get(i).star2)){
                startomovies.get(movieList.get(i).star2).add(movieList.get(i));
            }
            else {
                startomovies.put(movieList.get(i).star2, new ArrayList<>());
                startomovies.get(movieList.get(i).star2).add(movieList.get(i));
            }
            if (startomovies.containsKey(movieList.get(i).star3)){
                startomovies.get(movieList.get(i).star3).add(movieList.get(i));
            }
            else {
                startomovies.put(movieList.get(i).star3, new ArrayList<>());
                startomovies.get(movieList.get(i).star3).add(movieList.get(i));
            }
            if (startomovies.containsKey(movieList.get(i).star4)){
                startomovies.get(movieList.get(i).star4).add(movieList.get(i));
            }
            else {
                startomovies.put(movieList.get(i).star4, new ArrayList<>());
                startomovies.get(movieList.get(i).star4).add(movieList.get(i));
            }
        }
        if (by.equals("rating")){
            Comparator<Map.Entry<String, Double>> comparator = new Comparator<Map.Entry<String, Double>>() {
                @Override
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    return (o2.getValue().compareTo(o1.getValue()) == 0.0) ? o1.getKey().compareTo(o2.getKey()) : o2.getValue().compareTo(o1.getValue());
                }
            };
            //Map<String, Double> startoavg=new LinkedHashMap<>();
            List<Map.Entry<String, Double>> tmp = new ArrayList<>();
            for (Map.Entry<String, List<Movie>> entry : startomovies.entrySet()){
                double s = 0;
                for (int i = 0; i < entry.getValue().size(); i++){
                    s += entry.getValue().get(i).IMDB_Rating;
                }
                double avg = s / entry.getValue().size();
                tmp.add(Map.entry(entry.getKey(),avg));
            }

            Collections.sort(tmp, comparator);
            List<String> res = new ArrayList<>();
            for (int i = 0; i < top_k; i++){
                res.add(tmp.get(i).getKey());
                System.out.println(res.get(i) + tmp.get(i).getValue());
            }

            return res;
        }
        else if (by.equals("gross")){
            Comparator<Map.Entry<String, Double>> comparator = new Comparator<Map.Entry<String, Double>>() {
                @Override
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    return (o2.getValue().compareTo(o1.getValue()) == 0) ? o1.getKey().compareTo(o2.getKey()) : o2.getValue().compareTo(o1.getValue());
                }
            };
            //Map<String, Double> startoavg=new LinkedHashMap<>();
            List<Map.Entry<String, Double>> tmp = new ArrayList<>();
            for (Map.Entry<String, List<Movie>> entry : startomovies.entrySet()){
                double s = 0;
                int cnt = 0;
                for (int i = 0; i < entry.getValue().size(); i++){
                    if (entry.getValue().get(i).Gross != -1){
                        s += entry.getValue().get(i).Gross;
                        //System.out.println(entry.getValue().get(i).Gross);
                        cnt++;
                    }
                }
                double avg = s / cnt;
                //System.out.println(cnt+" "+avg);
                if (cnt != 0)
                    tmp.add(Map.entry(entry.getKey(), avg));
            }

            Collections.sort(tmp, comparator);
            List<String> res = new ArrayList<>();
            for (int i = 0; i < top_k; i++){
                res.add(tmp.get(i).getKey());
                System.out.println(res.get(i) + tmp.get(i).getValue());
            }

            return res;
        }

        return null;
    }

    public List<String> searchMovies(String genre, float min_rating, int max_runtime){
        List<String> res = new ArrayList<>();
        for (int i = 0; i < movieList.size(); i++){
            boolean flag = false;
            for (int j = 0; j < movieList.get(i).Genre.size(); j++){
                if (movieList.get(i).Genre.contains(genre)){
                    flag = true;
                    break;
                }
            }
            if (movieList.get(i).Runtime <= max_runtime&&movieList.get(i).IMDB_Rating >= min_rating && flag){
                res.add(movieList.get(i).Series_Title);
            }
        }
        Collections.sort(res);
        movieList.forEach(movie -> {if(movie.Series_Title.equals("3 Idiots")) System.out.println(movie.Overview); });
        return res;
    }




}