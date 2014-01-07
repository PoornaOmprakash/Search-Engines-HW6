import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


	public class Run_SVMFeatures {

		public static String train;
		public static String test;
		public static String c;
		public static PrintWriter writer;
		public static PrintWriter w;
		public static PrintWriter pw;
		
		public static void main(String[] args) throws Exception {
			// TODO Auto-generated method stub
	   
			String usage = "Please supply a parameter file.";
			
			if (args.length < 1) {
			      System.err.println(usage);
			      System.exit(1);
			    }

			    // read in the parameter file; one parameter per line in format of key=value
			    Map<String, String> params = new HashMap<String, String>();
			    Scanner scan = new Scanner(new File(args[0]));
			    String line = null;
			    
			    
			   do {
			      line = scan.nextLine();
			      String [] pair = line.split("=");
			      params.put(pair[0].trim(), pair[1].trim());
			    } while (scan.hasNext());
			    
			   FileInputStream fr = new FileInputStream(params.get("train"));
			   BufferedReader br = new BufferedReader(new InputStreamReader(fr)); 
			   c=params.get("c");
			   
//			   System.out.println(c);
			   
			   
			   
			   PrintWriter writer1 = new PrintWriter("c.txt", "UTF-8");
			   writer1.println(""+c);
			   writer1.close();
			   
			   
			   FileOutputStream stream = new FileOutputStream("train_svm.txt");
			   OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			   writer = new PrintWriter(sw);
			   
			   Map<Integer,ArrayList<ArrayList<Double>>> DPlus = new HashMap<Integer,ArrayList<ArrayList<Double>>>();
			   Map<Integer,ArrayList<ArrayList<Double>>> DMinus = new HashMap<Integer,ArrayList<ArrayList<Double>>>();
			   Set<Integer> qryID = new HashSet<Integer>();
			   
			   int relevance;
			   String str1;
			   int qryid;
			   String str2;
			   int index;
			   double value;
			   
			   String temp = null;
			   int count = 0;
			   while((str1=br.readLine())!=null)              //Process the training data: generate the x-Vector for each query.
			   {
				   //count++;
				   ArrayList<Double> xVector = new ArrayList<Double>();
				   int pos = str1.indexOf(" ");
				   relevance = Integer.parseInt(str1.substring(0,pos));
				   str1=str1.substring(pos+1);
				   str1.trim();
				   pos = str1.indexOf(" ");
				   str2 = str1.substring(0,pos);
				   str2.trim();
				   pos = str1.indexOf(":");
				   str2 = str2.substring(pos+1);
				   str2.trim();
				   qryid = Integer.parseInt(str2);
				   qryID.add(qryid);
				   //System.out.println("Relevance: "+relevance+" QueryID: "+qryid);
				   pos = str1.indexOf(" ");
				   str1 = str1.substring(pos+1);
				   str1.trim();
				   //count = 0;
				   while(!str1.startsWith("#"))
				   {
					   count++;
					   pos = str1.indexOf(" ");
					   str2 = str1.substring(0,pos);
					   index = str2.indexOf(":");
					   value = Double.parseDouble(str2.substring(index+1));
					   xVector.add(value);
					   str1 = str1.substring(pos+1);
				   }

				   if(relevance==0)         //Add to DMinus if the relevance is 0
				   {
					  ArrayList<ArrayList<Double>> t = DMinus.get(qryid);
					  if(t==null)
					  {
						  t=new ArrayList<ArrayList<Double>>();
					  }
					  t.add(xVector);
					  DMinus.put(qryid,t);
				   }
				   if(relevance==1)         //Add to DPlus if the relevance is 1
				   {
					  ArrayList<ArrayList<Double>> t = DPlus.get(qryid);
					  if(t==null)
					  {
						  t=new ArrayList<ArrayList<Double>>();
					  }
					  t.add(xVector);
					  DPlus.put(qryid,t);
				   }
			   }
			   
			   ArrayList<ArrayList<Double>> dplus = new ArrayList<ArrayList<Double>>();
			   ArrayList<ArrayList<Double>> dminus = new ArrayList<ArrayList<Double>>();
			   int numDPlus;
			   int numDMinus;
			   double max=0;
			   double min=0;
			   
				  
				  double sump=0;
				  double sumn=0;
				  
				  writer = new PrintWriter(sw);
				  for(int q:qryID)            //For each queryID
				  {
					  ArrayList<Double> dp = new ArrayList<Double>();
					  ArrayList<Double> dn = new ArrayList<Double>();
					  dplus = DPlus.get(q);
					  //System.out.println(dplus.size());
					  dminus = DMinus.get(q);
					  //System.out.println(dminus.size());
					  for(int i=0;i<dplus.size();i++)
					  {
						  for(int j=0;j<dminus.size();j++)
						  {
							  ArrayList<Double> vVector = new ArrayList<Double>();
							  ArrayList<Double> vVectorNeg = new ArrayList<Double>();
							  dp = dplus.get(i);
							  dn = dminus.get(j);
							  sump=0;
							  sumn=0;
							  
							  double val1 = dp.get(11) * dp.get(24);          //BM25 of anchor * BM25 of title
							  double val2 = dp.get(7) * dp.get(13);           //Page Rank times Host Rank
							  double val3 = dp.get(27) * dp.get(29);          //tf of body times tf of title
							  dp.add(44,val1);
							  dp.add(45,val2);
							  dp.add(46,val3);
							  
							  
							  double val4 = dn.get(11) * dn.get(24);          //BM25 of anchor * BM25 of title
							  double val5 = dn.get(7) * dn.get(13);           //Page Rank times Host Rank
							  double val6 = dn.get(27) * dn.get(29);          //tf of body times tf of title
							  dn.add(44,val1);
							  dn.add(45,val2);
							  dn.add(46,val3);
							  
							  
							  //sump = Math.sqrt(sump);
							  for(int p=0;p<47;p++)
							  {
								  double val = dp.get(p) - dn.get(p);
								  vVector.add(val);
							  }
							  writer.write("1 qid:"+q+" "); 
						      for(int g = 0; g<vVector.size(); g++)
					           {
					    		   writer.write(String.format(g+1+":%f ", vVector.get(g)));
					           }
						      writer.write("\n");
						  }
					  }
					  
				  }
				  
				  
				  FileInputStream f = new FileInputStream(params.get("test"));
				   BufferedReader b = new BufferedReader(new InputStreamReader(f)); 
				   
				   
				   
				   
				   PrintWriter writer2 = new PrintWriter("test_svm.txt", "UTF-8");
					 
					  str1="";
					  String s=" ";
					  count=0;
					  while((str1=b.readLine())!=null)              //Process the training data: generate the x-Vector for each query.
					   {
						   s="";
						   count++;
						   ArrayList<Double> vals = new ArrayList<Double>();
						   ArrayList<Double> xVector = new ArrayList<Double>();
						   int pos = str1.indexOf(" ");
						   relevance = Integer.parseInt(str1.substring(0,pos));
						   //str1=str1.substring(pos+1);
						   
						   str1=str1.substring(pos+1);
						   str1.trim();
						   pos = str1.indexOf(" ");
						   str2 = str1.substring(0,pos);
						   str2.trim();
						   pos = str1.indexOf(":");
						   str2 = str2.substring(pos+1);
						   str2.trim();
						   qryid = Integer.parseInt(str2);
						   
						   if(relevance==0)
						   {
							    writer2.write("-1 qid:"+qryid+" ");
						   }
						   else
						   {
							     writer2.write("1 qid:"+qryid+" ");
						   }
						   
						   
						   qryID.add(qryid);
						   //System.out.println("Relevance: "+relevance+" QueryID: "+qryid);
						   pos = str1.indexOf(" ");
						   str1 = str1.substring(pos+1);
						   str1.trim();
						   //count = 0;
						   while(!str1.startsWith("#"))
						   {
							   count++;
							   pos = str1.indexOf(" ");
							   str2 = str1.substring(0,pos);
							   index = str2.indexOf(":");
							   value = Double.parseDouble(str2.substring(index+1));
							   vals.add(value);
							   xVector.add(value);
							   str1 = str1.substring(pos+1);
						   }
						   
						   double val1 = vals.get(11) * vals.get(24);
						   double val2 = vals.get(7) * vals.get(13);
						   double val3 = vals.get(27) * vals.get(29);
						   vals.add(44,val1);
						   vals.add(45,val2);
						   vals.add(46,val3);
						   
						   double sum=0;
						   for(int y=0;y<vals.size();y++)
						   {
							   sum+=Math.pow(vals.get(y),2);
						   }
						   
						   for(int g = 0; g<vals.size(); g++)
				           {
				    		   writer2.write(String.format(g+1+":%f ", vals.get(g)/sum));
				           }
					      writer2.write("\n");
						   
						    //s+=str1;
						   //writer2.println(s);
					   }
                //System.out.println(count);
                writer2.close();
		}
}
