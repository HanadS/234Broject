import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
//this is just for displaying all the items. a comparison to loblaws.java 
public class Independent{
	File target;
	String blank="     ";
//	Document document;
	TreeSet<String> firstLevel=new TreeSet<>();
	TreeSet<String> secondLevel=new TreeSet<>();
	TreeSet<String> thirdLevel=new TreeSet<>();
	PrintWriter pw;
	String[] categories= {"Fruits & Vegetables","Deli & Ready Meals","Bakery","Meat & Seafood","Dairy and Eggs","Drinks","Frozen","Pantry"};
	String url="https://www.yourindependentgrocer.ca";
	static boolean status=false;
	ArrayList<String> failUrl=new ArrayList<>();
	public Independent() {
		status=false;
	}
	
	//function used to start webscraping
	public void execute(String targetFile) throws Exception {
		target=new File(targetFile);
		pw=new PrintWriter(new FileWriter(target));
		String first="/Independent/Food";
		String source= "";
		FirstLevel(source,first);
		status=true;
	}
	
	//to get first level means the [data-level=1]  on the web page. the header categories of the store
	public void FirstLevel(String source,String first) throws Exception {
		Document doc=Jsoup.connect(url+source).get();
		Elements e=doc.select("li[data-level=1]");
		ArrayList<String> firstl=new ArrayList<>();;
		ArrayList<String> text=new ArrayList<>();
		for(int x=0;x<e.size();x++) {
			Element a=e.get(x).select("a").first();
			if(a.attr("href").startsWith(first)) {
				if(firstLevel.add(a.attr("href"))) {
					firstl.add(a.attr("href"));
					text.add(a.text());
				}
			}
		}
		int fru=0;
		int org=8;
		boolean firstS=true;
		for(int x=0;x<firstl.size();x++) {
			String fl=firstl.get(x);
			if(fl.contains("Fruits-%26-Vegetables")) {
				fru=x;
				continue;
			}
			if(fl.contains("Natural-%26-Organic")) {
				org=x;
				continue;
			}
			if(firstS) {
				pw.println("{\""+text.get(x)+"\":[");
			}
			else {
				pw.println("],");
				pw.println("\""+text.get(x)+"\":[");
			}
			pw.flush();
			System.out.println(firstl.get(x)+"  --->  "+url+fl);
			String second=fl.substring(0,fl.indexOf("/c/"));
			SecondLevel(firstl.get(x),second,text.get(x));
			firstS=false;
		}
		String fl=firstl.get(fru);
		System.out.println(firstl.get(fru)+"  --->  "+url+fl);
		String second=fl.substring(0,fl.indexOf("/c/"));
		pw.println("],");
		pw.println("\""+text.get(fru)+"\":[");
		pw.flush();
		SecondLevel(firstl.get(fru),second,text.get(fru));
		
//		fl=firstl.get(org);
//		second=fl.substring(0,fl.indexOf("/c/"));
//		SecondLevel(firstl.get(org),second,text.get(fru));
		pw.println("]}");
		pw.flush();

		
	}
	//to get sub categories for the first level.
	public void SecondLevel(String source, String second,String cate) throws Exception {
		Document doc=Jsoup.connect(url+source).get();
		ArrayList<String> sle=new ArrayList<>();
		ArrayList<String> text=new ArrayList<>();
		Elements e=doc.select("li[data-level=2]");
		for(int x=0;x<e.size();x++) {
			Element a=e.get(x).select("a").first();
			if(a.attr("href").startsWith(second)&&!a.attr("href").contains(source)) {
				if(secondLevel.add(a.attr("href"))){
					sle.add(a.attr("href"));
					text.add(a.text());
				}
			}
		}
		for(int x=0;x<sle.size();x++) {
			String sl=sle.get(x);
			System.out.println(blank+sle.get(x)+"  --->  "+url+sl);
			String third=sl.substring(0,sl.indexOf("/c/"));
			if(x==sle.size()-1) {
				ThirdLevel(sle.get(x),third,true,cate);
			}
			else {
				ThirdLevel(sle.get(x),third,false,cate);
			}
		}
	}
	//to get sub categories for the second level (in some second categories there're no sub categories but only the product.
	public void ThirdLevel(String source, String third, boolean isLast,String cate) throws Exception {
		Document doc=Jsoup.connect(url+source).get();
		boolean hasPro=true;
		Elements e=doc.select("li[data-level=3]");
		ArrayList<String> tle=new ArrayList<>();
		ArrayList<String> text=new ArrayList<>();
		for(int x=0;x<e.size();x++) {			
			Element a=e.get(x).select("a").first();
			if(a.attr("href").startsWith(third)&&!a.attr("href").contains(source)) {
				if(thirdLevel.add(a.attr("href"))){
					hasPro=false;
					tle.add(a.attr("href"));
					text.add(a.text());
				}
			}
			
		}
		if(hasPro) {
			if(isLast) {
				productLevel(source,true,cate);
			}
			else {
				productLevel(source,false,cate);
			}
		}
		else {
			for(int x=0;x<tle.size();x++) {
				String tl=tle.get(x);
				System.out.println(blank+blank+text.get(x)+"  --->  "+url+tl);
				if(isLast&&x==tle.size()-1) {
					productLevel(tl,true,cate);
				}
				else {
					productLevel(tl,false,cate);
				}
			}
		}
	}
	//method used to convert Germany characters to English. 
	private static String[][] UMLAUT_REPLACEMENTS = { { new String("Ä"), "A" }, { new String("Ü"), "U" }, { new String("Ö"), "O" }, { new String("ä"), "a" }, { new String("ü"), "u" }, { new String("ö"), "o" }, { new String("ß"), "ss" },{new String("é"),"e"} ,{new String("É"),"E"}};
	public static String replaceUmlaute(String orig) {
	    String result = orig;

	    for (int i = 0; i < UMLAUT_REPLACEMENTS.length; i++) {
	        result = result.replace(UMLAUT_REPLACEMENTS[i][0], UMLAUT_REPLACEMENTS[i][1]);
	    }

	    return result;
	}
	ArrayList<String> brands=new ArrayList<>();//////used for testing

	public void addToBrands(String brand) {
		if(brand.length()==0||brand.equals("")||brand==null)return;
		//HashSet<String> notBrand=new HashSet<>();
	//	notBrand.add("Coca-Cola");
	//	notBrand.add("Pepsi");
	//	notBrand.add("Dr. Pepper");
	//	notBrand.add("Sprite");
		brand=replaceUmlaute(brand);
		if(brand.length()>0) {
			if(brand.charAt(brand.length()-1)==' ')brand=brand.substring(0, brand.length()-1);
		}
	//	if(brand.equals("p"))brand="Dare";
	//	if(notBrand.contains(brand))
		//	return;
		for(int x=0;x<brands.size();x++) {
			if(brands.get(x).equals(brand))
				return;
		}
		brands.add(brand);
	}
	
	//to webscrape the pages that contains product and write to a json file waiting to be merged after all the webscrapers are done
	public void productLevel(String source,boolean isLast,String cate) throws Exception {
		Document doc=Jsoup.connect(url+source).get();
		ArrayList<String> n=new ArrayList<>();
		ArrayList<String> d=new ArrayList<>();
		ArrayList<String> pr=new ArrayList<>();
		ArrayList<String> b=new ArrayList<>();
		ArrayList<String> i=new ArrayList<>();
		Elements e=doc.select("div.product-info");
		Elements im=doc.select("div.product-page-hotspot");
		System.out.println("raw product size :       "+e.size());
		for(int x=0;x<e.size();x++) {
			Element p=e.get(x);
		//	Element name=p.select
			String brand=p.select("span.js-product-entry-brand").text();
			brand=brand.replaceAll("\u00a0"," ");
			String name=p.select("span.js-product-entry-name").text();
			String price=p.select("span.reg-price-text").text();
			String description=p.select("span.js-product-entry-size-detail").text();
			String img=im.get(x).select("div.product-image ").select("img").attr("src");
			i.add("\""+img+"\"");
			if(brand==null||brand.equals("")||brand.length()==0) {
				brand="null";
			}
			else {
				brand=replaceUmlaute(brand);
				if(brand.charAt(brand.length()-1)==' ')brand=brand.substring(0, brand.length()-1);
				brands.add(brand);
				brand="\""+brand.replaceAll("\"", "'")+"\"";
			}
			name=replaceUmlaute(name);
			name="\""+name.replaceAll("\"", "'")+"\"";
			if(price==null||price.equals("")||price.length()==0) {
				price="null";
			}
			else {
				price="\""+price+"\"";
			}
			if(description==null||description.equals("")||description.length()==0) {
				description="null";
			}
			else {
				description="\""+description.replaceAll("\"", "'")+"\"";
			}
			description=description.replaceAll("\\(", "");
			description=description.replaceAll("\\)", "");
			n.add(name);
			pr.add(price);
			d.add(description);
			b.add(brand);

		}
		

		//System.out.println("product sizee: "+products.size()+blank+blank+doc.title()+"     product Level:");

		for(int x=0;x<n.size();x++) {
			if(isLast&&x==n.size()-1) {
				pw.println("{\"category\":\""+cate+"\",\"name\":"+n.get(x)+",\"description\":"+d.get(x)+",\"brand\":"+b.get(x)+",\"price\":"+pr.get(x)+",\"image\":"+i.get(x)+",\"store\":\"Independent\"}");
			}
			else {
				pw.println("{\"category\":\""+cate+"\",\"name\":"+n.get(x)+",\"description\":"+d.get(x)+",\"brand\":"+b.get(x)+",\"price\":"+pr.get(x)+",\"image\":"+i.get(x)+",\"store\":\"Independent\"},");
			}
			pw.flush();
			System.out.println(n.get(x)+"   "+d.get(x)+"    "+pr.get(x)+"   "+b.get(x));	
		//	System.out.println(blank+blank+blank+products.keySet().toArray()[x]+"    price:  "+products.get(products.keySet().toArray()[x]));
		}
	}
	public static void main(String args[]) throws IOException{
		String fn="D:\\Independent.json";
		Independent l=new Independent();
		try {
			l.execute(fn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			System.out.println(l.status);
		}
	}
}