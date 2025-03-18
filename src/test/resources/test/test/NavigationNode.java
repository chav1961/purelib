package test;

import java.util.Arrays;
import java.util.Properties;

class NavigationNode {
	public String		created;
	public String		modified;
	
	@Override
	public String toString() {
		return "NavigationNode [created=" + created + ", modified=" + modified + "]";
	}
}

class RootNode extends NavigationNode {
	public String			overview;
	public NavigationNode	child;
	
	@Override
	public String toString() {
		return "RootNode [overview=" + overview + ", child=" + child + ", created=" + created + ", modified=" + modified + "]";
	}
}

class TrivialNode extends RootNode {
	public NavigationNode	parent;
	public String 			name;
	public long				modifiers;
	
	@Override
	public String toString() {
		return "TrivialNode [parent=" + parent + ", name=" + name + ", modifiers=" + modifiers + ", overview=" + overview + ", child=" + child + ", created=" + created + ", modified=" + modified + "]";
	}
}

class PackageNode extends TrivialNode {
	public NavigationNode[]	children = new NavigationNode[0];
	public Properties		props;
	
	@Override
	public String toString() {
		return "PackageNode [children=" + Arrays.toString(children) + ", props=" + props + ", parent=" + parent + ", name=" + name + ", modifiers=" + modifiers + ", overview=" + overview + ", child=" + child + ", created=" + created + ", modified=" + modified + "]";
	}
}

class ClassNode extends TrivialNode {
	public String			type;
	public NavigationNode	extendsRef;
	public NavigationNode[]	implementsRefs = new NavigationNode[0];
	public NavigationNode[]	fields = new NavigationNode[0];
	public NavigationNode[]	methods = new NavigationNode[0];
	public NavigationNode[]	constructors = new NavigationNode[0];
	public Properties		props;

	@Override
	public String toString() {
		return "ClassNode [type=" + type + ", extendsRef=" + extendsRef + ", implementsRefs="
				+ Arrays.toString(implementsRefs) + ", fields=" + Arrays.toString(fields) + ", methods="
				+ Arrays.toString(methods) + ", constructors=" + Arrays.toString(constructors) + ", props=" + props
				+ ", parent=" + parent + ", name=" + name + ", modifiers=" + modifiers + ", overview=" + overview
				+ ", child=" + child + ", created=" + created + ", modified=" + modified + "]";
	}
}

class FieldNode extends TrivialNode {
	public String		type;
	public String		initial;
	public Properties	props;

	@Override
	public String toString() {
		return "FieldNode [type=" + type + ", initial=" + initial + ", props=" + props + ", parent=" + parent
				+ ", name=" + name + ", modifiers=" + modifiers + ", overview=" + overview + ", child=" + child
				+ ", created=" + created + ", modified=" + modified + "]";
	}
}

class MethodNode extends TrivialNode {
	public String			returned;
	public NavigationNode[]	parmRef = new NavigationNode[0];
	public NavigationNode[]	throwsRef = new NavigationNode[0];
	public Properties		props;

	@Override
	public String toString() {
		return "MethodNode [returned=" + returned + ", parmRef=" + Arrays.toString(parmRef) + ", throwsRef="
				+ Arrays.toString(throwsRef) + ", props=" + props + ", parent=" + parent + ", name=" + name
				+ ", modifiers=" + modifiers + ", overview=" + overview + ", child=" + child + ", created=" + created
				+ ", modified=" + modified + "]";
	}
}

class ConstructorNode extends TrivialNode {
	public NavigationNode[]	parmRef = new NavigationNode[0];
	public NavigationNode[]	throwsRef = new NavigationNode[0];
	public Properties		props;

	@Override
	public String toString() {
		return "ConstructorNode [parmRef=" + Arrays.toString(parmRef) + ", throwsRef=" + Arrays.toString(throwsRef)
				+ ", props=" + props + ", parent=" + parent + ", name=" + name + ", modifiers=" + modifiers
				+ ", overview=" + overview + ", child=" + child + ", created=" + created + ", modified=" + modified
				+ "]";
	}
}
