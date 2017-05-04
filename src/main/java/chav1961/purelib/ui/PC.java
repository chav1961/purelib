package chav1961.purelib.ui;

@WClass
public class PC {
	@WField(name="x",caption="my caption 1",tooltip="sdxsdsd")
	public int x = 20;
	@WField(name="y",caption="my caption 2",tooltip="12e2")
	public boolean y = true;
	@WField(name="z",caption="my caption 3")
	public PreferredType z = PreferredType.textarea;
	@WField(name="t",caption="my caption 4",preferredType=PreferredType.textarea)
	public String t = "content";
}
