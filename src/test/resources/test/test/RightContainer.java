package test;

public class RightContainer {

//	description:
//		package:
//		- overview (center, tab1)
//		- content (center, tab2)
//		- name (north)
//		- since (north)
//		- last update (north)
//		- flags (deprecated, beta) (north)
//		- keywords (south)
//		class:
//		- overview (center, tab1)
//		- methods (center, tab2)
//		- constructors (center, tab3)
//		- fields (center, tab4)
//		- code samples(center, tab5)
//		- inheritance (center, tab6)
//		- name (north)
//		- since (north)
//		- last update (north)
//		- flags (deprecated, beta, threadSafe) (north)
//		- keywords (south)
//		method:
//		- overview (center, tab1)
//		- parameters and throws (center, tab2)
//		- code samples(center, tab3)
//		- name (north)
//		- since (north)
//		- last update (north)
//		- flags (deprecated, beta) (north)
//		- keywords (south)
	
	
	private static class Keywords {
		private final String[]	keywords;
		
		Keywords(final String[]	keywords) {
			this.keywords = keywords;
		}
	}

	private static class Header {
		protected final TrivialNode 	node;
		
		Header(TrivialNode node) {
			this.node = node;
		}
	}
	
	private static class ClassHeader extends Header {
		ClassHeader(TrivialNode node) {
			super(node);
		}
		
	}

	private static class MethodHeader extends Header {
		MethodHeader(TrivialNode node) {
			super(node);
		}
	}
	
	private static class TabbedContent {
		private final String	tabName;
		
		TabbedContent(final String tabName) {
			this.tabName = tabName;
		}
	}
	
	private static class Overview extends TabbedContent {
		Overview(String tabName) {
			super(tabName);
		}
	}
	
	private static class CodeSample extends TabbedContent {
		CodeSample(String tabName) {
			super(tabName);
		}
		
	}
	
	private static class InnerContent extends TabbedContent {
		InnerContent(String tabName) {
			super(tabName);
		}
	}
	
	private static class ParametersContent extends InnerContent {
		ParametersContent(String tabName) {
			super(tabName);
		}
	}
	
	private static class ClassContent extends InnerContent {
		ClassContent(String tabName) {
			super(tabName);
		}
	}

	private static class PackageContent extends InnerContent {
		PackageContent(String tabName) {
			super(tabName);
		}
	}

	private static class InheritanceContent extends InnerContent {
		InheritanceContent(String tabName) {
			super(tabName);
		}
	}
}
