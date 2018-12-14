package chav1961.purelib.model.interfaces;

import java.sql.SQLException;

public interface ORMSerializer<Key,Content> extends Iterable<Content> {
	boolean insert(Key key, Content content) throws SQLException;
	Key insert(Content content) throws SQLException;
	boolean update(Content content) throws SQLException;
	boolean updateChanged(Content oldContent, Content newContent) throws SQLException;
	Content get(Key key) throws SQLException;
	boolean delete(Key key) throws SQLException;
}
