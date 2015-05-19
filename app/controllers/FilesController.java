package controllers;

import models.entities.S3File;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class FilesController extends BaseController {

    @Transactional
    public static Result index() {
        CriteriaQuery<S3File> cq = JPA.em().getCriteriaBuilder().createQuery(S3File.class);
        Root<S3File> root = cq.from(S3File.class);
        CriteriaQuery<S3File> all = cq.select(root);
        TypedQuery<S3File> allQuery = JPA.em().createQuery(all);

        List<S3File> uploads = allQuery.getResultList();
        return ok(index.render(uploads));
    }

    @Transactional
    public static Result show(long fileId) {
        return show(S3File.class, fileId);
    }

    @Transactional
    public static Result upload() {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart uploadFilePart = body.getFile("upload");
        if (uploadFilePart != null) {
            S3File s3File = new S3File();
            s3File.name = uploadFilePart.getFilename();
            s3File.file = uploadFilePart.getFile();
            s3File.save();
            return okJson(s3File);
        } else {
            return badRequest("File upload error");
        }
    }
}
