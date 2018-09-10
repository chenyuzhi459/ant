package io.sugo.server.pathanalysis.fetcher;

import java.util.List;

public interface DataFetcher
{
  String REDIS = "redis";

  byte[] readBytes();

  void writeBytes(byte[] buf);

  void close();

  String[] fetchData(List<String> itemIdSet, String field);

}
