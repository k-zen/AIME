package io.aime.brain;

import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.ipc.VersionedProtocol;

/**
 * Public interface for interacting with the Cerebellum.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 */
public interface BrainInterface extends VersionedProtocol
{

    public static final long VERSION = 2L;

    /**
     * Method to execute a method/function in the Cerebellum. The default format is XML.
     *
     * @param xml An XML file.
     *
     * @return Response of the function.
     */
    public ObjectWritable execute(Text xml);

    /**
     * This method is used to check if the connection to the Cerebellum is
     * alive.
     *
     * @return TRUE if the connection is alive, FALSE otherwise.
     */
    public boolean isAlive();
}
