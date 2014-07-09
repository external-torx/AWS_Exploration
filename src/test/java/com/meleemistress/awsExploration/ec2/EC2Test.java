/**
 * Copyright (c) 2012, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.meleemistress.awsExploration.ec2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * @author hparry
 *
 */
public class EC2Test {

    @Test
    public void testLaunchInstance() {
        File base = new File("target/test-classes/demo_scenario1/");
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                                            "Cannot load the credentials from the credential profiles file. "
                                                    + "Please make sure that your credentials file is at the correct "
                                                    + "location (~/.aws/credentials), and is in valid format.",
                                            e);
        }
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        AmazonS3 s3 = new AmazonS3Client(credentials);
        s3.setRegion(usWest2);
        CreateBucketRequest bucketReq = new CreateBucketRequest(
                                                                "meleemistress-" + System.currentTimeMillis());
        Bucket bucket = s3.createBucket(bucketReq);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket.getName(), "file1.txt", new File(base, "s3/file1.txt"));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text");
        putObjectRequest.setMetadata(metadata);
        s3.putObject(putObjectRequest);
        
        // Create the AmazonEC2Client object so we can call various APIs.
        AmazonEC2 ec2 = new AmazonEC2Client(credentials);

        ec2.setRegion(usWest2);

        String amiId = "ami-1b3b462b";
        RunInstancesRequest instanceReq = new RunInstancesRequest();
        instanceReq.setImageId(amiId);
        instanceReq.setMaxCount(1);
        instanceReq.setMinCount(1);
        RunInstancesResult result = ec2.runInstances(instanceReq);
        List<String> instanceIds = new ArrayList<String>();
        for (Instance i : result.getReservation().getInstances()) {
            instanceIds.add(i.getInstanceId());
        }
        TerminateInstancesRequest terminateReq = new TerminateInstancesRequest();
        terminateReq.setInstanceIds(instanceIds);
        ec2.terminateInstances(terminateReq);

 

    }

}
