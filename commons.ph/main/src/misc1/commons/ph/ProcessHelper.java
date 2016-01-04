package misc1.commons.ph;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import misc1.commons.ExceptionUtils;
import misc1.commons.ds.ImmutableSalvagingMap;
import misc1.commons.ds.SimpleStructKey;
import misc1.commons.ds.StructBuilder;
import misc1.commons.ds.StructKey;
import misc1.commons.ds.StructType;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessHelper extends StructBuilder<ProcessHelperStruct, ProcessHelper> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessHelper.class);

    private static final Path DEV_NULL = Paths.get("/dev/null");

    private ProcessHelper(ImmutableSalvagingMap<StructKey<ProcessHelperStruct, ?, ?>, Object> map) {
        super(TYPE, map);
    }

    public static final SimpleStructKey<ProcessHelperStruct, Path> DIR;
    public static final SimpleStructKey<ProcessHelperStruct, ImmutableList<String>> CMD;
    public static final SimpleStructKey<ProcessHelperStruct, ImmutableSalvagingMap<String, String>> ENV;
    public static final SimpleStructKey<ProcessHelperStruct, ProcessBuilder.Redirect> IN;
    public static final SimpleStructKey<ProcessHelperStruct, ProcessBuilder.Redirect> OUT;
    public static final SimpleStructKey<ProcessHelperStruct, ProcessBuilder.Redirect> ERR;
    static final StructType<ProcessHelperStruct, ProcessHelper> TYPE;
    static {
        ImmutableList.Builder<StructKey<ProcessHelperStruct, ?, ?>> b = ImmutableList.builder();

        b.add(DIR = new SimpleStructKey<ProcessHelperStruct, Path>("dir"));
        b.add(CMD = new SimpleStructKey<ProcessHelperStruct, ImmutableList<String>>("cmd"));
        b.add(ENV = new SimpleStructKey<ProcessHelperStruct, ImmutableSalvagingMap<String, String>>("env"));
        b.add(IN = new SimpleStructKey<ProcessHelperStruct, ProcessBuilder.Redirect>("in", ProcessBuilder.Redirect.PIPE));
        b.add(OUT = new SimpleStructKey<ProcessHelperStruct, ProcessBuilder.Redirect>("out", ProcessBuilder.Redirect.PIPE));
        b.add(ERR = new SimpleStructKey<ProcessHelperStruct, ProcessBuilder.Redirect>("err", ProcessBuilder.Redirect.PIPE));

        TYPE = new StructType<ProcessHelperStruct, ProcessHelper>(b.build(), ProcessHelperStruct::new, ProcessHelper::new);
    }

    public ProcessHelper inheritInput() {
        return set(IN, ProcessBuilder.Redirect.INHERIT);
    }

    public ProcessHelper fileInput(Path p) {
        return set(IN, ProcessBuilder.Redirect.from(p.toFile()));
    }

    public ProcessHelper inheritOutput() {
        return set(OUT, ProcessBuilder.Redirect.INHERIT);
    }

    public ProcessHelper ignoreOutput() {
        return fileOutput(DEV_NULL);
    }

    public ProcessHelper fileOutput(Path p) {
        return set(OUT, ProcessBuilder.Redirect.to(p.toFile()));
    }

    public ProcessHelper inheritError() {
        return set(ERR, ProcessBuilder.Redirect.INHERIT);
    }

    public ProcessHelper ignoreError() {
        return fileError(DEV_NULL);
    }

    public ProcessHelper fileError(Path p) {
        return set(ERR, ProcessBuilder.Redirect.to(p.toFile()));
    }

    public ProcessHelper putEnv(String key, String value) {
        return set(ENV, get(ENV).simplePut(key, value));
    }

    public ProcessHelper removeEnv(String key) {
        return set(ENV, get(ENV).simpleRemove(key));
    }

    public static ProcessHelper of(Path dir, String... cmd) {
        return of(dir, Arrays.asList(cmd));
    }

    public static ProcessHelper of(Path dir, Iterable<String> cmd) {
        ProcessHelper ph = TYPE.builder();
        ph = ph.set(DIR, dir);
        ph = ph.set(CMD, ImmutableList.copyOf(cmd));
        ph = ph.set(ENV, ImmutableSalvagingMap.copyOf(System.getenv()));
        return ph;
    }

    public static final class Result {
        public final String desc;
        public final ImmutableList<Pair<Boolean, String>> tagged;
        public final int exitCode;

        public final ImmutableList<String> stdout;
        public final ImmutableList<String> stderr;
        public final ImmutableList<String> combined;

        public Result(String desc, ImmutableList<Pair<Boolean, String>> tagged, int exitCode) {
            this.desc = desc;
            this.tagged = tagged;
            this.exitCode = exitCode;

            ImmutableList.Builder<String> stdoutBuilder = ImmutableList.builder();
            ImmutableList.Builder<String> stderrBuilder = ImmutableList.builder();
            ImmutableList.Builder<String> combinedBuilder = ImmutableList.builder();
            for(Pair<Boolean, String> line : tagged) {
                (line.getLeft() ? stderrBuilder : stdoutBuilder).add(line.getRight());
                combinedBuilder.add(line.getRight());
            }

            this.stdout = stdoutBuilder.build();
            this.stderr = stderrBuilder.build();
            this.combined = combinedBuilder.build();
        }

        public Result requireSuccess() {
            if(exitCode != 0) {
                throw failure("non-zero exit: " + exitCode);
            }
            return this;
        }

        public String requireLine() {
            requireSuccess();
            if(stdout.size() < 1) {
                throw failure("no output");
            }
            if(stdout.size() > 1) {
                throw failure("more than one line");
            }
            return stdout.get(0);
        }

        public <T> T require(Function<Result, T> cb) {
            return cb.apply(this);
        }

        public RuntimeException failure(String label) {
            return failure(label, null);
        }

        public RuntimeException failure(String label, Throwable cause) {
            StringBuilder sb = new StringBuilder();
            sb.append(desc + ": " + label);
            for(Pair<Boolean, String> line : tagged) {
                sb.append("\n[STD" + (line.getLeft() ? "ERR" : "OUT") + "] " + line.getRight());
            }
            return new RuntimeException(sb.toString(), cause);
        }
    }

    public interface Callback<T> {
        void line(boolean isError, String line);
        T complete(int exitCode);
    }

    private String getDesc() {
        return Joiner.on(' ').join(get(CMD));
    }

    public Result run() {
        return run(new Callback<Result>() {
            ImmutableList.Builder<Pair<Boolean, String>> b = ImmutableList.builder();

            @Override
            public void line(boolean isError, String line) {
                b.add(Pair.of(isError, line));
            }

            @Override
            public Result complete(int exitCode) {
                return new Result(getDesc(), b.build(), exitCode);
            }
        });
    }

    public <T> T run(Callback<T> cb) {
        Path dir = get(DIR);

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Running " + getDesc() + " in " + dir + "...");
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(get(CMD));
            pb = pb.directory(dir.toFile());
            Map<String, String> pbEnv = pb.environment();
            pbEnv.clear();
            for(Map.Entry<String, String> e : get(ENV).entries()) {
                pbEnv.put(e.getKey(), e.getValue());
            }
            pb = pb.redirectInput(get(IN));
            ProcessBuilder.Redirect out = get(OUT);
            pb = pb.redirectOutput(out);
            ProcessBuilder.Redirect err = get(ERR);
            pb = pb.redirectError(err);

            Process p = pb.start();

            // Now, the pain begins.  How many streams do we have to read?
            OutputQueue q0;
            {
                boolean readOut = (out == ProcessBuilder.Redirect.PIPE);
                boolean readErr = (err == ProcessBuilder.Redirect.PIPE);
                if(readOut) {
                    if(readErr) {
                        HorribleOutputQueue hq = new HorribleOutputQueue();
                        hq.start(false, p.getInputStream());
                        hq.start(true, p.getErrorStream());
                        q0 = hq;
                    }
                    else {
                        q0 = new SingleOutputQueue(false, p.getInputStream());
                    }
                }
                else {
                    if(readErr) {
                        q0 = new SingleOutputQueue(true, p.getErrorStream());
                    }
                    else {
                        q0 = new NullOutputQueue();
                    }
                }
            }

            try(OutputQueue q = q0) {
                while(true) {
                    Pair<Boolean, String> line = q.read();
                    if(line == null) {
                        return cb.complete(p.waitFor());
                    }
                    cb.line(line.getLeft(), line.getRight());
                }
            }
        }
        catch(Exception e) {
            throw ExceptionUtils.commute(e);
        }
    }

    private interface OutputQueue extends Closeable {
        Pair<Boolean, String> read() throws Exception;
    }

    private static final ExecutorService horribleExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicInteger nextId = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "ProcessHelper-IO-" + nextId.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    });

    private static class HorribleOutputQueue implements OutputQueue {
        private boolean closed = false;
        private Deque<Pair<Boolean, String>> q = Lists.newLinkedList();
        private Exception failed = null;
        private int threads = 0;

        @Override
        public synchronized Pair<Boolean, String> read() throws Exception {
            while(true) {
                if(failed != null) {
                    throw new RuntimeException("Reader thread failed", failed);
                }
                if(!q.isEmpty()) {
                    return q.removeFirst();
                }
                if(threads == 0) {
                    return null;
                }
                wait();
            }
        }

        private synchronized void threadStarted() {
            ++threads;
        }

        private synchronized boolean write(boolean isError, String line) {
            if(closed) {
                return false;
            }
            q.addLast(Pair.of(isError, line));
            notifyAll();
            return true;
        }

        private synchronized void threadFailed(Exception e) {
            if(failed == null) {
                failed = e;
            }
        }

        private synchronized void threadDone() {
            --threads;
            notifyAll();
        }

        public void start(final boolean isError, final InputStream is) {
            threadStarted();
            horribleExecutor.submit(() -> {
                try {
                    try(BufferedReader br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8))) {
                        while(true) {
                            String line = br.readLine();
                            if(line == null) {
                                return;
                            }
                            if(!write(isError, line)) {
                                return;
                            }
                        }
                    }
                    catch(Exception e) {
                        threadFailed(e);
                    }
                }
                finally {
                    threadDone();
                }
            });
        }

        @Override
        public synchronized void close() {
            closed = true;
        }
    }

    private static class SingleOutputQueue implements OutputQueue {
        private final boolean isError;
        private final BufferedReader br;

        public SingleOutputQueue(boolean isError, InputStream is) {
            this.isError = isError;
            this.br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
        }

        @Override
        public Pair<Boolean, String> read() throws Exception {
            String line = br.readLine();
            if(line == null) {
                return null;
            }
            return Pair.of(isError, line);
        }

        @Override
        public void close() throws IOException {
            br.close();
        }
    }

    private static class NullOutputQueue implements OutputQueue {
        @Override
        public Pair<Boolean, String> read() {
            return null;
        }

        @Override
        public void close() {
        }
    }
}
