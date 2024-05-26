package subwaysystem;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

//test data
public class Test {

	private List<Route> routes = new ArrayList <Route> ();

	/**
	 * create GUI
	 */
	public Test() {

	}
	
	/**
	 * read the subway.txt and get routes
	 */
    private void readFile() {
    	try {
    		Scanner s = new Scanner(new File("subway.txt"));
    		int rankLine = 0;
    		while (s.hasNext()) {
    			String line =s.nextLine();
    			if(line.contains("线")) {
    			    Route r  = new Route();
    			    r.setName(line.substring(0,(line.indexOf("线")) + 1));
    			    this.routes.add(r);
    			    rankLine++;  
    			}
    			if(line.contains("---")){
    				setIntervals(this.routes.get(rankLine-1), line);
    			} 
    		}
    		s.close();
    	}catch (NullPointerException e) {
    		e.printStackTrace();
		}
    	catch(IOException e){
    		e.printStackTrace();
    	}
    }
	
    /**
	 * set an interval from the read line
	 * @param r the route
	 * @param l the read string
	 * @throws IOException
	 */
	private void setIntervals(Route r, String l) throws IOException  {
		String[] s1 = l.split("---");
		String[] s2 = s1[1].split("\t");
		Interval in = new Interval(s1[0], s2[0], Double.valueOf(s2[1]));
		r.getIntevals().add(in);
	}
	
	/**
	 * get all the routes that through the stop
	 * @param stop
	 * @return
	 */
	private ArrayList<String> getAllRoutes(String stop) {
		ArrayList <String> all = new ArrayList<>();
		for(Route x: this.routes) {
			for(Interval i: x.getIntevals()) {
				if((i.getStops()[0].equals(stop))||(i.getStops()[1].equals(stop))) {
					all.add(x.getName());
					break;
				}
			}
		}
		return all;
	}
	
	/**
	 * return all the sorted stops according to the route name and the direction
	 * @param routeName the name of the route
	 * @param direction the direction of the route
	 * @return
	 */
	private ArrayList<String> getSortedStops(String routeName, String direction) {
		ArrayList<String> stops =new ArrayList<>();
		String routeEnd = direction.substring(0, direction.indexOf("方向"));
		for(Route r: this.routes) {
			if(r.getName().equals(routeName)) {
				if(r.getIntevals().get(0).getStops()[0].equals(routeEnd)) {
					for(int i = r.getIntevals().size()-1; i>=0; i--) {
						stops.add(r.getIntevals().get(i).getStops()[1]);
					}
					stops.add(routeEnd);
				}
				else if(r.getIntevals().get(r.getIntevals().size()-1).getStops()[1].equals(routeEnd)) {
					for(int i = 0; i<r.getIntevals().size(); i++) {
						stops.add(r.getIntevals().get(i).getStops()[0]);
					}
					stops.add(routeEnd);
				}
				break;
			}
		}
		return stops;
	}
	
	/**
	 * get the array of all the stops
	 */
	private ArrayList<String> getAllStops() {
		ArrayList< String> all = new ArrayList<>();
		for(Route r: this.routes) {
			for(Interval i: r.getIntevals()) {
				for(String s: i.getStops()) {
					boolean b = true;
					for(int j = 0; j<all.size(); j++) {
						if(all.get(j).equals(s)) {
							b = false;
						}
					}
					if(b == true) {
						all.add(s);
					}
				}
			}
		}
		return all;
	}
	
	/**
	 * initial map before Dijkstra
	 * @param map the distance matrix of stops
	 * @param all array of all the stops
	 * @param max as infinity
	 */
	private void initialDistance(double[][] map, ArrayList<String> all, double max) {
    	for(Route r : this.routes) {
    		for(Interval interval : r.getIntevals()) {
    			for(int i = 0; i<all.size(); i++) {
    				if(interval.getStops()[0].equals(all.get(i))) {
    					for(int j = 0; j<all.size(); j++) {
    						if(all.get(j).equals(interval.getStops()[1])) {
    							map[i][j] = interval.getDistance();
    							map[j][i] = interval.getDistance();
    						}
    					}
    				break;
    				}
    			}
    		}
    	}
    	for(int i = 0; i<map.length; i++) {
    		for(int j = 0; j<map.length; j++) {
    			if(map[i][j]==0.0 && i!=j) { //if can't reach
    				map[i][j] = max;
    			}
    		}
    	}
	}
	
	/**
     * show the best path from the start stop to the end stop
     * @param start the start stop
     * @param end the end stop
     */
	private ArrayList<String> getBestPath(String start, String end) {
    	ArrayList<String> best = new ArrayList<>();
    	ArrayList<String> all = getAllStops();
    	double max = 1000;
    	double[][] map = new double[all.size()][all.size()];
    	initialDistance(map, all, max);
    	boolean[] isVisited = new boolean[map.length];//mark whether the stop is considered
    	double[] minDistance = new double[map.length];//store the minimal distances from start to other stops
    	for(int i = 0; i<map.length; i++) {  //initialize data
    		isVisited[i] = false;
    		minDistance[i] = max;
    	}
    	int startIndex = 0;
    	int endIndex = 0;
    	//mark the start stop and the end stop
    	for(int i=0;i<all.size();i++) {   
    		if(all.get(i).equals(start)) {
    		    minDistance[i]=0;
    		    isVisited[i]=true;
    		    startIndex = i;
    		}
    		if(all.get(i).equals(end)) {
    			endIndex = i;
    		}
    	}
    	int unVisitedNum = map.length;  //record the number of unvisited stops
    	int indexNow = startIndex;  //store the index of the stop of minimal distance
    	int[] pre = new int[map.length];
    	while(unVisitedNum > 0 && indexNow!=endIndex) {
    		double min = max;
    		//find the minimal in the group of unvisited stops
    		for(int i = 0; i<map.length; i++) {   
    			if(!isVisited[i]) {
    				if(minDistance[i]<min) {
    				    min = minDistance[i];
    				    indexNow = i;  
    				}
    			}
    		}
    		isVisited[indexNow] = true;
    		unVisitedNum--;
    		//update the distance of the start stop to other stops
    		for(int j=0; j<map.length; j++) {
    			if(!isVisited[j]) {     
    	            if(minDistance[indexNow] + map[indexNow][j] < minDistance[j]) {  
    	    		    minDistance[j] = minDistance[indexNow] + map[indexNow][j];
    	    		    pre[j] = indexNow;
    	            }
    			}
    	    }
    	}
    	returnBestRoute(startIndex, endIndex, all, pre, best);
    	return best;
    }  
    
    /**
     * get the route from the start to the end by recursion
     * @param startIndex index of the start stop
     * @param endIndex index of the end stop
     * @param all array of all stops
     * @param pre an array that records the stop before current stop
     * @param best the array of the best route from start to the end
     */
	private void returnBestRoute(int startIndex, int endIndex, ArrayList<String> all, int[] pre, ArrayList<String>best) {
    	if(endIndex==startIndex) {
    		best.add(all.get(endIndex));
    		return;
    	}
    	returnBestRoute(startIndex, pre[endIndex], all, pre, best);
    	best.add(all.get(endIndex));
    }
    
    /**
     * judge whether the two stops are in the same route
     * @param s1 stop1
     * @param s2 stop2
     * @return
     */
	private boolean inSameRoute(String s1 , String s2) {
    	for(Route r : this.routes) {
    		boolean b1 = false;
    		boolean b2 = false;
    		for(Interval i : r.getIntevals()) {
    			for(String s : i.getStops()) {
    				if(s1.equals(s)) {
    					b1 = true;
    				}
    				else if(s2.equals(s)) {
    					b2 = true;
    				}
    			}
    		}
    		if(b1 && b2) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * get the route by two stops
     * @param s1 stop1
     * @param s2 stop2
     * @return
     */
	private String getRouteByTwoStops(String s1, String s2) {
    	for(String r1 : getAllRoutes(s1)) {
    		for(String r2 : getAllRoutes(s2)) {
    			if(r1.equals(r2)) {
    				return r2;
    			}
    		}
    	}
    	return null;
    }
    
    /**
     * print the best route standardly
     * @param start the start stop
     * @param end the end stop
     */
	private String printBestPath(String start, String end) {
    	ArrayList<String> path = getBestPath(start, end);
    	String result = "乘" + getRouteByTwoStops(path.get(0), path.get(1)) + "[" + start + " , ";
    	for(int i = 2; i<path.size(); i++) {
    		if((!inSameRoute(path.get(i), path.get(i-2)))||(inSameRoute(path.get(i), path.get(i-2))&&!getRouteByTwoStops(path.get(i), path.get(i-2)).equals(path.get(i-2)))) {
    			if(!getRouteByTwoStops(path.get(i), path.get(i-1)).equals(getRouteByTwoStops(path.get(i-1), path.get(i-2)))) {
    			result = result + path.get(i-1)+"] , 换乘" + getRouteByTwoStops(path.get(i-1), path.get(i)) + "[" + path.get(i-1) + " , ";
    			}
    			}
    	}
    	result = result + end + "]";
    	return result;
    }
    
    /**
     * get the distance of two stops
     * @param s1 stop1
     * @param s2 stop2
     * @return
     */
	private double getDistance(String s1, String s2) {
    	for(Route r : this.routes) {
    		for(Interval i : r.getIntevals()) {
    			if((i.getStops()[0].equals(s1) && i.getStops()[1].equals(s2)) || (i.getStops()[0].equals(s2) && i.getStops()[1].equals(s1))){
    				return i.getDistance();
    			}
    		}
    	}
    	return 0;
    }
    
    /**
     * get the distance of a path
     * @param path an array of all the stops of the path
     * @return
     */
	private double getDistance(ArrayList<String> path) {
    	double dsum = 0;
    	for(int i = 1; i<path.size(); i++) {
    		dsum = dsum + getDistance(path.get(i), path.get(i-1));
    	}
    	return dsum;
    }
    
    /**
     * calculate the fee of the path
     * @param path an array of the stops of the path
     * @param type the type of payment
     * @return
     */
	private double countPath(ArrayList<String> path, String type) {
    	Pay p = null;
    	if(type.equals("RegularPay")) {
    		return 0;
    	}
    	else if(type.equals("UsualPay")) {
    		p = new UsualPay(getDistance(path));
    	}
    	else if(type.equals("CardPay")) {
    		p = new CardPay(getDistance(path));
    	}
    	return p.count();
    }
    
	/**
	 * test functions
	 * @param args
	 */
	public static void main(String[] args) {
		Test t = new Test();
		t.readFile();
		System.out.println("请输入站点：");
		Scanner scanner = new Scanner(System.in);

		String target_station = scanner.nextLine();
		System.out.println("经过该站点的多有路径为：");
		String content = "";
		for(String s : t.getAllRoutes(target_station)) {
			content = content + "  " + s;
		}
		System.out.println(content);


		System.out.println("请输入线路和方向（中间用空格隔开）：");
		scanner = new Scanner(System.in);
		String info = scanner.nextLine();
		String xianlu = info.split(" ")[0];
		String fangxiang = info.split(" ")[1];
		content = "";
		ArrayList<String> sorted = t.getSortedStops(xianlu , fangxiang);
		int i = 0;
		for(i = 0; i <sorted.size()-1; i++) {
			content = content  +" " + sorted.get(i) + " " + "->";
		}
		for(String s : sorted){
			System.out.println(s);
		}
		content = content + sorted.get(i);
		System.out.println("该线路中所有站点的顺序列表：\n"+content);

		System.out.println("请输入站点起点：");
		scanner = new Scanner(System.in);
		String begin = scanner.nextLine();
		System.out.println("请输入站点终点：");
		scanner = new Scanner(System.in);
		String end = scanner.nextLine();
		System.out.println("最短路径为：");
		content = "";
		ArrayList<String> best = t.getBestPath(begin, end);
		i = 0;
		for(i = 0; i<best.size()-1; i++) {
			content = content + " " + best.get(i) + " " + "->";
		}
		content = content + best.get(i);
		System.out.println(content);

		System.out.println("打印最短路径至标准输出:");
		ArrayList<String> path = t.getBestPath(begin, end);
		String result = "乘" + t.getRouteByTwoStops(path.get(0), path.get(1)) + "[" + begin + " , ";
		for(i = 2; i<path.size(); i++) {
			if((!t.inSameRoute(path.get(i), path.get(i-2)))||(t.inSameRoute(path.get(i), path.get(i-2))&&!t.getRouteByTwoStops(path.get(i), path.get(i-2)).equals(path.get(i-2)))) {
				if(!t.getRouteByTwoStops(path.get(i), path.get(i-1)).equals(t.getRouteByTwoStops(path.get(i-1), path.get(i-2)))) {
					result = result + path.get(i-1)+"] , 换乘" + t.getRouteByTwoStops(path.get(i-1), path.get(i)) + "[" + path.get(i-1) + " , ";
				}
			}
		}
		result = result + end + "]";
		System.out.println(result);


		System.out.println("路径对应的乘车费用(普通单程票)");
		System.out.println(Double.valueOf(t.countPath(t.getBestPath(begin, end), "UsualPay")).toString());


		System.out.println("使用武汉通的乘客的票价");
		System.out.println(Double.valueOf(t.countPath(t.getBestPath(begin,end), "CardPay")).toString());



		System.out.println("使用日票的乘客的票价");
		System.out.println(Double.valueOf(t.countPath(t.getBestPath(begin,end), "RegularPay")).toString());



	}

}
