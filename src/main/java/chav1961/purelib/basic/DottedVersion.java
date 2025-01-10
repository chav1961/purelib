package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

/**
 * <p>This class manipulates with dot-splitten version strings. Supports {@linkplain Comparable} interface.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @last.update 0.0.8
 */
public class DottedVersion extends ArrayList<Integer> implements Comparable<DottedVersion>, Cloneable {
	private static final long 	serialVersionUID = -2071809269597291921L;
	
	public static final DottedVersion 				ZERO = new DottedVersion("0");
    public static final Comparator<DottedVersion> 	COMPARATOR = new VersionComparator();
    private static final Integer 					ZERO_INT = Integer.valueOf(0);

    /**
     * <p>Constructor of the class.</p>
     */
    public DottedVersion() {
        super(3);
        innerSetZero();
    }

    /**
     * <p>Constructor of the class.</p>
     * @param ver another dotted version to make clone from it. Can't be null.
     */
    public DottedVersion(final DottedVersion ver) {
        super(ver);
    }

    /**
     * <p>Constructor of the class.</p>
     * @param version string representation of dotted version. Can't be null or empty.
     * @throws IllegalArgumentException string is null, empty or has invalid syntax
     */
    public DottedVersion(final String version) throws IllegalArgumentException {
        if (Utils.checkEmptyOrNullString(version)) {
            throw new IllegalArgumentException("Version string can't be null or empty"); 
        } 
        else {
            try {
                for (String item : version.split("\\.")) {
                    add(Integer.valueOf(item)); // exception for null or empty.
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid version format [" + version + "]: "+ex.getLocalizedMessage());
            }
        }
    }
    
    @Override
    public Object clone() {
    	return fork();
    }

    /**
     * <p>Assign another version value to current version</p>
     * @param another another version to assign. Can't be null.
     * @throws NullPointerException another version is null.
     * @since 0.0.8
     */
    public void assign(final DottedVersion another) throws NullPointerException {
    	if (another == null) {
    		throw new NullPointerException("Another version instance can't be null");
    	}
    	else {
    		clear();
    		addAll(another);
    	}
    }
    
    /**
     * <p>Can the version string be parsed correctly</p> 
     * @param version service string to parsed.
     * @return true is string can be parsed, false otherwise
     */
    public static boolean isParsed(final String version) {
        if (Utils.checkEmptyOrNullString(version)) {
            return false; 
        } 
        else {
            try {            
                return !new DottedVersion(version).isEmpty();
            } catch (Exception ex) {
                return false;
            }
        }
    }

    /**
     * <p>This in a typified version of the {@linkplain #clone()} method</p>
     * @return see {@linkplain #clone()}
     */
    public DottedVersion fork() {
        return new DottedVersion(this);
    }

    /**
     * <p>Get parent version from current one</p>
     * @return parent version. Can't be null
     */
    public DottedVersion getParent() {
        final DottedVersion v = new DottedVersion();
        
        v.clear();
        for (int i = 0; i < size() - 1; i++) {
            v.add(get(i));
        }
        return v;
    }

    /**
     * <p>Is the version zero (same first)</p>
     * @return true if yes, false otherwise.
     */
    public boolean isZero() {
        return size() == 1 && get(0).equals(ZERO_INT);
    }
    
    /**
     * <p>Reset current version to zero.</p>
     */
    public void setZero() {
        innerSetZero();
    }

    /**
     * <p>Increment last number of version</p>
     * @return new version instance. Can't be null.
     */
    public DottedVersion inc() {
        final DottedVersion ver = new DottedVersion(this);
        
        if (ver.isEmpty()) {
            innerSetZero();
        }
        ver.set(size() - 1, get(size() - 1) + 1);
        return ver;
    }

    private void innerSetZero() {
        this.clear();
        this.add(ZERO_INT);
    }
    
    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        else {
            final StringBuilder sb = new StringBuilder();
            
            for (Integer val : this) {
                sb.append('.').append(val);
            }
            return sb.substring(1);
        }
    }

    /**
     * <p>Does the version to test equals with starting sequence of the current version:</p>
     * <ul>
     * <li>"1.2.3.4.5" starts with "1.2.3"</li> 
     * <li>"1.2.3.4.5" doesn't start with "1.2.2"</li> 
     * </ul> 
     * @param ver version to test. If null, false will be returned.
     * @return true if current versions 'starts with' version to test, false otherwise
     */
    public boolean startFrom(final DottedVersion ver) {
        if (ver == null || this.size() < ver.size()){
            return false;
        }
        for (int i=0; i<ver.size(); i++){
            if (!Objects.equals(ver.get(i), get(i))){
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int compareTo(final DottedVersion ver) {
        int i = 0;
        
        if (ver == null) {
            return 1;
        }
//        while (i < size() && i < ver.size()) {
        for (int minSize = Math.min(size(), ver.size()); i < minSize; i++) {
            if (get(i) > ver.get(i)) {
                return 1;
            }
            else if (get(i) < ver.get(i)) {
                return -1;
            }
//            i++;
        }
        while (i < size()) {
            if (get(i++) != 0) {
                return 1;
            }
        }
        while (i < ver.size()) {
            if (ver.get(i++) != 0) {
                return -1;
            }
        }
        return 0;
    }
    
    private static final class VersionComparator implements Comparator<DottedVersion> {
        @Override
        public int compare(final DottedVersion o1, final DottedVersion o2) {

            if (o1 == o2) {
                return 0;
            }

            if (o1 == null) {
                return -1;
            }

            if (o2 == null) {
                return 1;
            }

            int i = 0;
            for (int minSize = Math.min(o1.size(), o2.size()); i < minSize; i++) {
                if (o1.get(i) > o2.get(i)) {
                    return 1;
                }
                else if (o1.get(i) < o2.get(i)) {
                    return -1;
                }
            }

            while (i < o1.size()) {
                if (o1.get(i++) != 0) {
                    return 1;
                }
            }

            while (i < o2.size()) {
                if (o2.get(i++) != 0) {
                    return -1;
                }
            }

            return 0;
        }
    }

    /**
     * <p>Is version to test between version ranges.</p>
     * @param version version to test. If null, false will be returned.
     * @param fromInclusive lower version of the range (inclusive). If null, lower version check is not processed
     * @param toExclusive higher version of the range (exclusive). If null, high version check is not processed
     * @return true if version to test in the testing range, false otherwise.
     */
    public static boolean isBetween(final DottedVersion version, final DottedVersion fromInclusive, final DottedVersion toExclusive) {
        if (version == null) {
            return false;
        }
        else if (fromInclusive != null && version.compareTo(fromInclusive) < 0) {
            return false;
        }
        else if (toExclusive != null && version.compareTo(toExclusive) >= 0) {
            return false;
        }
        else {
            return true;
        }
    }
}

