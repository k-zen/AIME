package io.aime.util;

// Apache Hadoop
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

// IO
import java.io.IOException;

public class HadoopFSUtil {

    /**
     * Returns PathFilter that passes all paths through.
     * @return
     */
    public static PathFilter getPassAllFilter() {
        return new PathFilter() {
            @Override
            public boolean accept(Path arg0) {
                return true;
            }
        };
    }

    /**
     * Returns PathFilter that passes directories through.
     * @param fs
     * @return
     */
    public static PathFilter getPassDirectoriesFilter(final FileSystem fs) {
        return new PathFilter() {
            @Override
            public boolean accept(final Path path) {
                try {
                    return fs.getFileStatus(path).isDir();
                }
                catch (IOException ioe) {
                    return false;
                }
            }
        };
    }

    /**
     * Turns an array of FileStatus into an array of Paths.
     * @param stats
     * @return
     */
    public static Path[] getPaths(FileStatus[] stats) {
        if (stats == null) {
            return null;
        }

        if (stats.length == 0) {
            return new Path[0];
        }

        Path[] res = new Path[stats.length];
        for (int i = 0; i < stats.length; i++) {
            res[i] = stats[i].getPath();
        }

        return res;
    }
}
