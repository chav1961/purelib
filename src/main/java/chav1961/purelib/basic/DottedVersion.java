package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class DottedVersion extends ArrayList<Integer> implements Comparable<DottedVersion> {
	private static final long 	serialVersionUID = -2071809269597291921L;
	
	public static final DottedVersion 				ZERO = new DottedVersion("0");
    public static final Comparator<DottedVersion> 	COMPARATOR = new VersionComparator();
    private static final Integer 					ZERO_INT = Integer.valueOf(0);

    public DottedVersion() {
        super(3);
        innerSetZero();
    }

    public DottedVersion(final DottedVersion ver) {
        super(ver);
    }

    public DottedVersion(final String version) {
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

    public static boolean isParsed(final String version) {
        if (Utils.checkEmptyOrNullString(version)) {
            throw new IllegalArgumentException("Version string can't be null or empty"); 
        } 
        else {
            try {            
                return !new DottedVersion(version).isEmpty();
            } catch (Exception ex) {
                return false;
            }
        }
    }

    public DottedVersion fork() {
        return new DottedVersion(this);
    }

    public DottedVersion getParent() {
        final DottedVersion v = new DottedVersion();
        
        v.clear();
        for (int i = 0; i < size() - 1; i++) {
            v.add(get(i));
        }
        return v;
    }

    public boolean isZero() {
        return size() == 1 && get(0).equals(ZERO_INT);
    }
    
    public void setZero() {
        innerSetZero();
    }

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

