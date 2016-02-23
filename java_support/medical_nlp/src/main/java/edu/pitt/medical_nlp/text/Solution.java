package edu.pitt.medical_nlp.text;

public class Solution {

	public int myAtoi(String str) {
		int count = str.length();
		for (int i = 0; i < count; i++) {
			char ch = str.charAt(i);
			if (ch != '0' && ch != ' ') {
				str = str.substring(i);
				count -= i;
				break;
			}
		}
		if (count == 0) {
			return 0;
		}
		int sign = 1;
		boolean flagsign = false, ridzero = false;
		int digit = 0, fist_figit = 0;
		int total = 0, temp = 0;
		short ndigits = 0;
		for (int i = 0; i < count; i++) {
			char ch = str.charAt(i);
			if ('0' <= ch && ch <= '9') {
				digit = ch - '0';
				if (digit > 0 && !ridzero) {
					ridzero = true;
					fist_figit = digit;
				}
				if (ridzero) {
					if (++ndigits > 11 || (ndigits == 10 && fist_figit > 2)) {
						return sign == 1 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
					}
				} else {
					continue;
				}
				temp = total * 10 + digit;
				if ((temp - digit) != total * 10d || (temp < 0 && total > 0)) {
					return sign == 1 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
				} else {
					total = temp;
				}
			} else if (ch == '+' || ch == '-') {
				if (flagsign) {
					return 0;
				}
				sign = ch == '+' ? 1 : -1;
				flagsign = true;
			} else {
				break;
			}
		}
		total = total * sign;
		return total;
	}

	public static void main(String[] args) {
		int total = new Solution().myAtoi("   -123");
		System.out.println(total);
	}

}