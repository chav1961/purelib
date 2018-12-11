package chav1961.purelib.ui;

import chav1961.purelib.ui.interfacers.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfacers.Recommendations;

public class TrivialRecommendations implements Recommendations {
	private static final TrivialRecommendations	singleton = new TrivialRecommendations(); 
	
	protected TrivialRecommendations() {
	}
	
	@Override
	public About forNode(final ContentNodeMetadata node) {
		if (node == null) {
			throw new NullPointerException("Node to get recommendations for can' tb enull"); 
		}
		else {
			return new About(){
				@Override
				public Location getPreferredLocation() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public float getPreferredWidth() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public float getPreferredHeight() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public int getPreferredOrder() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public String getPreferredStyle() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public FunctionalUnit getPreferredFunctionalUnit() {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}
	}
	
	public static TrivialRecommendations getInstance() {
		return singleton;
	}
}
