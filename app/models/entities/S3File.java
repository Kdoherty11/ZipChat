package models.entities;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.hibernate.annotations.GenericGenerator;
import play.Logger;
import play.db.jpa.JPA;
import plugins.S3Plugin;

import javax.persistence.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

@Entity
@Table(name = "aws_files")
public class S3File {

    @Id
    @GenericGenerator(name = "s3_files_gen", strategy = "sequence", parameters = {
            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "s3_files_gen"),
            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
    })
    @GeneratedValue(generator = "s3_files_gen", strategy=GenerationType.SEQUENCE)
    public long fileId;

    private String bucket;

    public String name;

    @Transient
    public File file;

    public URL getUrl() throws MalformedURLException {
        return new URL("https://s3.amazonaws.com/" + bucket + "/" + getActualFileName());
    }

    private String getActualFileName() {
        return fileId + "/" + name;
    }

    public void save() {
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not save because amazonS3 was null");
            throw new RuntimeException("Could not save");
        } else {
            this.bucket = S3Plugin.s3Bucket;

            JPA.em().persist(this);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, getActualFileName(), file);
            putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public for all
            S3Plugin.amazonS3.putObject(putObjectRequest); // upload file
        }
    }

    public void delete() {
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not delete because amazonS3 was null");
            throw new RuntimeException("Could not delete");
        }
        else {
            S3Plugin.amazonS3.deleteObject(bucket, getActualFileName());
            JPA.em().remove(this);
        }
    }

}
