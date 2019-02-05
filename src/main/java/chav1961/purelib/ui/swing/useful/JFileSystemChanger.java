package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfacers.Format;

public class JFileSystemChanger extends JPanel {
	private static final long serialVersionUID = 6307351718365525165L;

	private final JButton 	test = new JButton();
	
	public JFileSystemChanger() {
		super(new BorderLayout(2,2));
		final JList<FileSystemInterfaceDescriptor>	fileSystemList = new JList<>();
		final JPanel		currentRecord = new JPanel(new LabelledLayout(5,5));
		final JPanel		buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		
	}
	
	@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/i18n")
	@LocaleResource(value="JFileSystemChanger.descriptor.caption",tooltip="JFileSystemChanger.descriptor.tooltip")
	public static class FileSystemDescription {
		@LocaleResource(value="JFileSystemChanger.descriptor.className",tooltip="JFileSystemChanger.descriptor.className.tooltip")
		@Format("r")
		String className;
		@LocaleResource(value="JFileSystemChanger.descriptor.version",tooltip="JFileSystemChanger.descriptor.version.tooltip")
		@Format("r")
		String version;
		@LocaleResource(value="JFileSystemChanger.descriptor.description",tooltip="JFileSystemChanger.descriptor.description.tooltip")
		@Format("r")
		String descriptionId;
		@LocaleResource(value="JFileSystemChanger.descriptor.vendor",tooltip="JFileSystemChanger.descriptor.vendor.tooltip")
		@Format("r")
		String vendorId;
		@LocaleResource(value="JFileSystemChanger.descriptor.license",tooltip="JFileSystemChanger.descriptor.license.tooltip")
		@Format("r")
		String licenseId;
		@LocaleResource(value="JFileSystemChanger.descriptor.uriTemplate",tooltip="JFileSystemChanger.descriptor.uriTemplate.tooltip")
		@Format("r")
		URI uriTemplate;
	}
}
