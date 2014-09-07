package common;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef;

/** Simple example of JNA interface mapping and usage. */
public class WindowsShell {

    public interface SHELL32 extends Library {
        
    	SHELL32 INSTANCE = (SHELL32) Native.loadLibrary( "shell32", SHELL32.class);

		WinDef.HINSTANCE ShellExecuteW(WinDef.HWND hwnd, WString lpOperation,
				WString lpFile, WString lpParameters, WString lpDirectory,
				int nShowCmd);        
		
		WinDef.HINSTANCE ShellExecuteA(WinDef.HWND hwnd, String lpOperation,
				String lpFile, String lpParameters, String lpDirectory,
				int nShowCmd);        
    }
    
    public interface USER32 extends Library {
        
    	USER32 INSTANCE = (USER32) Native.loadLibrary("User32" , USER32.class);

		void MessageBoxW(Object object, WString string, WString string2, int i);
		WinDef.HDC GetDC(WinDef.HWND hWnd);
		int ReleaseDC(WinDef.HWND hWnd, WinDef.HDC hDC);
        
    }

    public static void main(String args[]){

    }

    private static WinDef.HINSTANCE excuteShellW(WString op, WString fileName){
    	final int SW_MAXIMIZE = 3;
    	System.out.println(op+" "+fileName);
    	return SHELL32.INSTANCE.ShellExecuteW(null,op,fileName,null,null,SW_MAXIMIZE);    	
    }

    public static WinDef.HINSTANCE open(String fileName){
    	return excuteShellW(new WString("open"),new WString(fileName));    
//    	return excuteShellA("open",fileName);    	
    }
    
    public static WinDef.HINSTANCE explore(String directory){
    	return excuteShellW(new WString("explore"),new WString(directory));    	
//    	return excuteShellA("explore",directory);    	
    }

}

