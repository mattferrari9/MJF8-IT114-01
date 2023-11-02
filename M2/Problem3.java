package M2;

import java.util.Arrays;

public class Problem3 {
    public static void main(String[] args) {
        Integer[] a1 = new Integer[]{-1, -2, -3, -4, -5, -6, -7, -8, -9, -10};
        Integer[] a2 = new Integer[]{-1, 1, -2, 2, 3, -3, -4, 5};
        Double[] a3 = new Double[]{-0.01, -0.0001, -.15};
        String[] a4 = new String[]{"-1", "2", "-3", "4", "-5", "5", "-6", "6", "-7", "7"};
        
        bePositive(a1);
        bePositive(a2);
        bePositive(a3);
        bePositive(a4);
    }
    
    static <T> void bePositive(T[] arr){
        System.out.println("Processing Array:" + Arrays.toString(arr));
        T[] output = Arrays.copyOf(arr, arr.length);
        
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof Number) {
                // Check if the element is a number (Integer or Double)
                Number number = (Number) arr[i];
                if (number.doubleValue() < 0) {
                    // Convert negative numbers to positive
                    if (arr[i] instanceof Integer) {
                        output[i] = (T) Integer.valueOf(Math.abs(number.intValue()));
                    } else if (arr[i] instanceof Double) {
                        output[i] = (T) Double.valueOf(Math.abs(number.doubleValue()));
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (T value : output) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(value.toString() + " (" + value.getClass().getSimpleName().substring(0, 1) + ")");
        }

        System.out.println("Positive Output: " + sb.toString());
    }
}
