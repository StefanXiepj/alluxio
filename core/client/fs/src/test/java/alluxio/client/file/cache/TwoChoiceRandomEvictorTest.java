/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.client.file.cache;

import alluxio.client.file.cache.evictor.CacheEvictorOptions;
import alluxio.client.file.cache.evictor.TwoChoiceRandomEvictor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link TwoChoiceRandomEvictor} class.
 */
public class TwoChoiceRandomEvictorTest {
  private TwoChoiceRandomEvictor mEvictor;
  private final PageId mFirst = new PageId("1L", 2L);
  private final PageId mSecond = new PageId("3L", 4L);
  private final PageId mThird = new PageId("5L", 6L);

  /**
   * Sets up the instances.
   */
  @Before
  public void before() {
    mEvictor = new TwoChoiceRandomEvictor(new CacheEvictorOptions());
  }

  @Test
  public void evictGetOrder() {
    mEvictor.updateOnGet(mFirst);
    Assert.assertEquals(mFirst, mEvictor.evict());
    mEvictor.updateOnGet(mSecond);
    Assert.assertEquals(mSecond, mEvictor.evict());
  }

  @Test
  public void evictPutOrder() {
    mEvictor.updateOnPut(mFirst);
    Assert.assertEquals(mFirst, mEvictor.evict());
    mEvictor.updateOnPut(mSecond);
    mEvictor.updateOnPut(mFirst);
    PageId evictedPage = mEvictor.evict();
    Assert.assertTrue(evictedPage.equals(mFirst) || evictedPage.equals(mSecond));
  }

  @Test
  public void evictAfterDelete() {
    mEvictor.updateOnPut(mFirst);
    mEvictor.updateOnPut(mSecond);
    mEvictor.updateOnPut(mThird);
    mEvictor.updateOnDelete(mSecond);
    mEvictor.updateOnDelete(mThird);
    Assert.assertEquals(mFirst, mEvictor.evict());
  }

  @Test
  public void evictEmpty() {
    Assert.assertNull(mEvictor.evict());
  }

  @Test
  public void evictAllGone() {
    mEvictor.updateOnPut(mFirst);
    mEvictor.updateOnPut(mSecond);
    mEvictor.updateOnPut(mThird);
    mEvictor.updateOnDelete(mFirst);
    mEvictor.updateOnDelete(mSecond);
    mEvictor.updateOnDelete(mThird);
    Assert.assertNull(mEvictor.evict());
  }
}
