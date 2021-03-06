package org.cedj.geekseek.domain.attachment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.cedj.geekseek.domain.Repository;
import org.cedj.geekseek.domain.attachment.model.Attachment;
import org.infinispan.Cache;

@Stateless
@LocalBean
@Typed(AttachmentRepository.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AttachmentRepository implements Repository<Attachment> {

    @Inject
    private Cache<Object, Map<String, String>> cache;

    @Override
    public Class<Attachment> getType() {
        return Attachment.class;
    }

    @Override
    public Attachment store(Attachment entity) {
        try {
            cache.put(entity.getId(), serialize(entity));
        } catch (Exception e) {
            throw new RuntimeException("Could not store Attachment with id " + entity.getId());
        }
        return get(entity.getId());
    }

    @Override
    public Attachment get(String id) {
        try {
            Map<String, String> value = (Map<String, String>)cache.get(id);
            if(value == null) {
                return null;
            }
            return deserialize(value);
        } catch (Exception e) {
            throw new RuntimeException("Could not retreive Attachment with id " + id, e);
        }
    }

    @Override
    public void remove(Attachment entity) {
        cache.remove(entity.getId());
    }

    private Map<String, String> serialize(Attachment attachment) throws Exception {
        Map<String, String> map = new HashMap<String, String>();

        for(Field f : attachment.getClass().getDeclaredFields()) {
            if(!Modifier.isStatic(f.getModifiers())) {
                if(!f.isAccessible()) {
                    f.setAccessible(true);
                }
                Object value = f.get(attachment);
                if(value != null) {
                    if(f.getType() == Date.class) {
                        value = ((Date)value).getTime();
                    }
                    map.put(f.getName(), String.valueOf(value));
                }
            }
        }
        return map;
    }

    private Attachment deserialize(Map<String, String> values) throws Exception {
        Attachment attachment = createNewInstance();

        for(Map.Entry<String, String> entry : values.entrySet()) {
            Field f =  Attachment.class.getDeclaredField(entry.getKey());
            if(!f.isAccessible()) {
                f.setAccessible(true);
            }
            if(f.getType() == String.class) {
                f.set(attachment, entry.getValue());
            } else if(f.getType() == Date.class) {
                Date date = new Date();
                date.setTime(Long.parseLong(entry.getValue()));
                f.set(attachment, date);
            } else if(f.getType() == URL.class) {
                f.set(attachment, new URL(entry.getValue()));
            }
        }
        return attachment;
    }

    private Attachment createNewInstance() throws NoSuchMethodException, InstantiationException, IllegalAccessException,
        InvocationTargetException {
        Constructor<Attachment> constructor = Attachment.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Attachment attachment = constructor.newInstance();
        return attachment;
    }
}
