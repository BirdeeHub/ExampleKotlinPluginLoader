package MySweep;
import examplepluginloader.api.MyAPI;
import examplepluginloader.api.MyPlugin;
import examplepluginloader.api.plugin.PluginUnloadHandler;
import java.awt.EventQueue;
import java.awt.Frame;

public class PluginLoading implements MyPlugin {
    public void launchPlugin(MyAPI api){
        MineSweeper.StartMineSweeperMain(new String[]{""});
        api.plugin().registerShutdownSequence(new PluginUnloadHandler(){
            public void pluginUnloaded(){
                for (Frame frame : Frame.getFrames()) {
                    frame.dispose();
                }
            }
        });
    }
}
