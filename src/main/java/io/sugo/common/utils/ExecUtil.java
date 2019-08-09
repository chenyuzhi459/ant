package io.sugo.common.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Created by chenyuzhi on 19-8-7.
 */
public class ExecUtil {
	public static ScheduledExecutorService scheduledSingleThreaded( String nameFormat)
	{
		return scheduledSingleThreaded(nameFormat, null);
	}

	public static ScheduledExecutorService scheduledSingleThreaded( String nameFormat, @Nullable Integer priority)
	{
		return Executors.newSingleThreadScheduledExecutor(makeThreadFactory(nameFormat, priority));
	}

	public static ThreadFactory makeThreadFactory( String nameFormat, @Nullable Integer priority)
	{
		final ThreadFactoryBuilder builder = new ThreadFactoryBuilder()
				.setDaemon(true)
				.setNameFormat(nameFormat);
		if (priority != null) {
			builder.setPriority(priority);
		}

		return builder.build();
	}

	public static ScheduledExecutorService scheduledMutilThread(int thread, String nameFormat){
		return Executors.newScheduledThreadPool(thread, makeThreadFactory(nameFormat, null));
	}

	public static ExecutorService multiThreaded(int threads,  String nameFormat)
	{
		return multiThreaded(threads, nameFormat, null);
	}

	public static ExecutorService multiThreaded(int threads, String nameFormat, @Nullable Integer priority)
	{
		return Executors.newFixedThreadPool(threads, makeThreadFactory(nameFormat, priority));
	}

}
