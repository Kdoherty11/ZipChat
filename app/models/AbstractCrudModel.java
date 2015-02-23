package models;

import utils.CrudUtils;
import play.data.DynamicForm;
import play.db.ebean.Model;
import play.libs.F;
import play.mvc.Result;

import java.lang.reflect.Field;

import static play.libs.Json.toJson;

/**
 * Created by kevindoherty on 2/21/15.
 */
public class AbstractCrudModel extends Model {

    public F.Promise<Result> create(CrudUtils.Callback cb) {
        save();
        return F.Promise.promise(() -> cb.success(toJson(this)));
    }

    public F.Promise<Result> show(CrudUtils.Callback cb) {
        return F.Promise.promise(() -> cb.success(toJson(this)));
    }

    public F.Promise<Result> update(DynamicForm requestForm, CrudUtils.Callback cb) {
        boolean updated = false;

        Field[] modelFields = getClass().getDeclaredFields();
        int fieldIndex = 0;

        for (Field field : modelFields) {
            fieldIndex++;

            if (CrudUtils.canBeUpdated(field)) {

                String updateFieldValue = requestForm.get(field.getName());

                // The field is included in the update request
                if (updateFieldValue != null) {
                    _ebean_setField(fieldIndex, this, updateFieldValue);
                    updated = true;
                }
            }
        }

        if (updated) {
            update();
        }

        return F.Promise.promise(() -> cb.success(toJson(this)));
    }

    public F.Promise<Result> delete(CrudUtils.Callback cb) {
        delete();
        return F.Promise.promise(() -> cb.success(toJson(CrudUtils.DELETE_SUCCESS_RESPONSE)));
    }



}
