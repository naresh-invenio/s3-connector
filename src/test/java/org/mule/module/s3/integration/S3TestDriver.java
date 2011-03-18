/**
 * Mule S3 Cloud Connector
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.s3.integration;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mule.module.s3.AccessControlList.PRIVATE;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.module.s3.S3CloudConnector;
import org.mule.module.s3.StorageClass;
import org.mule.module.s3.simpleapi.VersioningStatus;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.Bucket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class S3TestDriver
{
    private S3CloudConnector connector;
    private String bucketName;

    @Before
    public void setup() throws InitialisationException
    {
        connector = new S3CloudConnector();
        connector.setAccessKey(System.getenv("user.key.access"));
        connector.setSecretKey(System.getenv("user.key.secret"));
        bucketName = System.getenv("bucket.name");
        connector.initialise();
    }

    @After
    public void teardown()
    {
        connector.deleteBucket(bucketName, true);
    }

    @Test(expected = AmazonServiceException.class)
    public void testDeleteNoForce() throws Exception
    {
        connector.createBucket(bucketName, null, PRIVATE);
        connector.createObject(bucketName, "anObject", "hello world", null, null, null, PRIVATE,
            StorageClass.STANDARD, null);
        connector.deleteBucket(bucketName, false);
    }

    /**
     * Creates a bucket, and asserts that buckets count has now increased in 1, and
     * that it is empty of objects. Then adds a new object and asserts that its
     * version is null (versioning disabled), and that the bucket is not empty anymore
     */
    @Test
    public void testCreateBucketAndObjects() throws Exception
    {
        // pre1
        int bucketsCount = connector.listBuckets().size();

        // op1
        Bucket bucket = connector.createBucket(bucketName, null, PRIVATE);

        // pos1
        assertNotNull(bucket);
        assertEquals(bucketName, bucket.getName());
        assertEquals(bucketsCount + 1, connector.listBuckets().size());
        assertFalse(connector.listObjects(bucketName, "").iterator().hasNext());

        // op2
        String objectVersion = connector.createObject(bucketName, "anObject", "hello world!", null, null,
            "text/plain", PRIVATE, StorageClass.STANDARD, null);
        // pos2
        assertNull(objectVersion);
        assertTrue(connector.listObjects(bucketName, "").iterator().hasNext());
    }

    /**
     * Creates a bucket, enables versioning, adds an object and overrides it with a
     * new content. Asserts that both returned version ids are not null and not equal
     */
    @Test
    public void testCreateBucketAndObjectsWithVersions() throws Exception
    {
        connector.createBucket(bucketName, null, PRIVATE);
        connector.setBucketVersioningStatus(bucketName, VersioningStatus.ENABLED);
        String versionId1 = connector.createObject(bucketName, "anObject", "hello", null, null, null,
            PRIVATE, StorageClass.STANDARD, null);
        assertNotNull(versionId1);
        String versionId2 = connector.createObject(bucketName, "anObject", "hello world", null, null, null,
            PRIVATE, StorageClass.STANDARD, null);
        assertNotNull(versionId2);
        assertFalse(versionId1.equals(versionId2));
    }
}