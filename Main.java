import fr.gouv.vitam.generator.scanner.core.ScanFileSystemTreeImpl;

public class Main{
	public static void main (String args[]){
		try{
		new ScanFileSystemTreeImpl(args[0],args[1],args[2]).scan();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
