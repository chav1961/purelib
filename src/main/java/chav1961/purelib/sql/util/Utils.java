package chav1961.purelib.sql.util;

public class Utils {
	public static boolean like(final String content, final String template) {
		if (content == null) {
			throw new NullPointerException("Content string can'tbe null");
		}
		else if (template == null) {
			throw new NullPointerException("Template string can'tbe null");
		}
		else if (content.length() == 0) {
			return  template.length() == 0;
		}
		else if (template.length() == 0) {
			return false;
		}
		else {
			return likeInternal(content.toCharArray(),0,template.toCharArray(),0);
		}
	}

	public static boolean like(final char[] content, final char[] template) {
		if (content == null) {
			throw new NullPointerException("Content string can'tbe null");
		}
		else if (template == null) {
			throw new NullPointerException("Template string can'tbe null");
		}
		else if (content.length == 0) {
			return  template.length == 0;
		}
		else if (template.length == 0) {
			return false;
		}
		else {
			return likeInternal(content,0,template,0);
		}
	}

	private static boolean likeInternal(final char[] content, final int contentFrom, final char[] template,final int templateFrom) {
		// TODO Auto-generated method stub
		return false;
	}
}
