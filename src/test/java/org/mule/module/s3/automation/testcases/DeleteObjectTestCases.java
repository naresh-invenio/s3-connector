/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.s3.automation.testcases;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mule.api.MuleEvent;
import org.mule.api.processor.MessageProcessor;

import com.amazonaws.services.s3.model.S3ObjectSummary;

public class DeleteObjectTestCases extends S3TestParent {
	
	String bucketName;
	
	@Before
	public void setUp(){

		bucketName = UUID.randomUUID().toString();
		
		testObjects = new HashMap<String, Object>();
		testObjects.put("bucketName", bucketName);
    	
		try {

			MessageProcessor flow = lookupMessageProcessor("create-bucket");
			flow.process(getTestEvent(testObjects));
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
			
	}
	
	@After
	public void tearDown() {
		
		try {
				
			MessageProcessor flow = lookupMessageProcessor("delete-bucket-optional-attributes");
			flow.process(getTestEvent(testObjects));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
				fail();
		}
		
	}
    
	@Category({SmokeTests.class, SanityTests.class, RegressionTests.class})
 	@Test
 	public void testDeleteInputStreamObjectVersioningEnabled() {
     	
		InputStream inputStream = null;
    	
		testObjects.putAll((HashMap<String,Object>) context.getBean("deleteInputStreamObjectTestData"));

     	String host = testObjects.get("host").toString();
     	String path = testObjects.get("path").toString();
     	String urlString = String.format("http://%s/%s",host, path);
     	
 		try {
 				
 	    	URL url = new URL(urlString);
 	    	URLConnection connection = url.openConnection();
 	    	inputStream = connection.getInputStream();
 	    	
	    	testObjects.put("contentRef", inputStream);
 	    	
 	    	createDeleteAndCheckBucketContents(true);
 		
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			fail();
 		} finally {
 			if (inputStream != null) try { inputStream.close(); } catch (IOException logOrIgnore) {}
 		}
      
 	}
    
    @Category({SmokeTests.class, SanityTests.class, RegressionTests.class})
	@Test
	public void testDeleteInputStreamObject() {
    	
		InputStream inputStream = null;
    	
		testObjects.putAll((HashMap<String,Object>) context.getBean("deleteInputStreamObjectTestData"));

    	String host = testObjects.get("host").toString();
    	String path = testObjects.get("path").toString();
    	String urlString = String.format("http://%s/%s",host, path);
    	
		try {
			
	    	URL url = new URL(urlString);
	    	URLConnection connection = url.openConnection();
	    	inputStream = connection.getInputStream();	    
	    	
	    	testObjects.put("contentRef", inputStream);

	    	createDeleteAndCheckBucketContents(false);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} finally {
			if (inputStream != null) try { inputStream.close(); } catch (IOException logOrIgnore) {}
		}
     
	}
    
    @Category({SmokeTests.class, SanityTests.class, RegressionTests.class})
	@Test
	public void testDeleteStringObject() {
    	
		testObjects.putAll((HashMap<String,Object>) context.getBean("deleteStringObjectTestData"));
		
    	createDeleteAndCheckBucketContents(false);
     
	}
    
    @Category({SmokeTests.class, SanityTests.class, RegressionTests.class})
	@Test    
	public void testDeleteStringObjectVersioningEnabled() {
    	
		testObjects.putAll((HashMap<String,Object>) context.getBean("deleteStringObjectTestData"));
    	
		try {

 	    	createDeleteAndCheckBucketContents(true);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
     
	}
      
    private void createDeleteAndCheckBucketContents(boolean versioning) {
    	
    	String deleteFlowName = "delete-object";
    	
    	try {
    		
    		if (versioning){
    			
    			testObjects.put("versioningStatus", "ENABLED");
    			
    			MessageProcessor setBucketVersioningStatusFlow = lookupMessageProcessor("set-bucket-versioning-status");
    			setBucketVersioningStatusFlow.process(getTestEvent(testObjects)); 
    			
    			deleteFlowName.concat("-optional-attributes");
    			
    		}

			MessageProcessor createObjectFlow = lookupMessageProcessor("create-object-child-elements-from-message");
			createObjectFlow.process(getTestEvent(testObjects));
			
			MessageProcessor deleteObjectFlow = lookupMessageProcessor(deleteFlowName);
			deleteObjectFlow.process(getTestEvent(testObjects));
		
			MessageProcessor listObjectsFlow = lookupMessageProcessor("list-objects");
			MuleEvent listObjectsResponse = listObjectsFlow.process(getTestEvent(testObjects));
			Iterable<S3ObjectSummary> s3ObjectsSummaries = (Iterable<S3ObjectSummary>) listObjectsResponse.getMessage().getPayload();
		
			Iterator<S3ObjectSummary> iterator = s3ObjectsSummaries.iterator();  
			
			while (iterator.hasNext()) {
				
				S3ObjectSummary s3ObjectSummary = iterator.next();
				assertFalse(s3ObjectSummary.getKey().equals(testObjects.get("key").toString()));

			}
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
    	
    }
    
    @Category({SmokeTests.class, SanityTests.class, RegressionTests.class})
	@Test
	public void testDeleteByteArrayObject() {
    	
		testObjects.putAll((HashMap<String,Object>) context.getBean("deleteByteArrayObjectTestData"));

    	byte data[] = bucketName.getBytes();
    	testObjects.put("contentRef", data);
    	
    	createDeleteAndCheckBucketContents(false);
    	
	}
    
    @Category({SmokeTests.class, SanityTests.class, RegressionTests.class})
	@Test    
	public void testDeleteByteArrayObjectVersioningEnabled() {
    	
		testObjects.putAll((HashMap<String,Object>) context.getBean("deleteByteArrayObjectTestData"));

    	byte data[] = bucketName.getBytes();
    	testObjects.put("contentRef", data);
    	
    	createDeleteAndCheckBucketContents(true);
     
	}
    
    @Category({SmokeTests.class, SanityTests.class, RegressionTests.class})
	@Test
	public void testDeleteFileObject() {
    	
    	File temp = null;
    	
		testObjects.putAll((HashMap<String,Object>) context.getBean("deleteFileObjectTestData"));
    	
		try {
			
			temp = File.createTempFile("temp-file-name", ".tmp"); 
			
	    	testObjects.put("contentRef", temp);
			
	    	createDeleteAndCheckBucketContents(false);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} finally {
			if (temp != null) {	temp.delete(); }
		}
     
	}
    
    @Category({SmokeTests.class, SanityTests.class, RegressionTests.class})
	@Test    
	public void testDeleteFileObjectVersioningEnabled() {
    	
    	File temp = null;
    	
		testObjects.putAll((HashMap<String,Object>) context.getBean("deleteFileObjectTestData"));
    	
		try {
			
			temp = File.createTempFile("temp-file-name", ".tmp"); 
			
	    	testObjects.put("contentRef", temp);

	    	createDeleteAndCheckBucketContents(true);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} finally {
			if (temp != null) {	temp.delete(); }
		}
     
	}

}