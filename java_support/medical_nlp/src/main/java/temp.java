import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

public class temp {
	public static void main(String[] args) throws Exception {
		StringBuilder sb = new StringBuilder();

		BufferedReader reader = new BufferedReader(
				new FileReader(new File("C:\\Users\\phd2\\PycharmProjects\\medical_nn2\\result.txt")));
		String line = null;
		while (null != (line = reader.readLine())) {
			if (line.startsWith("[")) {
				String[] nums_str = line.substring(1, line.length()).split(",");
				double[] nums = new double[nums_str.length-1];
				
				double avg = 0;
				double max = Double.MIN_VALUE;
				double min = Double.MAX_VALUE;
				
				for (int i = 0; i < nums.length; i++) {
					nums[i] = Double.parseDouble(nums_str[i]);
					
					avg += nums[i];
					if (max < nums[i]) {
						max = nums[i];
					}
					if (min > nums[i]) {
						min = nums[i];
					}
				}
				avg /= nums.length;
				
				double median = getMedian(nums);
				
				avg = (double)Math.round(avg * 10000d) / 10000d;
				min = (double)Math.round(min * 10000d) / 10000d;
				max = (double)Math.round(max * 10000d) / 10000d;
				median = (double)Math.round(median * 10000d) / 10000d;
				
				sb.append(median +"-"+ avg + "(" + min +" ~ "+ max + ")").append("\n");
				
				
			}else{
				sb.append(line).append("\n");
			}
		}
		reader.close();
		
		System.out.println(sb.toString());
	}
	
	static double getMedian(double[] arr){
		Arrays.sort(arr);
		double median;
		if (arr.length % 2 == 0)
		    median = ((double)arr[arr.length/2] + (double)arr[arr.length/2 - 1])/2;
		else
		    median = (double) arr[arr.length/2];
		return median;
	}
}
