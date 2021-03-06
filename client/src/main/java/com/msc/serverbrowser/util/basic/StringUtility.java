package com.msc.serverbrowser.util.basic;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Marcel
 * @since 19.09.2017
 */
public final class StringUtility {
	private StringUtility() {
		// Constructor to prevent instantiation
	}

	/**
	 * Escapes SQL Statements.
	 *
	 * @param string the sql statement
	 * @param replacements replacements for valueplaceholders
	 * @return the escaped statements containing given values
	 */
	public static String escapeFormat(final String string, final Object... replacements) {
		final String[] replacementsNew = new String[replacements.length];
		for (int i = 0; i < replacements.length; i++) {
			final Object value = replacements[i];
			if (Objects.nonNull(value)) {
				replacementsNew[i] = value.toString().replace("'", "''");
			}
		}
		return MessageFormat.format(string, (Object[]) replacementsNew);
	}

	/**
	 * Puts <code>http://</code> infront of the url if not it already has
	 * <code>http://</code> or
	 * <code>https://</code> infront of it.
	 *
	 * @param url
	 *            the url to fix
	 * @return the fixed url or the original if there was no need to fix
	 */
	public static String fixUrlIfNecessary(final String url) {
		if (!(url.startsWith("http://") || url.startsWith("https://"))) {
			return "http://" + url;
		}
		return url;
	}

	/**
	 * Stolen and edited from <a href=
	 * 'https://stackoverflow.com/questions/237159/whats-the-best-way-to-check-to-see-if-a-string-represents-an-integer-in-java'>
	 * this StackOverflow question</a>
	 * <p>
	 * Checks if a {@link String} looks like a Number and tries to parse it ito an {@link Integer},
	 * if it fails an empty {@link Optional} will be returned instead.
	 * </p>
	 *
	 * @param string
	 *            the {@link String} that shall be parsed
	 * @return {@link Optional} containing the {@link Integer} or
	 *         {@link Optional#empty()}
	 */
	public static Optional<Integer> parseInteger(final String string) {
		if (string == null) {
			return Optional.empty();
		}
		final int length = string.length();
		if (length == 0) {
			return Optional.empty();
		}

		int i = 0;
		if (string.charAt(0) == '-') {
			if (length == 1) {
				return Optional.empty();
			}
			i = 1;
		}

		for (; i < length; i++) {
			final char c = string.charAt(i);
			if (c < '0' || c > '9') {
				return Optional.empty();
			}
		}

		return OptionalUtility.attempt(() -> Integer.parseInt(string));
	}

	/**
	 * Converts a String to a boolean.
	 *
	 * @param toBeConverted
	 *            the string that has to be converted
	 * @return true if the string equals <code>true</code> (ignorecase) or
	 *         <code>1</code>
	 */
	public static boolean stringToBoolean(final String toBeConverted) {
		return "true".equalsIgnoreCase(toBeConverted) || "1".equals(toBeConverted);
	}

	/**
	 * <p>
	 * Converts bytes into a human readable format the follwing way:
	 * </p>
	 *
	 * <pre>
	 * 1024 byte = 1 KibiByte
	 * 1024 KibiByte = 1 MebiByte
	 * 1024 MebiByte = 1 GibiByte
	 * 1024 GibiByte = 1 TebiByte
	 * 1024 TebiByte = 1 PebiByte
	 * 1024 PebiByte = 1 ExbiByte
	 * </pre>
	 *
	 * @param bytes
	 *            that will be converted
	 * @return a human readable string following the example given in the method description
	 */
	public static String humanReadableByteCount(final long bytes) {
		final int unit = 1024;
		if (bytes < unit) {
			// Needn't do any conversion at all, since it is still below 1 KiB
			return bytes + " B";
		}
		final int expression = (int) (Math.log(bytes) / Math.log(unit));
		final String bytePrefix = "KMGTPE".charAt(expression - 1) + "i";
		return String.format("%.1f %sB", bytes / Math.pow(unit, expression), bytePrefix);
	}

	/**
	 * Checks if a {@link String} conforms to the uri format.
	 *
	 * @param possibleUrl
	 *            the {@link String} to check.
	 * @return true if it was valid and false otherwise
	 */
	public static boolean isValidURL(final String possibleUrl) {
		if (possibleUrl == null) {
			return false;
		}

		return possibleUrl
				.matches("^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!10(?:\\.\\d{1,3}){3})(?!127(?:\\.\\d{1,3}){3})(?!169\\.254(?:\\.\\d{1,3}){2})(?!192\\.168(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)*(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}]{2,})))(?::\\d{2,5})?(?:/[^\\s]*)?$");
	}

	/**
	 * Escapes certain characters in given html:
	 *
	 * @param html html to be escaped
	 * @return the escaped html
	 */
	public static String escapeHTML(final String html) {
		return html.replace("\"", "&quot").replace("&", "&amp").replace("<", "&lt").replace("<", "&gt");
	}
}
